package com.youssfi.ebanqueaccountservice.service;

import com.youssfi.ebanqueaccountservice.dtos.BankAccountRequestDTO;
import com.youssfi.ebanqueaccountservice.dtos.BankAccountResponseDTO;
import com.youssfi.ebanqueaccountservice.entities.BankAccount;
import com.youssfi.ebanqueaccountservice.enums.AccountType;
import com.youssfi.ebanqueaccountservice.repositories.BankAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BankAccountServiceImplTest {

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    void shouldCreateAccountWithGeneratedUuidAndDate() {
        BankAccountRequestDTO request = BankAccountRequestDTO.builder()
                .balance(15000.0)
                .currency("MAD")
                .type(AccountType.CURRENT_ACCOUNT)
                .build();

        BankAccountResponseDTO response = bankAccountService.addAccount(request);

        assertNotNull(response.getId());
        assertDoesNotThrow(() -> UUID.fromString(response.getId()));
        assertNotNull(response.getCreatedAt());
        assertEquals(15000.0, response.getBalance());
        assertEquals("MAD", response.getCurrency());
        assertEquals(AccountType.CURRENT_ACCOUNT, response.getType());
        assertTrue(bankAccountRepository.findById(response.getId()).isPresent());
    }

    @Test
    void shouldCreateAccountWithZeroBalance() {
        BankAccountResponseDTO response = bankAccountService.addAccount(BankAccountRequestDTO.builder()
                .balance(0.0)
                .currency("EUR")
                .type(AccountType.SAVING_ACCOUNT)
                .build());

        assertEquals(0.0, response.getBalance());
        assertNotNull(response.getId());
    }

    @Test
    void shouldRejectMissingBalanceOnCreate() {
        BankAccountRequestDTO request = BankAccountRequestDTO.builder()
                .currency("MAD")
                .type(AccountType.CURRENT_ACCOUNT)
                .build();

        assertThrows(IllegalArgumentException.class, () -> bankAccountService.addAccount(request));
    }

    @Test
    void shouldListAccounts() {
        List<BankAccountResponseDTO> accounts = bankAccountService.getAccounts();
        assertEquals(bankAccountRepository.count(), accounts.size());
        assertFalse(accounts.isEmpty());
    }

    @Test
    void shouldGetAccountById() {
        BankAccount existing = bankAccountRepository.findAll().get(0);
        BankAccountResponseDTO response = bankAccountService.getAccountById(existing.getId());

        assertEquals(existing.getId(), response.getId());
        assertEquals(existing.getCurrency(), response.getCurrency());
        assertEquals(existing.getBalance(), response.getBalance());
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        String missingId = "00000000-0000-0000-0000-000000000000";
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bankAccountService.getAccountById(missingId));
        assertEquals("Account " + missingId + " not found", exception.getMessage());
    }

    @Test
    void shouldUpdateCurrencyWithoutChangingBalance() {
        BankAccountResponseDTO created = bankAccountService.addAccount(BankAccountRequestDTO.builder()
                .balance(12345.67)
                .currency("MAD")
                .type(AccountType.CURRENT_ACCOUNT)
                .createdAt(new Date())
                .build());

        BankAccountResponseDTO updated = bankAccountService.updateAccount(created.getId(),
                BankAccountRequestDTO.builder().currency("USD").build());

        assertEquals(created.getId(), updated.getId());
        assertEquals("USD", updated.getCurrency());
        assertEquals(12345.67, updated.getBalance());
    }

    @Test
    void shouldUpdateBalanceToZeroAndKeepId() {
        BankAccountResponseDTO created = bankAccountService.addAccount(BankAccountRequestDTO.builder()
                .balance(2500.0)
                .currency("MAD")
                .type(AccountType.SAVING_ACCOUNT)
                .build());

        BankAccountResponseDTO updated = bankAccountService.updateAccount(created.getId(),
                BankAccountRequestDTO.builder().balance(0.0).build());

        assertEquals(created.getId(), updated.getId());
        assertEquals(0.0, updated.getBalance());
        assertEquals("MAD", updated.getCurrency());
    }

    @Test
    void shouldDeleteAccount() {
        BankAccountResponseDTO created = bankAccountService.addAccount(BankAccountRequestDTO.builder()
                .balance(100.0)
                .currency("MAD")
                .type(AccountType.SAVING_ACCOUNT)
                .build());

        bankAccountService.deleteAccount(created.getId());
        assertFalse(bankAccountRepository.findById(created.getId()).isPresent());
    }
}
