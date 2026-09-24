package com.ldn.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@EnableWebSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass
public class SecurityConfig {

    private final RequestMappingHandlerMapping handlerMapping;

    // Dùng @Lazy để tránh lỗi vòng lặp phụ thuộc (Circular Dependency) khi khởi tạo Bean
    public SecurityConfig(@Lazy RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Lấy danh sách tất cả các URL được đánh dấu @Public
        String[] publicUrls = getPublicUrls();

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicUrls).permitAll() // Mở khóa tự động cho tất cả @Public
                        .anyRequest().authenticated()            // Tất cả endpoint còn lại bắt buộc phải Login
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    private String[] getPublicUrls() {
        List<String> publicUrls = new ArrayList<>();

        handlerMapping.getHandlerMethods().forEach((requestMappingInfo, handlerMethod) -> {
            // Kiểm tra xem Method hoặc Controller Class có gắn @Public không
            boolean isPublicOnMethod = handlerMethod.hasMethodAnnotation(Public.class);
            boolean isPublicOnClass = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Public.class) != null;

            if (isPublicOnMethod || isPublicOnClass) {
                // Lấy tất cả path/pattern của endpoint đó (Ví dụ: /register, /auth/login,...)
                if (requestMappingInfo.getPatternValues() != null) {
                    publicUrls.addAll(requestMappingInfo.getPatternValues());
                }
            }
        });

        return publicUrls.toArray(new String[0]);
    }
}