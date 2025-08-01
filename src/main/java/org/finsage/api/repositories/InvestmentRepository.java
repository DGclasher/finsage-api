package org.finsage.api.repositories;

import org.finsage.api.entities.AppUser;
import org.finsage.api.entities.Investment;
import org.finsage.api.entities.InvestmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentRepository extends JpaRepository<Investment, UUID> {
    Page<Investment> findInvestmentsByAppUser(AppUser appUser, Pageable pageable);
    List<Investment> findByAppUserAndType(AppUser appUser, InvestmentType investmentType);
    List<Investment> findAllByAppUser(AppUser appUser);
    Optional<Investment> findByIdAndAppUser(UUID id, AppUser appUser);
    void deleteByIdAndAppUser(UUID id, AppUser appUser);
}
