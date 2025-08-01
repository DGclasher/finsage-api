package org.finsage.api.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvestmentSummaryDTO {
    private Double totalInvested;
    private Double currentValue;
    private Double totalGainLoss;
    private Double gainLossPercentage;
}