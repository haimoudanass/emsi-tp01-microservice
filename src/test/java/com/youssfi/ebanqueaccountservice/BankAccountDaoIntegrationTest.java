package com.youssfi.ebanqueaccountservice;

import com.youssfi.ebanqueaccountservice.entities.BankAccount;
import com.youssfi.ebanqueaccountservice.repositories.BankAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BankAccountDaoIntegrationTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    void shouldPersistTenBankAccountsWithUniqueIds() {
        long count = bankAccountRepository.count();
        assertEquals(10, count);

        List<BankAccount> accounts = bankAccountRepository.findAll();
        assertEquals(10, accounts.size());

        Set<String> ids = new HashSet<>();
        for (BankAccount account : accounts) {
            assertNotNull(account.getId());
            assertTrue(ids.add(account.getId()), "Duplicate id: " + account.getId());
            assertEquals("MAD", account.getCurrency());
            assertNotNull(account.getCreatedAt());
            assertNotNull(account.getType());
        }
        assertEquals(10, ids.size());
    }

}
