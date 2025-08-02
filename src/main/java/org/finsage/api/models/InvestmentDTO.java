package org.finsage.api.models;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.finsage.api.entities.InvestmentType;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class InvestmentDTO {
    private UUID id;
    private Integer version;

    @NotNull
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
}
