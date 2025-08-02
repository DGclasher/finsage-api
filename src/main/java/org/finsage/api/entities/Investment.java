package org.finsage.api.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Investment {

    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, updatable = false, nullable = false)
    private UUID id;

    @Version
    private Integer version;

    @Enumerated(EnumType.STRING)
    private InvestmentType type;

    private String symbol;
    private Double units;
    private Double buyPrice;
    private Double currentPrice;

    private LocalDate startDate;
    private LocalDate endDate;

    private Double interestRate;
    private Double totalAmountInvested;
    private Double currentValue;

    @ManyToOne(fetch = FetchType.LAZY)
    private AppUser appUser;

}
