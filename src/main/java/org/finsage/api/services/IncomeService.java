package org.finsage.api.services;

import org.finsage.api.models.IncomeDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface IncomeService {
    IncomeDTO createIncome(UUID userId, IncomeDTO income);
    Optional<IncomeDTO> getIncomeById(UUID userId, UUID incomeId);
    Page<IncomeDTO> getAllIncomes(UUID userId, Integer pageNumber, Integer pageSize);
    Optional<IncomeDTO> updateIncome(UUID userId, UUID incomeId, IncomeDTO income);
    void deleteIncome(UUID userId, UUID incomeId);
    Optional<IncomeDTO> getIncomeByYear(UUID userId, int year);
}
