package br.com.fiap.inovagab.security;

import java.time.Instant;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.fiap.inovagab.user.document.UserDocument;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public GeneratedToken generate(UserDocument user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.expiration());

        var headers = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT")
                .build();
        var claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getId())
                .claim("roles", List.of(user.getRole().name()))
                .build();

        String value = jwtEncoder.encode(
                JwtEncoderParameters.from(headers, claims)
        ).getTokenValue();

        return new GeneratedToken(
                value,
                expiresAt,
                properties.expiration().toSeconds()
        );
    }

    public record GeneratedToken(
            String value,
            Instant expiresAt,
            long expiresIn
    ) {
    }
}
