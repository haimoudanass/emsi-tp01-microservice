package com.youssfi.ebanqueaccountservice.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youssfi.ebanqueaccountservice.entities.BankAccount;
import com.youssfi.ebanqueaccountservice.enums.AccountType;
import com.youssfi.ebanqueaccountservice.repositories.BankAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SpringDataRestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    void shouldExposeHalCollectionUnderDataRestBasePath() throws Exception {
        MvcResult result = mockMvc.perform(get("/data-rest/bankAccounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._embedded").exists())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode embedded = root.get("_embedded");
        assertTrue(embedded.fieldNames().hasNext(), "_embedded should contain resources");

        String collectionName = embedded.fieldNames().next();
        JsonNode accounts = embedded.get(collectionName);
        assertTrue(accounts.isArray() && accounts.size() > 0, "HAL collection should contain accounts");
        assertTrue(accounts.get(0).has("_links"), "HAL resource should expose _links");
        assertTrue(root.has("page") || root.path("_links").has("self"), "HAL response should expose pagination or self link");
    }

    @Test
    void shouldApplyProjectionP1() throws Exception {
        MvcResult result = mockMvc.perform(get("/data-rest/bankAccounts").param("projection", "p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode embedded = root.get("_embedded");
        String collectionName = embedded.fieldNames().next();
        JsonNode account = embedded.get(collectionName).get(0);

        assertTrue(account.has("id"), "projection p1 should expose id");
        assertTrue(account.has("type"), "projection p1 should expose type");
        assertTrue(account.has("balance"), "projection p1 should expose balance");
        assertFalse(account.has("currency"), "projection p1 should hide currency");
        assertFalse(account.has("createdAt"), "projection p1 should hide createdAt");
    }

    @Test
    void shouldSearchByTypeCurrentAndSaving() throws Exception {
        BankAccount current = bankAccountRepository.save(BankAccount.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(new Date())
                .balance(1111.0)
                .currency("MAD")
                .type(AccountType.CURRENT_ACCOUNT)
                .build());

        BankAccount saving = bankAccountRepository.save(BankAccount.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(new Date())
                .balance(2222.0)
                .currency("MAD")
                .type(AccountType.SAVING_ACCOUNT)
                .build());

        MvcResult currentResult = mockMvc.perform(get("/data-rest/bankAccounts/search/byType")
                        .param("t", "CURRENT_ACCOUNT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andReturn();

        assertSearchContainsOnlyType(currentResult.getResponse().getContentAsString(), AccountType.CURRENT_ACCOUNT, current.getId());

        MvcResult savingResult = mockMvc.perform(get("/data-rest/bankAccounts/search/byType")
                        .param("t", "SAVING_ACCOUNT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andReturn();

        assertSearchContainsOnlyType(savingResult.getResponse().getContentAsString(), AccountType.SAVING_ACCOUNT, saving.getId());
    }

    @Test
    void shouldKeepClassicRestAndOpenApiWorking() throws Exception {
        mockMvc.perform(get("/bankAccounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths").value(hasKey("/bankAccounts")))
                .andExpect(jsonPath("$.paths").value(hasKey("/bankAccounts/{id}")));
    }

    private void assertSearchContainsOnlyType(String json, AccountType expectedType, String expectedId) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode embedded = root.get("_embedded");
        String collectionName = embedded.fieldNames().next();
        JsonNode accounts = embedded.get(collectionName);

        boolean found = false;
        Iterator<JsonNode> iterator = accounts.elements();
        while (iterator.hasNext()) {
            JsonNode account = iterator.next();
            assertTrue(expectedType.name().equals(account.get("type").asText()),
                    "search result should only contain " + expectedType);

            String accountId = resolveAccountId(account);
            if (expectedId.equals(accountId)) {
                found = true;
            }
        }
        assertTrue(found, "search result should include seeded account " + expectedId);
    }

    private String resolveAccountId(JsonNode account) {
        if (account.hasNonNull("id")) {
            return account.get("id").asText();
        }
        JsonNode selfHref = account.path("_links").path("self").path("href");
        if (selfHref.isMissingNode() || selfHref.isNull()) {
            return null;
        }
        String href = selfHref.asText();
        return href.substring(href.lastIndexOf('/') + 1);
    }
}
