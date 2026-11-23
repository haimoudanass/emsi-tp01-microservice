package com.youssfi.ebanqueaccountservice.mappers;

import com.youssfi.ebanqueaccountservice.dtos.BankAccountRequestDTO;
import com.youssfi.ebanqueaccountservice.dtos.BankAccountResponseDTO;
import com.youssfi.ebanqueaccountservice.entities.BankAccount;
import org.springframework.stereotype.Component;

/**
 * Mapper entre entités JPA et DTOs.
 * Politique null :
 * - argument entièrement null => retourne null
 * - balance absente (null) dans un RequestDTO lors de toBankAccount => IllegalArgumentException
 * - ne génère jamais d'identifiant
 */
@Component
public class BankAccountMapper {

    public BankAccountResponseDTO fromBankAccount(BankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }
        return BankAccountResponseDTO.builder()
                .id(bankAccount.getId())
                .createdAt(bankAccount.getCreatedAt())
                .balance(bankAccount.getBalance())
                .currency(bankAccount.getCurrency())
                .type(bankAccount.getType())
                .build();
    }

    public BankAccount toBankAccount(BankAccountRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        if (dto.getBalance() == null) {
            throw new IllegalArgumentException("balance is required to build a complete BankAccount entity");
        }
        return BankAccount.builder()
                .createdAt(dto.getCreatedAt())
                .balance(dto.getBalance())
                .currency(dto.getCurrency())
                .type(dto.getType())
                .build();
    }
}
