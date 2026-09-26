package com.stocksense.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name="app_users", uniqueConstraints=@UniqueConstraint(columnNames="email"))
@Getter @Setter @NoArgsConstructor
public class AppUser extends BaseEntity {
    @Column(nullable=false, length=120) private String name;
    @Column(nullable=false, length=190) private String email;
    @Column(nullable=false) private String passwordHash;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="user_roles", joinColumns=@JoinColumn(name="user_id"))
    @Enumerated(EnumType.STRING) @Column(name="role", nullable=false)
    private Set<DomainTypes.RoleName> roles = new HashSet<>();
}
