package org.finsage.api.mappers;

import org.finsage.api.entities.Investment;
import org.finsage.api.models.InvestmentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InvestmentMapper {
    Investment investmentDtoToInvestment(InvestmentDTO investment);
    InvestmentDTO investmentToInvestmentDto(Investment investment);
}
