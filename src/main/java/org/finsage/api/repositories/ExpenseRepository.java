package org.finsage.api.repositories;

import org.finsage.api.entities.AppUser;
import org.finsage.api.entities.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    Page<Expense> findByAppUser(AppUser appUser, Pageable pageable);
    Optional<Expense> findByIdAndAppUser(UUID id, AppUser appUser);
    void deleteByIdAndAppUser(UUID id, AppUser appUser);
}
