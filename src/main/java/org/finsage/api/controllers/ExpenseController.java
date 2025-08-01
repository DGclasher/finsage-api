package org.finsage.api.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.finsage.api.models.ExpenseDTO;
import org.finsage.api.services.ExpenseService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    private static final String EXPENSE_PATH = "/api/v1/users/{userId}/expenses";
    private static final String EXPENSE_ID_PATH = EXPENSE_PATH + "/{expenseId}";

    @PostMapping(EXPENSE_PATH)
    public ResponseEntity<ExpenseDTO> createExpense(
            @PathVariable UUID userId,
            @RequestBody @Valid ExpenseDTO expense) {

        ExpenseDTO created = expenseService.createExpense(userId, expense);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping(EXPENSE_ID_PATH)
    public ResponseEntity<ExpenseDTO> getExpenseById(
            @PathVariable UUID userId,
            @PathVariable UUID expenseId) {

        return expenseService.getExpenseById(userId, expenseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(EXPENSE_PATH)
    public ResponseEntity<Page<ExpenseDTO>> getAllExpenses(
            @PathVariable UUID userId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {

        Page<ExpenseDTO> expenses = expenseService.getAllExpenses(userId, page, size);
        return ResponseEntity.ok(expenses);
    }

    @PutMapping(EXPENSE_ID_PATH)
    public ResponseEntity<ExpenseDTO> updateExpense(
            @PathVariable UUID userId,
            @PathVariable UUID expenseId,
            @RequestBody @Valid ExpenseDTO expense) {

        return expenseService.updateExpense(userId, expenseId, expense)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping(EXPENSE_ID_PATH)
    public ResponseEntity<Void> deleteExpense(
            @PathVariable UUID userId,
            @PathVariable UUID expenseId) {

        expenseService.deleteExpense(userId, expenseId);
        return ResponseEntity.noContent().build();
    }
}

