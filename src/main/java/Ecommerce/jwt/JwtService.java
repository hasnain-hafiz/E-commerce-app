package Ecommerce.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final String secretKey;

    // CHANGED (Phase 3): was hardcoded to 100*60*60*10 ms (~41 days) — an
    // effectively long-lived bearer token with no way to shorten its
    // lifetime short of revoking it outright. Now short-lived (default 15
    // minutes) and configurable; long-lived sessions are handled by the
    // new server-side-revocable refresh token instead of a long-lived,
    // stateless access token.
    private final long accessTokenExpirationMs;

    public JwtService(@Value("${jwt.secret}") String secretKey,
                       @Value("${jwt.access-token-expiration-ms:900000}") long accessTokenExpirationMs) {
        this.secretKey = secretKey;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(),userDetails);
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public  String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractSubject(token);

        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = extractClaim(token, Claims::getExpiration);

        return expiration.before(new Date());
    }

}
