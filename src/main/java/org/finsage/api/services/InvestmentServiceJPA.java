package org.finsage.api.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.finsage.api.entities.AppUser;
import org.finsage.api.entities.Investment;
import org.finsage.api.entities.InvestmentType;
import org.finsage.api.mappers.InvestmentMapper;
import org.finsage.api.models.InvestmentDTO;
import org.finsage.api.models.InvestmentSummaryDTO;
import org.finsage.api.repositories.AppUserRepository;
import org.finsage.api.repositories.InvestmentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
        if (entity.getType() == InvestmentType.STOCK) {
            Double currentPrice = stockValuationClient.fetchCurrentPrice(entity.getSymbol());
            if (currentPrice == null) {
                throw new RuntimeException("Unable to fetch stock price for symbol: " + entity.getSymbol());
            }
            entity.setBuyPrice(currentPrice);
            entity.setCurrentPrice(currentPrice);
            entity.setCurrentValue(currentPrice * entity.getUnits());
            entity.setTotalAmountInvested(currentPrice * entity.getUnits());
        } else {
            entity.setCurrentPrice(0.0);
        }
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

    private void updateStockInvestment(Investment investment) {
        if (investment.getType() == InvestmentType.STOCK) {
            Double currentPrice = stockValuationClient.fetchCurrentPrice(investment.getSymbol());
            if (currentPrice != null) {
                investment.setCurrentPrice(currentPrice);
                investment.setCurrentValue(currentPrice * investment.getUnits());
            } else {
                throw new RuntimeException("Failed to fetch current price for stock: " + investment.getSymbol());
            }
        }
    }

    @Override
    public Page<InvestmentDTO> getAllInvestments(UUID userId, Integer pageNumber, Integer pageSize) {
        Page<Investment> investmentPage;
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);
        AppUser appUser = appUserRepository.findById(userId).orElse(null);
        investmentPage = investmentRepository.findInvestmentsByAppUser(appUser, pageRequest);
        for( Investment investment : investmentPage) {
            updateStockInvestment(investment);
        }
        return investmentPage.map(investmentMapper::investmentToInvestmentDto);
    }

    @Override
    public Optional<InvestmentDTO> updateInvestment(UUID userId, UUID investmentId, InvestmentDTO investment) {
        AtomicReference<Optional<InvestmentDTO>> atomicReference = new AtomicReference<>();

        investmentRepository.findById(investmentId).ifPresentOrElse(foundInvestment -> {
            foundInvestment.setType(investment.getType());
            foundInvestment.setStartDate(investment.getStartDate());
            foundInvestment.setEndDate(investment.getEndDate());
            foundInvestment.setSymbol(investment.getSymbol());
            Double prevUnits = foundInvestment.getUnits();
            foundInvestment.setUnits(investment.getUnits());
            foundInvestment.setInterestRate(investment.getInterestRate());

            if (investment.getType() == InvestmentType.STOCK) {
                Double currentPrice = stockValuationClient.fetchCurrentPrice(investment.getSymbol());
                if (currentPrice == null) {
                    throw new RuntimeException("Unable to fetch stock price for symbol: " + investment.getSymbol());
                }
                foundInvestment.setBuyPrice(currentPrice);
                foundInvestment.setCurrentPrice(currentPrice);
                foundInvestment.setTotalAmountInvested(investment.getTotalAmountInvested() + ((investment.getUnits()- prevUnits) * currentPrice));
                foundInvestment.setCurrentValue(currentPrice * investment.getUnits());
            } else {
                foundInvestment.setBuyPrice(investment.getBuyPrice());
                foundInvestment.setCurrentPrice(investment.getCurrentPrice());
                foundInvestment.setTotalAmountInvested(investment.getTotalAmountInvested());
            }

            Investment updated = investmentRepository.save(foundInvestment);
            atomicReference.set(Optional.of(investmentMapper.investmentToInvestmentDto(updated)));
        }, () -> atomicReference.set(Optional.empty()));

        return atomicReference.get();
    }


    @Override
    @Transactional
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
            updateStockInvestment(investment);
        }
        return investments.stream()
                .map(investmentMapper::investmentToInvestmentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "investmentSummary", key = "#userId")
    public InvestmentSummaryDTO getInvestmentSummary(UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Investment> investments = investmentRepository.findAllByAppUser(user);

        double totalInvested = 0;
        double totalCurrentValue = 0;

        for (Investment inv : investments) {
            double investedAmount = 0;
            double currentValue = 0;
            double units = 0.0;
            if (inv.getType() != InvestmentType.FD) {
                units = inv.getUnits() != null ? inv.getUnits() : 0.0;
            }

            switch (inv.getType()) {
                case FD:
                case BOND:
                    investedAmount = inv.getTotalAmountInvested();
                    currentValue = investedAmount + (investedAmount * inv.getInterestRate() / 100);
                    break;

                case STOCK:
                    investedAmount = inv.getBuyPrice() * units;
                    Double livePrice = stockValuationClient.fetchCurrentPrice(inv.getSymbol());
                    currentValue = (livePrice != null ? livePrice : 0.0) * units;
                    break;

                case MUTUAL_FUND:
                case ETF:
                    investedAmount = inv.getTotalAmountInvested(); // or buyPrice * units
                    long daysHeld = ChronoUnit.DAYS.between(inv.getStartDate(), LocalDate.now());
                    double interestEarned = investedAmount * (inv.getInterestRate() / 100) * (daysHeld / 365.0);
                    currentValue = investedAmount + interestEarned;
                    break;
            }

            totalInvested += investedAmount;
            totalCurrentValue += currentValue;
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
