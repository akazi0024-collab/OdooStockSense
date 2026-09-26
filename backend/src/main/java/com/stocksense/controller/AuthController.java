package com.stocksense.controller;

import com.stocksense.dto.ApiDtos.*;
import com.stocksense.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service=service; }
    @PostMapping("/register") public AuthResponse register(@Valid @RequestBody RegisterRequest request) { return service.register(request); }
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request) { return service.login(request); }
    @PostMapping("/forgot-password") public MessageResponse forgot(@Valid @RequestBody ForgotRequest request) { return service.forgot(request); }
    @PostMapping("/verify-otp") public MessageResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) { return service.verifyOtp(request); }
    @PostMapping("/reset-password") public MessageResponse reset(@Valid @RequestBody ResetRequest request) { return service.reset(request); }
}
