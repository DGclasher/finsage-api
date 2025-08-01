package org.finsage.api.services;

import lombok.RequiredArgsConstructor;
import org.finsage.api.entities.AppUser;
import org.finsage.api.entities.Investment;
import org.finsage.api.entities.InvestmentType;
import org.finsage.api.mappers.InvestmentMapper;
import org.finsage.api.models.InvestmentDTO;
import org.finsage.api.models.InvestmentSummaryDTO;
import org.finsage.api.repositories.AppUserRepository;
import org.finsage.api.repositories.InvestmentRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class InvestmentServiceJPA implements InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final InvestmentMapper investmentMapper;
    private final AppUserRepository appUserRepository;
    private final StockValuationClient stockValuationClient;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    public InvestmentDTO addInvestment(UUID userId, InvestmentDTO investment) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Investment entity = investmentMapper.investmentDtoToInvestment(investment);
        entity.setAppUser(user);

        Investment saved = investmentRepository.save(entity);
        return investmentMapper.investmentToInvestmentDto(saved);
    }

    @Override
    public Optional<InvestmentDTO> getInvestmentById(UUID userId, UUID investmentId) {
        return Optional.ofNullable(investmentMapper.investmentToInvestmentDto(investmentRepository.findById(investmentId).orElse(null)));
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
    public Page<InvestmentDTO> getAllInvestments(UUID userId, Integer pageNumber, Integer pageSize) {
        Page<Investment> investmentPage;
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);
        AppUser appUser = appUserRepository.findById(userId).orElse(null);
        investmentPage = investmentRepository.findInvestmentsByAppUser(appUser, pageRequest);
        return investmentPage.map(investmentMapper::investmentToInvestmentDto);
    }

    @Override
    public Optional<InvestmentDTO> updateInvestment(UUID userId, UUID investmentId, InvestmentDTO investment) {
        AtomicReference<Optional<InvestmentDTO>> atomicReference = new AtomicReference<>();
        investmentRepository.findById(investmentId).ifPresentOrElse(foundInvestment -> {
            foundInvestment.setTotalAmountInvested(investment.getTotalAmountInvested());
            foundInvestment.setBuyPrice(investment.getBuyPrice());
            foundInvestment.setType(investment.getType());
            foundInvestment.setCurrentPrice(investment.getCurrentPrice());
            foundInvestment.setStartDate(investment.getStartDate());
            foundInvestment.setSymbol(investment.getSymbol());
            foundInvestment.setInterestRate(investment.getInterestRate());
            foundInvestment.setUnits(investment.getUnits());
            foundInvestment.setEndDate(investment.getEndDate());

            Investment updatedInvestment = investmentRepository.save(foundInvestment);
            atomicReference.set(Optional.of(investmentMapper.investmentToInvestmentDto(updatedInvestment)));
        }, () -> atomicReference.set(Optional.empty()));
        return atomicReference.get();
    }

    @Override
    public void deleteInvestment(UUID userId, UUID investmentId) {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (investmentRepository.findByIdAndAppUser(investmentId, appUser).isPresent()) {
            investmentRepository.deleteByIdAndAppUser(investmentId, appUser);
        } else {
            throw new RuntimeException("Investment not found for user");
        }
    }

    @Override
    public List<InvestmentDTO> getInvestmentsByType(UUID userId, InvestmentType type) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Investment> investments = investmentRepository.findByAppUserAndType(user, type);
        for(Investment investment : investments) {
            if(investment.getType() == InvestmentType.STOCK) {
                Double currentPrice = stockValuationClient.fetchCurrentPrice(investment.getSymbol());
                if (currentPrice != null) {
                    investment.setCurrentPrice(currentPrice * investment.getUnits());
                } else {
                    throw new RuntimeException("Failed to fetch current price for stock: " + investment.getSymbol());
                }
            }
        }
        return investments.stream()
                .map(investmentMapper::investmentToInvestmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public InvestmentSummaryDTO getInvestmentSummary(UUID userId) {

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Investment> investments = investmentRepository.findAllByAppUser(user);

        double totalInvested = 0;
        double totalCurrentValue = 0;

        for (Investment inv : investments) {
            if (inv.getType() == InvestmentType.FD || inv.getType() == InvestmentType.BOND) {
                totalInvested += inv.getTotalAmountInvested();
                totalCurrentValue += inv.getTotalAmountInvested() +
                        (inv.getTotalAmountInvested() * inv.getInterestRate() / 100);
            } else {
                double units = inv.getUnits();
                totalInvested += inv.getBuyPrice() * units;
                totalCurrentValue += inv.getCurrentPrice() * units;
            }
        }

        double gainLoss = totalCurrentValue - totalInvested;
        double gainLossPercentage = totalInvested == 0 ? 0 : (gainLoss / totalInvested) * 100;

        return InvestmentSummaryDTO.builder()
                .totalInvested(totalInvested)
                .currentValue(totalCurrentValue)
                .totalGainLoss(gainLoss)
                .gainLossPercentage(gainLossPercentage)
                .build();
    }
}
