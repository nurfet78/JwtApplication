package org.nurfet.jwtapplication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private Instant creationTime;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private Instant expirationTime;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public RefreshToken(String username, String token, Instant expirationTime, Instant creationTime, User user) {
        this.username = username;
        this.token = token;
        this.creationTime = creationTime;
        this.expirationTime = expirationTime;
        this.user = user;
    }
}
