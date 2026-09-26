package com.stocksense.service;

import com.stocksense.domain.AppUser;
import com.stocksense.domain.DomainTypes.RoleName;
import com.stocksense.dto.ApiDtos.*;
import com.stocksense.exception.BadRequestException;
import com.stocksense.exception.NotFoundException;
import com.stocksense.repository.UserRepository;
import com.stocksense.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private record Otp(String value,Instant expiresAt) {}
    private final UserRepository users; private final PasswordEncoder passwords; private final JwtService jwt;
    private final Map<String,Otp> otps=new ConcurrentHashMap<>(); private final SecureRandom random=new SecureRandom();
    @Value("${app.reset.expose-otp:true}") private boolean exposeOtp;
    public AuthService(UserRepository users,PasswordEncoder passwords,JwtService jwt) { this.users=users; this.passwords=passwords; this.jwt=jwt; }
    @Transactional public AuthResponse register(RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) throw new BadRequestException("Email is already registered");
        AppUser user=new AppUser(); user.setName(request.name().trim()); user.setEmail(request.email().trim().toLowerCase()); user.setPasswordHash(passwords.encode(request.password())); user.getRoles().add(RoleName.STAFF);
        return response(users.save(user));
    }
    @Transactional(readOnly=true) public AuthResponse login(LoginRequest request) {
        AppUser user=users.findByEmailIgnoreCase(request.email()).orElseThrow(()->new BadRequestException("Invalid email or password"));
        if (!passwords.matches(request.password(),user.getPasswordHash())) throw new BadRequestException("Invalid email or password");
        return response(user);
    }
    @Transactional(readOnly=true) public MessageResponse forgot(ForgotRequest request) {
        var user=users.findByEmailIgnoreCase(request.email());
        if (user.isEmpty()) return new MessageResponse("If the account exists, a reset code has been issued",null);
        String otp=String.format("%06d",random.nextInt(1_000_000)); otps.put(user.get().getEmail(),new Otp(otp,Instant.now().plusSeconds(600)));
        return new MessageResponse("Reset code issued; it expires in 10 minutes",exposeOtp?otp:null);
    }
    @Transactional public MessageResponse reset(ResetRequest request) {
        String email=request.email().trim().toLowerCase(); Otp otp=otps.get(email);
        if (otp==null || !otp.expiresAt().isAfter(Instant.now()) || !otp.value().equals(request.otp())) throw new BadRequestException("Reset code is invalid or expired");
        AppUser user=users.findByEmailIgnoreCase(email).orElseThrow(()->new NotFoundException("User not found"));
        user.setPasswordHash(passwords.encode(request.newPassword())); users.save(user); otps.remove(email);
        return new MessageResponse("Password updated",null);
    }
    private AuthResponse response(AppUser user) { return new AuthResponse(jwt.generate(user),user.getId(),user.getName(),user.getEmail(),user.getRoles().stream().map(Enum::name).toList()); }
}
