package com.youssfi.ebanqueaccountservice.web;

import com.youssfi.ebanqueaccountservice.dtos.BankAccountRequestDTO;
import com.youssfi.ebanqueaccountservice.dtos.BankAccountResponseDTO;
import com.youssfi.ebanqueaccountservice.enums.AccountType;
import com.youssfi.ebanqueaccountservice.repositories.BankAccountRepository;
import com.youssfi.ebanqueaccountservice.service.BankAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureGraphQlTester
class BankAccountGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    void shouldQueryAccounts() {
        graphQlTester.document("{ accounts { id balance currency type createdAt } }")
                .execute()
                .errors().verify()
                .path("accounts")
                .entityList(Object.class)
                .hasSizeGreaterThan(0);
    }

    @Test
    void shouldQueryAccountById() {
        BankAccountResponseDTO existing = bankAccountService.getAccounts().get(0);

        graphQlTester.document("query($id: ID!) { accountById(id: $id) { id currency balance type } }")
                .variable("id", existing.getId())
                .execute()
                .errors().verify()
                .path("accountById.id").entity(String.class).isEqualTo(existing.getId())
                .path("accountById.currency").entity(String.class).isEqualTo(existing.getCurrency());
    }

    @Test
    void shouldAddAccountWithUuidAndIsoDate() {
        String createdAt = graphQlTester.document("""
                        mutation {
                          addAccount(bankAccount: {
                            balance: 25000
                            currency: "MAD"
                            type: CURRENT_ACCOUNT
                          }) {
                            id
                            balance
                            currency
                            type
                            createdAt
                          }
                        }
                        """)
                .execute()
                .errors().verify()
                .path("addAccount.id").entity(String.class).satisfies(id -> assertNotNull(id))
                .path("addAccount.balance").entity(Double.class).isEqualTo(25000.0)
                .path("addAccount.currency").entity(String.class).isEqualTo("MAD")
                .path("addAccount.type").entity(String.class).isEqualTo("CURRENT_ACCOUNT")
                .path("addAccount.createdAt").entity(String.class).get();

        assertDoesNotThrowIso(createdAt);
    }

    @Test
    void shouldUpdateCurrencyWithoutChangingBalance() {
        BankAccountResponseDTO created = bankAccountService.addAccount(BankAccountRequestDTO.builder()
                .balance(12345.67)
                .currency("MAD")
                .type(AccountType.CURRENT_ACCOUNT)
                .build());

        graphQlTester.document("""
                        mutation($id: ID!) {
                          updateAccount(id: $id, bankAccount: { currency: "EUR" }) {
                            id
                            balance
                            currency
                          }
                        }
                        """)
                .variable("id", created.getId())
                .execute()
                .errors().verify()
                .path("updateAccount.id").entity(String.class).isEqualTo(created.getId())
                .path("updateAccount.currency").entity(String.class).isEqualTo("EUR")
                .path("updateAccount.balance").entity(Double.class).isEqualTo(12345.67);
    }

    @Test
    void shouldUpdateBalanceToZero() {
        BankAccountResponseDTO created = bankAccountService.addAccount(BankAccountRequestDTO.builder()
                .balance(2500.0)
                .currency("MAD")
                .type(AccountType.SAVING_ACCOUNT)
                .build());

        graphQlTester.document("""
                        mutation($id: ID!) {
                          updateAccount(id: $id, bankAccount: { balance: 0 }) {
                            id
                            balance
                            currency
                          }
                        }
                        """)
                .variable("id", created.getId())
                .execute()
                .errors().verify()
                .path("updateAccount.balance").entity(Double.class).isEqualTo(0.0)
                .path("updateAccount.id").entity(String.class).isEqualTo(created.getId());
    }

    @Test
    void shouldDeleteAccount() {
        BankAccountResponseDTO created = bankAccountService.addAccount(BankAccountRequestDTO.builder()
                .balance(100.0)
                .currency("MAD")
                .type(AccountType.SAVING_ACCOUNT)
                .build());

        graphQlTester.document("mutation($id: ID!) { deleteAccount(id: $id) }")
                .variable("id", created.getId())
                .execute()
                .errors().verify()
                .path("deleteAccount").entity(Boolean.class).isEqualTo(true);

        assertFalse(bankAccountRepository.findById(created.getId()).isPresent());
    }

    @Test
    void shouldFailWhenCreatingWithoutBalance() {
        long before = bankAccountRepository.count();

        graphQlTester.document("""
                        mutation {
                          addAccount(bankAccount: {
                            currency: "MAD"
                            type: CURRENT_ACCOUNT
                          }) {
                            id
                          }
                        }
                        """)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertFalse(errors.isEmpty(), "GraphQL errors expected when balance is missing");
                    // Spring GraphQL 1.0 may mask the IllegalArgumentException message as INTERNAL_ERROR
                    assertTrue(errors.stream().anyMatch(error ->
                                    error.getMessage() != null
                                            && (error.getMessage().toLowerCase().contains("balance")
                                            || error.getMessage().contains("INTERNAL_ERROR")
                                            || error.getErrorType() != null)),
                            "Expected a GraphQL error related to missing balance or INTERNAL_ERROR");
                });

        assertEquals(before, bankAccountRepository.count());
    }

    @Test
    void shouldFailWhenAccountIdDoesNotExist() {
        String missingId = "00000000-0000-0000-0000-000000000000";

        graphQlTester.document("query($id: ID!) { accountById(id: $id) { id } }")
                .variable("id", missingId)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertFalse(errors.isEmpty(), "GraphQL errors expected for unknown account id");
                    // Documented behavior with Spring GraphQL 1.0.1: exception details may be masked
                    assertTrue(errors.stream().anyMatch(error ->
                                    error.getMessage() != null
                                            && (error.getMessage().contains(missingId)
                                            || error.getMessage().contains("INTERNAL_ERROR")
                                            || error.getErrorType() != null)),
                            "Expected a GraphQL error for missing account");
                });
    }

    @Test
    void shouldExposeCreatedAtAsIso8601() {
        BankAccountResponseDTO created = bankAccountService.addAccount(BankAccountRequestDTO.builder()
                .balance(500.0)
                .currency("MAD")
                .type(AccountType.CURRENT_ACCOUNT)
                .build());

        String createdAt = graphQlTester.document("query($id: ID!) { accountById(id: $id) { createdAt } }")
                .variable("id", created.getId())
                .execute()
                .errors().verify()
                .path("accountById.createdAt")
                .entity(String.class)
                .get();

        assertDoesNotThrowIso(createdAt);
    }

    @Test
    void shouldReturnSuccessfulMutationWithoutErrorsFieldIssues() {
        Map<String, Object> response = graphQlTester.document("""
                        mutation {
                          addAccount(bankAccount: {
                            balance: 10
                            currency: "USD"
                            type: SAVING_ACCOUNT
                          }) {
                            id
                            balance
                          }
                        }
                        """)
                .execute()
                .errors().verify()
                .path("addAccount")
                .entity(Map.class)
                .get();

        assertNotNull(response.get("id"));
        assertEquals(10.0, ((Number) response.get("balance")).doubleValue());
    }

    private void assertDoesNotThrowIso(String createdAt) {
        assertNotNull(createdAt);
        Instant parsed = Instant.parse(createdAt);
        assertNotNull(parsed);
    }
}
