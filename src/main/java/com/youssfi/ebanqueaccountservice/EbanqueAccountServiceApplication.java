package com.youssfi.ebanqueaccountservice;

import com.youssfi.ebanqueaccountservice.entities.BankAccount;
import com.youssfi.ebanqueaccountservice.enums.AccountType;
import com.youssfi.ebanqueaccountservice.repositories.BankAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.UUID;

@SpringBootApplication
public class EbanqueAccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbanqueAccountServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner start(BankAccountRepository bankAccountRepository) {
        return args -> {
            for (int i = 0; i < 10; i++) {
                BankAccount bankAccount = BankAccount.builder()
                        .id(UUID.randomUUID().toString())
                        .createdAt(new Date())
                        .balance(1000 + Math.random() * 60000)
                        .currency("MAD")
                        .type(Math.random() > 0.5 ? AccountType.CURRENT_ACCOUNT : AccountType.SAVING_ACCOUNT)
                        .build();
                bankAccountRepository.save(bankAccount);
            }
        };
    }

}
