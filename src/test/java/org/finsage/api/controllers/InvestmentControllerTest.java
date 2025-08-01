package org.finsage.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.finsage.api.entities.InvestmentType;
import org.finsage.api.models.InvestmentDTO;
import org.finsage.api.models.InvestmentSummaryDTO;
import org.finsage.api.services.InvestmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(InvestmentController.class)
class InvestmentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    InvestmentService investmentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final static String INVESTMENT_PATH = "/api/v1/users/{userId}/investments";
    private final static String INVESTMENT_ID_PATH = INVESTMENT_PATH + "/{investmentId}";

    UUID userId;
    UUID investmentId;
    InvestmentDTO sampleInvestment;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        investmentId = UUID.randomUUID();
        sampleInvestment = InvestmentDTO.builder()
                .id(investmentId)
                .type(InvestmentType.STOCK)
                .symbol("AAPL")
                .units(10.0)
                .buyPrice(100.0)
                .currentPrice(120.0)
                .totalAmountInvested(1000.0)
                .startDate(LocalDate.now().minusMonths(6))
                .endDate(null)
                .interestRate(null)
                .build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testGetInvestmentById() throws Exception {
        when(investmentService.getInvestmentById(userId, investmentId))
                .thenReturn(Optional.of(sampleInvestment));
        mockMvc.perform(get(INVESTMENT_ID_PATH, userId, investmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(investmentId.toString()))
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.type").value("STOCK"));
    }

    @Test
    void testInvestmentIdNotFound() throws Exception {
        when(investmentService.getInvestmentById(userId, investmentId))
                .thenReturn(Optional.empty());
        mockMvc.perform(get(INVESTMENT_ID_PATH, userId, investmentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllInvestments() throws Exception {
        Page<InvestmentDTO> page = new PageImpl<>(List.of(sampleInvestment));
        when(investmentService.getAllInvestments(userId, 0, 10))
                .thenReturn(page);
        mockMvc.perform(get(INVESTMENT_PATH, userId)
                .param("pageNumber", "0")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(investmentId.toString()));

    }

    @Test
    void testCreateInvestment() throws Exception {
        when(investmentService.addInvestment(eq(userId), any(InvestmentDTO.class)))
                .thenReturn(sampleInvestment);
        mockMvc.perform(post(INVESTMENT_PATH, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleInvestment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("AAPL"));
    }

    @Test
    void testUpdateInvestment() throws  Exception {
        when(investmentService.updateInvestment(eq(userId), eq(investmentId), any(InvestmentDTO.class)))
                .thenReturn(Optional.of(sampleInvestment));

        mockMvc.perform(put(INVESTMENT_ID_PATH, userId, investmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleInvestment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(investmentId.toString()))
                .andExpect(jsonPath("$.symbol").value("AAPL"));
    }

    @Test
    void testUpdateInvestmentIdNotFound() throws Exception {
        when(investmentService.updateInvestment(eq(userId), eq(investmentId), any(InvestmentDTO.class)))
                .thenReturn(Optional.empty());
        mockMvc.perform(put(INVESTMENT_ID_PATH, userId, investmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleInvestment)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteInvestment() throws Exception {
        doNothing().when(investmentService).deleteInvestment(eq(userId), eq(investmentId));
        mockMvc.perform(delete(INVESTMENT_ID_PATH, userId, investmentId))
                .andExpect(status().isNoContent());

    }

    @Test
    void testGetInvestmentBytype() throws Exception {
        when(investmentService.getInvestmentsByType(userId, InvestmentType.STOCK))
                .thenReturn(List.of(sampleInvestment));
        mockMvc.perform(get(INVESTMENT_PATH + "/type/{type}", userId, "STOCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"));
    }

    @Test
    void testGetInvestmentSummary() throws Exception {
        InvestmentSummaryDTO summaryDTO = InvestmentSummaryDTO.builder()
                .totalInvested(1000.0)
                .currentValue(1200.0)
                .gainLossPercentage(20.0)
                .build();
        when(investmentService.getInvestmentSummary(userId)).thenReturn(summaryDTO);
        mockMvc.perform(get(INVESTMENT_PATH + "/summary", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInvested", is(1000.0)))
                .andExpect(jsonPath("$.currentValue", is(1200.0)))
                .andExpect(jsonPath("$.gainLossPercentage", is(20.0)));
    }
}