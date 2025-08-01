package org.finsage.api.services;

import org.finsage.api.entities.InvestmentType;
import org.finsage.api.models.InvestmentDTO;
import org.finsage.api.models.InvestmentSummaryDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentService {
    InvestmentDTO addInvestment(UUID userId, InvestmentDTO investment);
    Optional<InvestmentDTO> getInvestmentById(UUID userId, UUID investmentId);
    Page<InvestmentDTO> getAllInvestments(UUID userId, Integer pageNumber, Integer pageSize);
    Optional<InvestmentDTO> updateInvestment(UUID userId, UUID investmentId, InvestmentDTO investment);
    void deleteInvestment(UUID userId, UUID investmentId);
    List<InvestmentDTO> getInvestmentsByType(UUID userId, InvestmentType type);
    InvestmentSummaryDTO getInvestmentSummary(UUID userId);
}
