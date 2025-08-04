package org.finsage.api.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.finsage.api.components.JwtUtil;
import org.finsage.api.models.ExpenseDTO;
import org.finsage.api.services.ExpenseService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(@RequestBody ExpenseDTO expense) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        ExpenseDTO created = expenseService.createExpense(userId, expense);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping({"/{expenseId}"})
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable UUID expenseId) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        return expenseService.getExpenseById(userId, expenseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<ExpenseDTO>> getAllExpenses(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        Page<ExpenseDTO> expenses = expenseService.getAllExpenses(userId, page, size);
        return ResponseEntity.ok(expenses);
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseDTO> updateExpense(
            @PathVariable UUID expenseId,
            @RequestBody @Valid ExpenseDTO expense) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        return expenseService.updateExpense(userId, expenseId, expense)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID expenseId) throws Exception {
        UUID userId = jwtUtil.getUserIdFromToken(SecurityContextHolder.getContext().getAuthentication());
        expenseService.deleteExpense(userId, expenseId);
        return ResponseEntity.noContent().build();
    }
}

