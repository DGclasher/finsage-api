package org.finsage.api.controllers;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.finsage.api.entities.Expense;
import org.finsage.api.entities.AppUser;
import org.junit.jupiter.api.BeforeEach;
import jakarta.transaction.Transactional;
import org.finsage.api.models.ExpenseDTO;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.finsage.api.repositories.AppUserRepository;
import org.finsage.api.repositories.ExpenseRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    private UUID userId;
    private UUID expenseId;
    private ExpenseDTO expenseDTO;
    private String jwtToken;

    private final String EXPENSE_PATH = "/api/v1/expenses";
    private final String EXPENSE_ID_PATH = EXPENSE_PATH + "/{expenseId}";

    @BeforeEach
    void setUp() throws Exception {
        jwtToken = getJwtToken("john.doe@example.com", "password");

        AppUser user = userRepository.findByEmail("john.doe@example.com")
                .orElseThrow(() -> new IllegalStateException("Sample user not found"));
        userId = user.getId();

        Expense existingExpense = expenseRepository.findAll().stream()
                .filter(e -> e.getAppUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No expense found for test user"));
        expenseId = existingExpense.getId();

        expenseDTO = ExpenseDTO.builder()
                .amount(1500.0)
                .category("Food")
                .build();
    }

    private String getJwtToken(String email, String password) throws Exception {
        var loginRequest = Map.of("email", email, "password", password);

        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("token").asText();
    }

    private RequestPostProcessor bearerToken() {
        return request -> {
            request.addHeader("Authorization", "Bearer " + jwtToken);
            return request;
        };
    }

    @Test
    void testCreateExpense() throws Exception {
        mockMvc.perform(post(EXPENSE_PATH)
                        .with(bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.amount").value(1500.0));
    }

    @Test
    void testGetExpenseById() throws Exception {
        mockMvc.perform(get(EXPENSE_ID_PATH, expenseId)
                        .with(bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId.toString()));
    }

    @Test
    void testGetAllExpenses() throws Exception {
        mockMvc.perform(get(EXPENSE_PATH)
                        .with(bearerToken())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testUpdateExpense() throws Exception {
        ExpenseDTO updatedDTO = ExpenseDTO.builder()
                .category("Updated Category")
                .amount(2000.0)
                .build();

        mockMvc.perform(put(EXPENSE_ID_PATH, expenseId)
                        .with(bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Updated Category"))
                .andExpect(jsonPath("$.amount").value(2000.0));
    }

    @Test
    @Rollback
    @Transactional
    void testDeleteExpense() throws Exception {
        mockMvc.perform(delete(EXPENSE_ID_PATH, expenseId)
                        .with(bearerToken()))
                .andExpect(status().isNoContent());
    }
}