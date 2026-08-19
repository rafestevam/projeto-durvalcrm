// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.domain.exception;

public class CategoryNotFoundError extends DomainValidationException {
    public CategoryNotFoundError(String message) {
        super(message);
    }
}
