package org.finsage.api.mappers;

import org.finsage.api.entities.Expense;
import org.finsage.api.models.ExpenseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    Expense expenseDtoToExpense(ExpenseDTO expense);
    ExpenseDTO expenseToExpenseDto(Expense expense);
}
