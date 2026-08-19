// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.dto;

import java.time.Instant;
import java.util.UUID;

import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;

public record CategoryResponse(
    UUID id,
    String name,
    CategoryType type,
    boolean active,
    Instant createdAt,
    Instant updatedAt,
    Instant deletedAt
) {
    public static CategoryResponse fromEntity(FinancialCategory category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getType(),
            category.isActive(),
            category.getCreatedAt(),
            category.getUpdatedAt(),
            category.getDeletedAt()
        );
    }
}
