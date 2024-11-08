package org.nurfet.jwtapplication.jwt.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.jwtapplication.jwt.JwtConfig.JwtConfig;
import org.nurfet.jwtapplication.service.UserService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtUtil {

    private final JwtConfig jwtConfig;
    private final SecretKey jwtAccessSecret;
    private final SecretKey jwtRefreshSecret;
    private final UserService userService;


    public JwtUtil(JwtConfig jwtConfig, UserService userService) {
        this.jwtConfig = jwtConfig;
        this.jwtAccessSecret = generateSecretKey(jwtConfig.getAccessSecret());
        this.jwtRefreshSecret = generateSecretKey(jwtConfig.getRefreshSecret());
        this.userService = userService;
    }

    private SecretKey generateSecretKey(String secret) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    private String createToken(Map<String, Object> claims, String subject,
                               SecretKey key, int expirationMinutes) {
        LocalDateTime now = LocalDateTime.now();

        Date issuedAt = DateUtils.toDate(now);
        Date expiration = DateUtils.toDate(now.plusMinutes(expirationMinutes));

        log.info("Создание токена для субъекта: {}", subject);
        log.info("Время создания токена: {}", now);
        log.info("Срок действия токена: {}", expiration);
        log.info("Срок действия токена: {} минут", expirationMinutes);


        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key, Jwts.SIG.HS256)
                .compact();

    }

    public String generateToken(UserDetails user, boolean isRefreshToken) {

        Map<String, Object> claims = new HashMap<>();

        if (isRefreshToken) {
            claims.put(JwtConstants.REFRESH_TOKEN_CLAIM, true);
        } else {
            String roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
            claims.put(JwtConstants.ROLES_CLAIM, roles);

            org.nurfet.jwtapplication.model.User users = userService.findUserByUserName(user.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Добавляем ID пользователя в claims
            claims.put("id", users.getId());
        }

        int expirationMinutes = isRefreshToken ? jwtConfig.getRefreshTokenExpiration() : jwtConfig.getAccessTokenExpiration();
        SecretKey key = isRefreshToken ? jwtRefreshSecret : jwtAccessSecret;


        return createToken(claims, user.getUsername(), key, expirationMinutes);
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private boolean validateToken(String token, SecretKey key)  {
        try {
            return !isTokenExpired(extractClaims(token, key));
        } catch (ExpiredJwtException expEx) {
            throw new JwtException("Срок действия JWT токена истек", expEx);
        } catch (UnsupportedJwtException unsEx) {
            throw new JwtException("Неподдерживаемый JWT токен", unsEx);
        } catch (MalformedJwtException mjEx) {
            throw new JwtException("Неправильно сформированный JWT токен", mjEx);
        } catch (JwtException | ResponseStatusException sEx) {
            throw new JwtException("Недействительный JWT токен", sEx);
        }
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, jwtAccessSecret);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, jwtRefreshSecret);
    }

    private Claims extractClaims(String token, SecretKey secretKey) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims getAccessClaims(String token) {
        return extractClaims(token, jwtAccessSecret);
    }

    public Claims getRefreshClaims(String token) {
        return extractClaims(token, jwtRefreshSecret);
    }

    public Authentication getAuthentication(String token) {

        Claims claims = getAccessClaims(token);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(JwtConstants.ROLES_CLAIM).toString().split(","))
                        .map(String::trim)
                        .filter(role -> !role.isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails userDetails = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
    }
}
