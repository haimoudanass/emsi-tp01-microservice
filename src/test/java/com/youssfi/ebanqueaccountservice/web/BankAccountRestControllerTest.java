package com.youssfi.ebanqueaccountservice.web;

import com.youssfi.ebanqueaccountservice.entities.BankAccount;
import com.youssfi.ebanqueaccountservice.enums.AccountType;
import com.youssfi.ebanqueaccountservice.repositories.BankAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.UUID;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BankAccountRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    void shouldReturnInitializedAccounts() throws Exception {
        long count = bankAccountRepository.count();

        mockMvc.perform(get("/bankAccounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value((int) count));
    }

    @Test
    void shouldReturnAccountById() throws Exception {
        BankAccount account = bankAccountRepository.findAll().get(0);

        mockMvc.perform(get("/bankAccounts/{id}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId()))
                .andExpect(jsonPath("$.currency").value(account.getCurrency()));
    }

    @Test
    void shouldCreateAccount() throws Exception {
        long before = bankAccountRepository.count();
        String body = "{\"balance\":1500.0,\"currency\":\"EUR\",\"type\":\"CURRENT_ACCOUNT\"}";

        mockMvc.perform(post("/bankAccounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.type").value("CURRENT_ACCOUNT"))
                .andExpect(jsonPath("$.balance").value(1500.0));

        assertEquals(before + 1, bankAccountRepository.count());
    }

    @Test
    void shouldUpdateCurrencyWithoutOverwritingBalance() throws Exception {
        BankAccount account = bankAccountRepository.save(BankAccount.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(new Date())
                .balance(12345.67)
                .currency("MAD")
                .type(AccountType.CURRENT_ACCOUNT)
                .build());

        mockMvc.perform(put("/bankAccounts/{id}", account.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currency\":\"USD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(closeTo(12345.67, 0.0001)));
    }

    @Test
    void shouldAcceptZeroBalanceOnUpdate() throws Exception {
        BankAccount account = bankAccountRepository.save(BankAccount.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(new Date())
                .balance(2500.0)
                .currency("MAD")
                .type(AccountType.SAVING_ACCOUNT)
                .build());

        mockMvc.perform(put("/bankAccounts/{id}", account.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"balance\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0.0));

        assertEquals(0.0, bankAccountRepository.findById(account.getId()).orElseThrow().getBalance());
    }

    @Test
    void shouldDeleteAccount() throws Exception {
        BankAccount account = bankAccountRepository.save(BankAccount.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(new Date())
                .balance(100.0)
                .currency("MAD")
                .type(AccountType.SAVING_ACCOUNT)
                .build());

        mockMvc.perform(delete("/bankAccounts/{id}", account.getId()))
                .andExpect(status().isOk());

        assertFalse(bankAccountRepository.findById(account.getId()).isPresent());
    }
}
