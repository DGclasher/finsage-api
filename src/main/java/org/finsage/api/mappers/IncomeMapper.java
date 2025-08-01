package org.finsage.api.mappers;

import org.finsage.api.entities.Income;
import org.finsage.api.models.IncomeDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IncomeMapper {
    Income incomeDtoToIncome(IncomeDTO income);
    IncomeDTO incomeToIncomeDto(Income income);
}
