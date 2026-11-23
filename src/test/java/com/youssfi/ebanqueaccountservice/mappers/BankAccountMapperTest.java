package com.youssfi.ebanqueaccountservice.mappers;

import com.youssfi.ebanqueaccountservice.dtos.BankAccountRequestDTO;
import com.youssfi.ebanqueaccountservice.dtos.BankAccountResponseDTO;
import com.youssfi.ebanqueaccountservice.entities.BankAccount;
import com.youssfi.ebanqueaccountservice.enums.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BankAccountMapperTest {

    private BankAccountMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BankAccountMapper();
    }

    @Test
    void shouldMapEntityToResponseDto() {
        Date createdAt = new Date();
        BankAccount account = BankAccount.builder()
                .id("acc-1")
                .createdAt(createdAt)
                .balance(1500.5)
                .currency("MAD")
                .type(AccountType.CURRENT_ACCOUNT)
                .build();

        BankAccountResponseDTO dto = mapper.fromBankAccount(account);

        assertEquals("acc-1", dto.getId());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(1500.5, dto.getBalance());
        assertEquals("MAD", dto.getCurrency());
        assertEquals(AccountType.CURRENT_ACCOUNT, dto.getType());
    }

    @Test
    void shouldMapRequestDtoToEntityWithoutGeneratingId() {
        Date createdAt = new Date();
        BankAccountRequestDTO dto = BankAccountRequestDTO.builder()
                .createdAt(createdAt)
                .balance(2500.0)
                .currency("EUR")
                .type(AccountType.SAVING_ACCOUNT)
                .build();

        BankAccount account = mapper.toBankAccount(dto);

        assertNull(account.getId());
        assertEquals(createdAt, account.getCreatedAt());
        assertEquals(2500.0, account.getBalance());
        assertEquals("EUR", account.getCurrency());
        assertEquals(AccountType.SAVING_ACCOUNT, account.getType());
    }

    @Test
    void shouldPreserveZeroBalanceWhenMappingBothWays() {
        BankAccount account = BankAccount.builder()
                .id("acc-zero")
                .createdAt(new Date())
                .balance(0.0)
                .currency("MAD")
                .type(AccountType.CURRENT_ACCOUNT)
                .build();

        BankAccountResponseDTO response = mapper.fromBankAccount(account);
        assertEquals(0.0, response.getBalance());

        BankAccountRequestDTO request = BankAccountRequestDTO.builder()
                .balance(0.0)
                .currency("MAD")
                .type(AccountType.SAVING_ACCOUNT)
                .createdAt(new Date())
                .build();

        BankAccount mapped = mapper.toBankAccount(request);
        assertEquals(0.0, mapped.getBalance());
        assertNull(mapped.getId());
    }

    @Test
    void shouldReturnNullWhenArgumentsAreNull() {
        assertNull(mapper.fromBankAccount(null));
        assertNull(mapper.toBankAccount(null));
    }

    @Test
    void shouldRejectMissingBalanceWhenBuildingEntity() {
        BankAccountRequestDTO dto = BankAccountRequestDTO.builder()
                .currency("MAD")
                .type(AccountType.CURRENT_ACCOUNT)
                .createdAt(new Date())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> mapper.toBankAccount(dto));
        assertEquals("balance is required to build a complete BankAccount entity", exception.getMessage());
    }
}
