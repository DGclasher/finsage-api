package org.finsage.api.controllers;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.finsage.api.entities.Income;
import org.junit.jupiter.api.BeforeEach;
import org.finsage.api.models.IncomeDTO;
import org.finsage.api.entities.AppUser;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.finsage.api.repositories.IncomeRepository;
import org.springframework.test.web.servlet.MvcResult;
import org.finsage.api.repositories.AppUserRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class IncomeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    private UUID userId;
    private UUID incomeId;
    private String jwtToken;

    private final String INCOME_PATH = "/api/v1/incomes";
    private final String INCOME_ID_PATH = INCOME_PATH + "/{incomeId}";
    private final String INCOME_YEAR_PATH = INCOME_PATH + "/year/{year}";

    @BeforeEach
    void setUp() throws Exception {
        jwtToken = getJwtToken("john.doe@example.com", "password");

        AppUser user = userRepository.findByEmail("john.doe@example.com")
                .orElseThrow(() -> new IllegalStateException("Sample user not found"));
        userId = user.getId();

        Income income = incomeRepository.findAll().stream()
                .filter(i -> i.getAppUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No income found for user"));
        incomeId = income.getId();
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
    void testCreateIncome() throws Exception {
        IncomeDTO dto = IncomeDTO.builder()
                .annualPostTaxIncome(800000.0)
                .incomeYear(2026)
                .build();

        mockMvc.perform(post(INCOME_PATH)
                        .with(bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.annualPostTaxIncome").value(800000.0))
                .andExpect(jsonPath("$.incomeYear").value(2026));
    }

    @Test
    void testGetIncomeById() throws Exception {
        mockMvc.perform(get(INCOME_ID_PATH, incomeId)
                        .with(bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(incomeId.toString()));
    }

    @Test
    void testGetAllIncomes() throws Exception {
        mockMvc.perform(get(INCOME_PATH)
                        .with(bearerToken())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testUpdateIncome() throws Exception {
        IncomeDTO updated = IncomeDTO.builder()
                .annualPostTaxIncome(950000.0)
                .incomeYear(2025)
                .build();

        mockMvc.perform(put(INCOME_ID_PATH, incomeId)
                        .with(bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annualPostTaxIncome").value(950000.0));
    }

    @Test
    void testDeleteIncome() throws Exception {
        mockMvc.perform(delete(INCOME_ID_PATH, incomeId)
                        .with(bearerToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetIncomeByYear() throws Exception {
        mockMvc.perform(get(INCOME_YEAR_PATH, 2025)
                        .with(bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incomeYear").value(2025));
    }
}

