package org.finsage.api.models;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ExpenseDTO {
    private UUID id;
    private Integer version;

    @NotNull
    @NotBlank
    private String category;

    @NotNull
    @Positive
    private Double amount;
}
