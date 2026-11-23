package com.youssfi.ebanqueaccountservice.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldExposeOpenApiSpecWithCrudOperations() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths").value(hasKey("/bankAccounts")))
                .andExpect(jsonPath("$.paths").value(hasKey("/bankAccounts/{id}")))
                .andExpect(jsonPath("$.paths['/bankAccounts'].get").exists())
                .andExpect(jsonPath("$.paths['/bankAccounts'].post").exists())
                .andExpect(jsonPath("$.paths['/bankAccounts/{id}'].get").exists())
                .andExpect(jsonPath("$.paths['/bankAccounts/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/bankAccounts/{id}'].delete").exists());
    }
}
