package org.finsage.api.repositories;

import org.finsage.api.entities.AppUser;
import org.finsage.api.entities.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IncomeRepository extends JpaRepository<Income, UUID> {
    Page<Income> findByAppUser(AppUser appUser, Pageable pageable);
    Optional<Income> findByIdAndAppUser(UUID id, AppUser appUser);
    Income findByAppUserAndIncomeYear(AppUser appUser, int year);
    void deleteByAppUserAndId(AppUser appUser, UUID id);
}
