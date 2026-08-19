// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.infrastructure.http.dto;

import br.org.durvalcrm.context.financial.application.dto.CreateCategoryCommand;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCategoryRequest(

    @NotBlank(message = "O nome da categoria é obrigatório.")
    String name,

    @NotNull(message = "O tipo da categoria (RECEITA ou DESPESA) é obrigatório.")
    CategoryType type

) {
    public CreateCategoryCommand toCommand() {
        return new CreateCategoryCommand(name, type);
    }
}
