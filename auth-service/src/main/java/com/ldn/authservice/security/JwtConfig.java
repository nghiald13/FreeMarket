package com.ldn.authservice.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.crypto.RsaKeyConversionServicePostProcessor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.util.UUID;

@Configuration
@EnableConfigurationProperties(RsaKeyProperties.class)
@RequiredArgsConstructor
public class JwtConfig {

    private final RsaKeyProperties rsaKeys;

    /** Encodes (signs) JwtClaimsSet -> compact JWT string, used when issuing tokens. */
    @Bean
    public JwtEncoder jwtEncoder() {
        RSAKey rsaKey = new RSAKey.Builder(rsaKeys.publicKey())
                .privateKey(rsaKeys.privateKey())
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwks);
    }

    /** Decodes + verifies a JWT signature using the public key only. */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsaKeys.publicKey()).build();
    }

    /**
     * Registers the converter that turns "classpath:certs/public.pem" style
     * @Value / @ConfigurationProperties strings into RSAPublicKey/RSAPrivateKey.
     * Must be a static @Bean method so it runs before property binding.
     */
    @Bean
    static RsaKeyConversionServicePostProcessor rsaKeyConversionServicePostProcessor() {
        return new RsaKeyConversionServicePostProcessor();
    }
}
