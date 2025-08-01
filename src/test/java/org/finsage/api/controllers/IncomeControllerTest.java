package org.finsage.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.finsage.api.models.IncomeDTO;
import org.finsage.api.services.IncomeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(IncomeController.class)
class IncomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncomeService incomeService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID incomeId;
    private IncomeDTO sampleIncome;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        incomeId = UUID.randomUUID();
        sampleIncome = IncomeDTO.builder()
                .id(incomeId)
                .annualPostTaxIncome(50000.00)
                .incomeYear(2024)
                .build();
    }

    @Test
    void testCreateIncome() throws Exception {
        given(incomeService.createIncome(eq(userId), any(IncomeDTO.class)))
                .willReturn(sampleIncome);

        mockMvc.perform(post("/api/v1/users/{userId}/incomes", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleIncome)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(incomeId.toString()))
                .andExpect(jsonPath("$.annualPostTaxIncome").value(50000.00));
    }

    @Test
    void testGetIncomeById_found() throws Exception {
        given(incomeService.getIncomeById(userId, incomeId)).willReturn(Optional.of(sampleIncome));

        mockMvc.perform(get("/api/v1/users/{userId}/incomes/{incomeId}", userId, incomeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(incomeId.toString()));
    }

    @Test
    void testGetIncomeById_notFound() throws Exception {
        given(incomeService.getIncomeById(userId, incomeId)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/{userId}/incomes/{incomeId}", userId, incomeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllIncomes() throws Exception {
        Page<IncomeDTO> page = new PageImpl<>(List.of(sampleIncome));
        given(incomeService.getAllIncomes(userId, 0, 10)).willReturn(page);

        mockMvc.perform(get("/api/v1/users/{userId}/incomes", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(incomeId.toString()));
    }

    @Test
    void testUpdateIncome_found() throws Exception {
        given(incomeService.updateIncome(eq(userId), eq(incomeId), any(IncomeDTO.class)))
                .willReturn(Optional.of(sampleIncome));

        mockMvc.perform(put("/api/v1/users/{userId}/incomes/{incomeId}", userId, incomeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleIncome)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(incomeId.toString()));
    }

    @Test
    void testUpdateIncome_notFound() throws Exception {
        given(incomeService.updateIncome(eq(userId), eq(incomeId), any(IncomeDTO.class)))
                .willReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/users/{userId}/incomes/{incomeId}", userId, incomeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleIncome)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteIncome_success() throws Exception {
        doNothing().when(incomeService).deleteIncome(userId, incomeId);

        mockMvc.perform(delete("/api/v1/users/{userId}/incomes/{incomeId}", userId, incomeId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteIncome_notFound() throws Exception {
        doThrow(new RuntimeException("Income not found"))
                .when(incomeService).deleteIncome(userId, incomeId);

        mockMvc.perform(delete("/api/v1/users/{userId}/incomes/{incomeId}", userId, incomeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetIncomeByYear_found() throws Exception {
        given(incomeService.getIncomeByYear(userId, 2024)).willReturn(Optional.of(sampleIncome));

        mockMvc.perform(get("/api/v1/users/{userId}/incomes/year/{year}", userId, 2024))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incomeYear").value(2024));
    }

    @Test
    void testGetIncomeByYear_notFound() throws Exception {
        given(incomeService.getIncomeByYear(userId, 2024)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/{userId}/incomes/year/{year}", userId, 2024))
                .andExpect(status().isNotFound());
    }
}
