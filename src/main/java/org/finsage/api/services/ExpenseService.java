package org.finsage.api.services;

import org.finsage.api.models.ExpenseDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseService {
    ExpenseDTO createExpense(UUID userId, ExpenseDTO expense);
    Optional<ExpenseDTO> getExpenseById(UUID userId, UUID expenseId);
    Page<ExpenseDTO> getAllExpenses(UUID userId, Integer pageNumber, Integer pageSize);
    Optional<ExpenseDTO> updateExpense(UUID userId, UUID expenseId, ExpenseDTO expense);
    void deleteExpense(UUID userId, UUID expenseId);
}
