package org.finsage.api.bootstrap;

import lombok.RequiredArgsConstructor;
import org.finsage.api.entities.*;
import org.finsage.api.repositories.AppUserRepository;
import org.finsage.api.repositories.ExpenseRepository;
import org.finsage.api.repositories.IncomeRepository;
import org.finsage.api.repositories.InvestmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BootStrapData implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final InvestmentRepository investmentRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create sample user
        AppUser user;
        if(appUserRepository.count() == 0) {
            user = AppUser.builder()
                    .name("John Doe")
                    .email("john.doe@example.com")
                    .provider(AuthProvider.LOCAL)
                    .passwordHash("$2a$10$aNFIfXkyjs0WEo8LYp9DSuMQGKlm0PbAAu698Ldv9Sc7CVjaSHFCe")
                    .build();
            appUserRepository.save(user);
        } else {
            user = appUserRepository.findByEmail("john.doe@example.com").get();
        }

        System.out.println("Sample user: " + user.getName() + " ID: " + user.getId());
        // Create income
        if(incomeRepository.count() == 0) {
            Income income = Income.builder()
                    .appUser(user)
                    .annualPostTaxIncome(750000.0)
                    .incomeYear(2025)
                    .build();

            incomeRepository.save(income);
            System.out.println("Sample income generated");
        }

        // Create expenses
        if(expenseRepository.count() == 0) {
            List<Expense> expenses = List.of(
                    Expense.builder()
                            .appUser(user)
                            .category("Groceries")
                            .amount(4000.0)
                            .build(),
                    Expense.builder()
                            .appUser(user)
                            .category("Rent")
                            .amount(12000.0)
                            .build(),
                    Expense.builder()
                            .appUser(user)
                            .category("Internet")
                            .amount(999.0)
                            .build()
            );

            expenseRepository.saveAll(expenses);
            System.out.println("Sample expenses generated");
        }

        // Create investments
        if(investmentRepository.count() == 0) {
            List<Investment> investments = List.of(
                    Investment.builder()
                            .appUser(user)
                            .type(InvestmentType.STOCK)
                            .symbol("HDFCBANK")
                            .units(10.0)
                            .buyPrice(150.0)
                            .totalAmountInvested(1500.0)
                            .build(),

                    Investment.builder()
                            .appUser(user)
                            .type(InvestmentType.MUTUAL_FUND)
                            .symbol("XYZ-MF")
                            .units(50.0)
                            .buyPrice(100.0)
                            .interestRate(22.1)
                            .currentPrice(95.0)
                            .startDate(LocalDate.of(2025, 3, 1))
                            .totalAmountInvested(5000.0)
                            .build(),

                    Investment.builder()
                            .appUser(user)
                            .type(InvestmentType.FD)
                            .totalAmountInvested(20000.0)
                            .startDate(LocalDate.of(2024, 1, 1))
                            .endDate(LocalDate.of(2027, 1, 1))
                            .interestRate(7.5)
                            .build()
            );

            investmentRepository.saveAll(investments);
            System.out.println("Sample investments generated");
        }
    }
}
