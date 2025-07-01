package salex.messenger.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import salex.messenger.config.JwtConfig;

@Component
@RequiredArgsConstructor
public class JwtHelper {
    private final JwtConfig jwtConfig;

    public String createToken(Map<String, Object> claims, String subject) {
        Date expiryDate = Date.from(Instant.ofEpochMilli(
                System.currentTimeMillis() + jwtConfig.lifetime().toMillis()));

        Key hmacKey = new SecretKeySpec(
                Base64.getDecoder().decode(jwtConfig.secret()), SignatureAlgorithm.HS256.getJcaName());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiryDate)
                .signWith(hmacKey)
                .compact();
    }

    public String extractUsername(String bearerToken) {
        return extractClaimBody(bearerToken, Claims::getSubject);
    }

    public <T> T extractClaimBody(String bearerToken, Function<Claims, T> claimsResolver) {
        Jws<Claims> jwsClaims = extractClaims(bearerToken);
        return claimsResolver.apply(jwsClaims.getBody());
    }

    private Jws<Claims> extractClaims(String bearerToken) {
        return Jwts.parserBuilder().setSigningKey(jwtConfig.secret()).build().parseClaimsJws(bearerToken);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String bearerToken) {
        return extractExpiry(bearerToken).before(new Date());
    }

    public Date extractExpiry(String bearerToken) {
        return extractClaimBody(bearerToken, Claims::getExpiration);
    }
}
