package org.finsage.api.controllers;

import lombok.RequiredArgsConstructor;
import org.finsage.api.components.JwtUtil;
import org.finsage.api.entities.InvestmentType;
import org.finsage.api.models.InvestmentDTO;
import org.finsage.api.models.InvestmentSummaryDTO;
import org.finsage.api.services.InvestmentService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/investments")
public class InvestmentController {

    private final InvestmentService investmentService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<InvestmentDTO> createInvestment(@RequestBody InvestmentDTO investmentDTO) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        InvestmentDTO saved = investmentService.addInvestment(userId, investmentDTO);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{investmentId}")
    public ResponseEntity<InvestmentDTO> getInvestmentById(@PathVariable UUID investmentId) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        return investmentService.getInvestmentById(userId, investmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<InvestmentDTO>> getAllInvestments(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        Page<InvestmentDTO> result = investmentService.getAllInvestments(userId, page, size);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{investmentId}")
    public ResponseEntity<InvestmentDTO> updateInvestment(
            @PathVariable UUID investmentId,
            @RequestBody InvestmentDTO investmentDTO) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        return investmentService.updateInvestment(userId, investmentId, investmentDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{investmentId}")
    public ResponseEntity<Void> deleteInvestment(@PathVariable UUID investmentId) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        investmentService.deleteInvestment(userId, investmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<InvestmentDTO>> getInvestmentsByType(@PathVariable InvestmentType type) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok(investmentService.getInvestmentsByType(userId, type));
    }

    @GetMapping("/summary")
    public ResponseEntity<InvestmentSummaryDTO> getInvestmentSummary() throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok(investmentService.getInvestmentSummary(userId));
    }
}
