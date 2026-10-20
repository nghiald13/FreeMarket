package com.ldn.financeservice;

import com.ldn.grpc.proto.AccountGrpcServiceGrpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ImportGrpcClients(target = "auth-service", types = AccountGrpcServiceGrpc.AccountGrpcServiceBlockingStub.class)
public class FinanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceServiceApplication.class, args);
    }

}
