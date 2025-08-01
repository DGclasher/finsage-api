package org.finsage.api.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class IncomeDTO {
    private UUID id;
    private Integer version;

    @NotNull
    @NotBlank
    private Double annualPostTaxIncome;

    @NotNull
    @NotBlank
    private Integer incomeYear;
}
