package org.finsage.api.services;

import lombok.RequiredArgsConstructor;
import org.finsage.api.entities.AppUser;
import org.finsage.api.entities.Income;
import org.finsage.api.mappers.IncomeMapper;
import org.finsage.api.models.IncomeDTO;
import org.finsage.api.repositories.AppUserRepository;
import org.finsage.api.repositories.IncomeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class IncomeServiceJPA implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final IncomeMapper incomeMapper;
    private final AppUserRepository appUserRepository;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    public IncomeDTO createIncome(UUID userId, IncomeDTO incomeDTO) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Income income = incomeMapper.incomeDtoToIncome(incomeDTO);
        income.setAppUser(user);
        Income saved = incomeRepository.save(income);
        return incomeMapper.incomeToIncomeDto(saved);
    }

    @Override
    public Optional<IncomeDTO> getIncomeById(UUID userId, UUID incomeId) {
        return incomeRepository.findById(incomeId)
                .filter(income -> income.getAppUser().getId().equals(userId))
                .map(incomeMapper::incomeToIncomeDto);
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
    public Page<IncomeDTO> getAllIncomes(UUID userId, Integer pageNumber, Integer pageSize) {
        Page<IncomeDTO> incomePage;
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Page<Income> incomeEntities = incomeRepository.findByAppUser(appUser, pageRequest);
        incomePage = incomeEntities.map(incomeMapper::incomeToIncomeDto);
        return incomePage;
    }

    @Override
    public Optional<IncomeDTO> updateIncome(UUID userId, UUID incomeId, IncomeDTO income) {
        AtomicReference<Optional<IncomeDTO>> atomicReference = new AtomicReference<>();
        incomeRepository.findById(incomeId).ifPresentOrElse(existingIncome -> {
            if (existingIncome.getAppUser().getId().equals(userId)) {
                Income updatedIncome = incomeMapper.incomeDtoToIncome(income);
                updatedIncome.setId(existingIncome.getId());
                updatedIncome.setAppUser(existingIncome.getAppUser());
                Income saved = incomeRepository.save(updatedIncome);
                atomicReference.set(Optional.of(incomeMapper.incomeToIncomeDto(saved)));
            } else {
                atomicReference.set(Optional.empty());
            }
        }, () -> atomicReference.set(Optional.empty()));
        return atomicReference.get();
    }

    @Override
    public void deleteIncome(UUID userId, UUID incomeId) {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (incomeRepository.findByIdAndAppUser(incomeId, appUser).isPresent()) {
            incomeRepository.deleteByAppUserAndId(appUser, incomeId);
        } else {
            throw new RuntimeException("Income not found");
        }
    }

    @Override
    public Optional<IncomeDTO> getIncomeByYear(UUID userId, int year) {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Income income = incomeRepository.findByAppUserAndIncomeYear(appUser, year);
        return Optional.ofNullable(incomeMapper.incomeToIncomeDto(income));
    }
}
