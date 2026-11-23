package com.youssfi.ebanqueaccountservice.web;

import com.youssfi.ebanqueaccountservice.dtos.BankAccountRequestDTO;
import com.youssfi.ebanqueaccountservice.dtos.BankAccountResponseDTO;
import com.youssfi.ebanqueaccountservice.service.BankAccountService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class BankAccountGraphQLController {

    private final BankAccountService bankAccountService;

    public BankAccountGraphQLController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @QueryMapping
    public List<BankAccountResponseDTO> accounts() {
        return bankAccountService.getAccounts();
    }

    @QueryMapping
    public BankAccountResponseDTO accountById(@Argument String id) {
        return bankAccountService.getAccountById(id);
    }

    @MutationMapping
    public BankAccountResponseDTO addAccount(@Argument("bankAccount") BankAccountRequestDTO bankAccount) {
        return bankAccountService.addAccount(bankAccount);
    }

    @MutationMapping
    public BankAccountResponseDTO updateAccount(@Argument String id,
                                                @Argument("bankAccount") BankAccountRequestDTO bankAccount) {
        return bankAccountService.updateAccount(id, bankAccount);
    }

    @MutationMapping
    public Boolean deleteAccount(@Argument String id) {
        bankAccountService.deleteAccount(id);
        return true;
    }

    /**
     * Expose createdAt as ISO-8601 string (e.g. 2026-11-22T15:00:00.123Z).
     * Null-safe: returns null when the date is absent.
     */
    @SchemaMapping(typeName = "BankAccountDTO", field = "createdAt")
    public String createdAt(BankAccountResponseDTO bankAccount) {
        if (bankAccount == null || bankAccount.getCreatedAt() == null) {
            return null;
        }
        return bankAccount.getCreatedAt().toInstant().toString();
    }
}
