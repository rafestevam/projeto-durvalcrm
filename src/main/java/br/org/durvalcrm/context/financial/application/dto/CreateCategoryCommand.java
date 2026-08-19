// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.dto;

import br.org.durvalcrm.context.financial.domain.enums.CategoryType;

public record CreateCategoryCommand (
    String name,
    CategoryType type
) {}
