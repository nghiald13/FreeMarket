package com.ldn.authservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.grpc.server.security.AuthenticationProcessInterceptor;
import org.springframework.grpc.server.security.GrpcSecurity;

@Configuration
public class GrpcSecurityConfig {
    @Bean
    @GlobalServerInterceptor
    AuthenticationProcessInterceptor jwtSecurityFilterChain(GrpcSecurity grpc) throws Exception {
        return grpc
                .authorizeRequests(requests -> requests
//                        .methods("Simple/StreamHello").hasAuthority("ROLE_ADMIN")
//                        .methods("Simple/SayHello").hasAuthority("ROLE_USER")
                        .methods("AccountGrpcService/GetAccountInfo").permitAll()
                        .methods("grpc.*/*").permitAll()
                        .allRequests().denyAll())
                .build();
    }
}
