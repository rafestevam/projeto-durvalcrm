// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.domain.exception;

public class CategoryAlreadyExistsError extends DomainValidationException {
    public CategoryAlreadyExistsError(String message) {
        super(message);
    }
}
