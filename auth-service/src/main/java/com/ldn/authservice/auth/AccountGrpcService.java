package com.ldn.authservice.auth;

import com.ldn.authservice.auth.entities.Account;
import com.ldn.authservice.auth.repositories.AccountRepository;
import com.ldn.grpc.proto.AccountGrpcServiceGrpc;
import com.ldn.grpc.proto.AccountRequest;
import com.ldn.grpc.proto.AccountResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class AccountGrpcService extends AccountGrpcServiceGrpc.AccountGrpcServiceImplBase {
    private final AccountRepository accountRepository;

    @Override
    public void getAccountInfo(AccountRequest request, StreamObserver<AccountResponse> responseObserver) {
        Account account = this.accountRepository.findById(request.getId()).orElse(null);
        AccountResponse accountResponse = null;
        if (account != null)
            accountResponse = AccountResponse.newBuilder()
                .setId(account.getId())
                .setEmail(account.getEmail())
                .setFullName(account.getName())
                .build();
        responseObserver.onNext(accountResponse);
        responseObserver.onCompleted();
    }
}
