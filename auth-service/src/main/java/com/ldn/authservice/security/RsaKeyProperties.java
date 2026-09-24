package com.ldn.authservice.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Binds jwt.public-key / jwt.private-key (classpath: or file: locations
 * pointing at PEM files) straight into RSA key objects, thanks to Spring
 * Security's RsaKeyConversionServicePostProcessor registered in JwtConfig.
 */
@ConfigurationProperties(prefix = "jwt")
public record RsaKeyProperties(RSAPublicKey publicKey, RSAPrivateKey privateKey) {}
