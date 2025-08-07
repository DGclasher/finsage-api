package org.finsage.api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentSummaryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Double totalInvested;
    private Double currentValue;
    private Double totalGainLoss;
    private Double gainLossPercentage;
}