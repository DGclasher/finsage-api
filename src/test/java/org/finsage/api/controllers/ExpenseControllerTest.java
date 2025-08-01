package org.finsage.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.finsage.api.models.ExpenseDTO;
import org.finsage.api.services.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(controllers = ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID userId;
    private UUID expenseId;
    private ExpenseDTO expenseDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        expenseId = UUID.randomUUID();
        expenseDTO = ExpenseDTO.builder()
                .id(expenseId)
                .amount(1500.0)
                .category("Food")
                .build();
    }

    @Test
    void testCreateExpense() throws Exception {
        Mockito.when(expenseService.createExpense(eq(userId), any(ExpenseDTO.class)))
                .thenReturn(expenseDTO);

        mockMvc.perform(post("/api/v1/users/{userId}/expenses", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expenseId.toString()))
                .andExpect(jsonPath("$.amount").value(1500.0))
                .andExpect(jsonPath("$.category").value("Food"));
    }

    @Test
    void testGetExpenseById_found() throws Exception {
        Mockito.when(expenseService.getExpenseById(userId, expenseId))
                .thenReturn(Optional.of(expenseDTO));

        mockMvc.perform(get("/api/v1/users/{userId}/expenses/{expenseId}", userId, expenseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId.toString()))
                .andExpect(jsonPath("$.amount").value(1500.0));
    }

    @Test
    void testGetExpenseById_notFound() throws Exception {
        Mockito.when(expenseService.getExpenseById(userId, expenseId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/{userId}/expenses/{expenseId}", userId, expenseId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllExpenses() throws Exception {
        Page<ExpenseDTO> expensePage = new PageImpl<>(List.of(expenseDTO));
        Mockito.when(expenseService.getAllExpenses(userId, 0, 10))
                .thenReturn(expensePage);

        mockMvc.perform(get("/api/v1/users/{userId}/expenses", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(expenseId.toString()));
    }

    @Test
    void testUpdateExpense_found() throws Exception {
        Mockito.when(expenseService.updateExpense(eq(userId), eq(expenseId), any(ExpenseDTO.class)))
                .thenReturn(Optional.of(expenseDTO));

        mockMvc.perform(put("/api/v1/users/{userId}/expenses/{expenseId}", userId, expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId.toString()));
    }

    @Test
    void testUpdateExpense_notFound() throws Exception {
        Mockito.when(expenseService.updateExpense(eq(userId), eq(expenseId), any(ExpenseDTO.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/users/{userId}/expenses/{expenseId}", userId, expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteExpense() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{userId}/expenses/{expenseId}", userId, expenseId))
                .andExpect(status().isNoContent());
    }
}
