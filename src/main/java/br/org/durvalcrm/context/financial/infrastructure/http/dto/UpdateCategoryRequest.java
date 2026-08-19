// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.infrastructure.http.dto;

import br.org.durvalcrm.context.financial.application.dto.UpdateCategoryCommand;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UpdateCategoryRequest(

    @NotBlank(message = "O novo nome da categoria é obrigatório.")
    String name

) {
    public UpdateCategoryCommand toCommand(UUID id) {
        return new UpdateCategoryCommand(id, name);
    }
}
