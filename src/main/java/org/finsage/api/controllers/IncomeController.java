package org.finsage.api.controllers;

import lombok.RequiredArgsConstructor;
import org.finsage.api.components.JwtUtil;
import org.finsage.api.models.IncomeDTO;
import org.finsage.api.services.IncomeService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<IncomeDTO> createIncome(@RequestBody IncomeDTO incomeDTO) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        IncomeDTO created = incomeService.createIncome(userId, incomeDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{incomeId}")
    public ResponseEntity<IncomeDTO> getIncomeById(@PathVariable UUID incomeId) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        return incomeService.getIncomeById(userId, incomeId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<IncomeDTO>> getAllIncomes(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        Page<IncomeDTO> incomePage = incomeService.getAllIncomes(userId, page, size);
        return ResponseEntity.ok(incomePage);
    }

    @PutMapping("/{incomeId}")
    public ResponseEntity<IncomeDTO> updateIncome(
            @PathVariable UUID incomeId,
            @RequestBody IncomeDTO incomeDTO) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        return incomeService.updateIncome(userId, incomeId, incomeDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{incomeId}")
    public ResponseEntity<Void> deleteIncome(@PathVariable UUID incomeId) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        try {
            incomeService.deleteIncome(userId, incomeId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<IncomeDTO> getIncomeByYear(@PathVariable int year) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        return incomeService.getIncomeByYear(userId, year)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

