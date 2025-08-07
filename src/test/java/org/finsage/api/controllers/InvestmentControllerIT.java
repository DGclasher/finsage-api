package org.finsage.api.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.finsage.api.entities.AppUser;
import org.springframework.http.MediaType;
import org.finsage.api.entities.Investment;
import org.finsage.api.models.InvestmentDTO;
import com.fasterxml.jackson.databind.JsonNode;
import org.finsage.api.entities.InvestmentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.finsage.api.repositories.AppUserRepository;
import org.springframework.test.web.servlet.MvcResult;
import org.finsage.api.repositories.InvestmentRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class InvestmentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private InvestmentRepository investmentRepository;

    private UUID userId;
    private UUID investmentId;
    private String jwtToken;

    private final String BASE_PATH = "/api/v1/investments";
    private final String ID_PATH = BASE_PATH + "/{investmentId}";
    private final String TYPE_PATH = BASE_PATH + "/type/{type}";
    private final String SUMMARY_PATH = BASE_PATH + "/summary";

    @BeforeEach
    void setUp() throws Exception {
        jwtToken = getJwtToken("john.doe@example.com", "password");

        AppUser user = userRepository.findByEmail("john.doe@example.com")
                .orElseThrow(() -> new IllegalStateException("Sample user not found"));
        userId = user.getId();

        Investment investment = investmentRepository.findAll().stream()
                .filter(i -> i.getAppUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No investment found for user"));

        investmentId = investment.getId();
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
    void testCreateInvestment() throws Exception {
        InvestmentDTO dto = InvestmentDTO.builder()
                .type(InvestmentType.STOCK)
                .symbol("INFY")
                .units(20.0)
                .buyPrice(1600.0)
                .totalAmountInvested(32000.0)
                .build();

        mockMvc.perform(post(BASE_PATH)
                .with(bearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("INFY"))
                .andExpect(jsonPath("$.type").value("STOCK"));
    }

    @Test
    void testGetInvestmentById() throws Exception {
        mockMvc.perform(get(ID_PATH, investmentId)
                .with(bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(investmentId.toString()));
    }

    @Test
    void testGetAllInvestments() throws Exception {
        mockMvc.perform(get(BASE_PATH)
                .with(bearerToken())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testUpdateInvestment() throws Exception {
        InvestmentDTO update = InvestmentDTO.builder()
                .type(InvestmentType.MUTUAL_FUND)
                .symbol("NEW-MF")
                .units(100.0)
                .buyPrice(90.0)
                .totalAmountInvested(9000.0)
                .build();

        mockMvc.perform(put(ID_PATH, investmentId)
                .with(bearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("NEW-MF"))
                .andExpect(jsonPath("$.type").value("MUTUAL_FUND"));
    }

    @Test
    void testDeleteInvestment() throws Exception {
        mockMvc.perform(delete(ID_PATH, investmentId)
                .with(bearerToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetInvestmentsByType() throws Exception {
        mockMvc.perform(get(TYPE_PATH, InvestmentType.STOCK)
                .with(bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetInvestmentSummary() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(SUMMARY_PATH)
                .with(bearerToken()))
                .andExpect(status().isOk()).andReturn();
        String response = mvcResult.getResponse().getContentAsString();
    }
}
