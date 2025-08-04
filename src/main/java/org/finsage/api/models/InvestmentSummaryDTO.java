package org.finsage.api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentSummaryDTO {
    private Double totalInvested;
    private Double currentValue;
    private Double totalGainLoss;
    private Double gainLossPercentage;
}