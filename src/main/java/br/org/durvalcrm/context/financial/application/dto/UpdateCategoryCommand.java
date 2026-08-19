// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.application.dto;

import java.util.UUID;

public record UpdateCategoryCommand(
    UUID id,
    String newName
) {}
