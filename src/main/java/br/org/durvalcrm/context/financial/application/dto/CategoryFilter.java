// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.dto;

import br.org.durvalcrm.context.financial.domain.enums.CategoryType;

public record CategoryFilter(
    CategoryType type,
    boolean isActive
) {
    public static CategoryFilter all(){
        return new CategoryFilter(null, false);
    }

    public static CategoryFilter activeOnly() {
        return new CategoryFilter(null, true);
    }

    public static CategoryFilter byType(CategoryType type) {
        return new CategoryFilter(type, false);
    }



}
