package org.finsage.api.controllers;

import lombok.RequiredArgsConstructor;
import org.finsage.api.entities.InvestmentType;
import org.finsage.api.models.InvestmentDTO;
import org.finsage.api.models.InvestmentSummaryDTO;
import org.finsage.api.services.InvestmentService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    private final static String INVESTMENT_PATH = "/api/v1/users/{userId}/investments";
    private final static String INVESTMENT_ID_PATH = INVESTMENT_PATH + "/{investmentId}";

    @PostMapping(INVESTMENT_PATH)
    public ResponseEntity<InvestmentDTO> createInvestment(
            @PathVariable UUID userId,
            @RequestBody InvestmentDTO investmentDTO) {
        InvestmentDTO saved = investmentService.addInvestment(userId, investmentDTO);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping(INVESTMENT_ID_PATH)
    public ResponseEntity<InvestmentDTO> getInvestmentById(
            @PathVariable UUID userId,
            @PathVariable UUID investmentId) {
        return investmentService.getInvestmentById(userId, investmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(INVESTMENT_PATH)
    public ResponseEntity<Page<InvestmentDTO>> getAllInvestments(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<InvestmentDTO> result = investmentService.getAllInvestments(userId, page, size);
        return ResponseEntity.ok(result);
    }

    @PutMapping(INVESTMENT_ID_PATH)
    public ResponseEntity<InvestmentDTO> updateInvestment(
            @PathVariable UUID userId,
            @PathVariable UUID investmentId,
            @RequestBody InvestmentDTO investmentDTO) {
        return investmentService.updateInvestment(userId, investmentId, investmentDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping(INVESTMENT_ID_PATH)
    public ResponseEntity<Void> deleteInvestment(
            @PathVariable UUID userId,
            @PathVariable UUID investmentId) {
        investmentService.deleteInvestment(userId, investmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(INVESTMENT_PATH + "/type/{type}")
    public ResponseEntity<List<InvestmentDTO>> getInvestmentsByType(
            @PathVariable UUID userId,
            @PathVariable InvestmentType type) {
        return ResponseEntity.ok(investmentService.getInvestmentsByType(userId, type));
    }

    @GetMapping(INVESTMENT_PATH + "/summary")
    public ResponseEntity<InvestmentSummaryDTO> getInvestmentSummary(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(investmentService.getInvestmentSummary(userId));
    }
}