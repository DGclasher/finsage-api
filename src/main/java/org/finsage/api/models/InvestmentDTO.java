package org.finsage.api.models;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.finsage.api.entities.InvestmentType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
