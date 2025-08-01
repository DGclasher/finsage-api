package org.finsage.api.services;

import lombok.RequiredArgsConstructor;
import org.finsage.api.entities.AppUser;
import org.finsage.api.entities.Expense;
import org.finsage.api.mappers.ExpenseMapper;
import org.finsage.api.models.ExpenseDTO;
import org.finsage.api.repositories.AppUserRepository;
import org.finsage.api.repositories.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class ExpenseServiceJPA implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final AppUserRepository appUserRepository;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    public ExpenseDTO createExpense(UUID userId, ExpenseDTO expense) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Expense entity = expenseMapper.expenseDtoToExpense(expense);
        entity.setAppUser(user);
        Expense saved = expenseRepository.save(entity);
        return expenseMapper.expenseToExpenseDto(saved);
    }

    @Override
    public Optional<ExpenseDTO> getExpenseById(UUID userId, UUID expenseId) {
        return Optional.ofNullable(expenseMapper.expenseToExpenseDto(expenseRepository.findById(expenseId).orElse(null)));
    }

    public PageRequest buildPageRequest(Integer pageNumber, Integer pageSize) {
        if (pageNumber == null || pageNumber < 0) {
            pageNumber = DEFAULT_PAGE;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        return PageRequest.of(pageNumber, pageSize);
    }

    @Override
    public Page<ExpenseDTO> getAllExpenses(UUID userId, Integer pageNumber, Integer pageSize) {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<Expense> expensePage;
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

        expensePage = expenseRepository.findByAppUser(appUser, pageRequest);
        return expensePage.map(expenseMapper::expenseToExpenseDto);
    }

    @Override
    public Optional<ExpenseDTO> updateExpense(UUID userId, UUID expenseId, ExpenseDTO expense) {
        AtomicReference<Optional<ExpenseDTO>> atomicReference = new AtomicReference<>(Optional.empty());
        expenseRepository.findById(expenseId).ifPresent(foundExpense -> {
            foundExpense.setCategory(expense.getCategory());
            foundExpense.setAmount(expense.getAmount());
            Expense updated = expenseRepository.save(foundExpense);
            atomicReference.set(Optional.of(expenseMapper.expenseToExpenseDto(updated)));
        });
        return atomicReference.get();
    }


    @Override
    public void deleteExpense(UUID userId, UUID expenseId) {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(expenseRepository.findByIdAndAppUser(expenseId, appUser).isPresent()) {
            expenseRepository.deleteByIdAndAppUser(expenseId, appUser);
        } else {
            throw new RuntimeException("Expense not found for user");
        }
    }
}
