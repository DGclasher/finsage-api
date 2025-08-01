package org.finsage.api.controllers;

import lombok.RequiredArgsConstructor;
import org.finsage.api.models.IncomeDTO;
import org.finsage.api.services.IncomeService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    private static final String INCOME_PATH = "/api/v1/users/{userId}/incomes";
    private static final String INCOME_ID_PATH = INCOME_PATH + "/{incomeId}";

    @PostMapping(INCOME_PATH)
    public ResponseEntity<IncomeDTO> createIncome(@PathVariable UUID userId, @RequestBody IncomeDTO incomeDTO) {
        IncomeDTO created = incomeService.createIncome(userId, incomeDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping(INCOME_ID_PATH)
    public ResponseEntity<IncomeDTO> getIncomeById(@PathVariable UUID userId, @PathVariable UUID incomeId) {
        return incomeService.getIncomeById(userId, incomeId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(INCOME_PATH)
    public ResponseEntity<Page<IncomeDTO>> getAllIncomes(
            @PathVariable UUID userId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        Page<IncomeDTO> incomePage = incomeService.getAllIncomes(userId, page, size);
        return ResponseEntity.ok(incomePage);
    }

    @PutMapping(INCOME_ID_PATH)
    public ResponseEntity<IncomeDTO> updateIncome(
            @PathVariable UUID userId,
            @PathVariable UUID incomeId,
            @RequestBody IncomeDTO incomeDTO) {
        return incomeService.updateIncome(userId, incomeId, incomeDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(INCOME_ID_PATH)
    public ResponseEntity<Void> deleteIncome(@PathVariable UUID userId, @PathVariable UUID incomeId) {
        try {
            incomeService.deleteIncome(userId, incomeId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(INCOME_PATH + "/year/{year}")
    public ResponseEntity<IncomeDTO> getIncomeByYear(@PathVariable UUID userId, @PathVariable int year) {
        return incomeService.getIncomeByYear(userId, year)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

