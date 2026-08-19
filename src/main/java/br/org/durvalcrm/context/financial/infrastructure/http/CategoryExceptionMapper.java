// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.infrastructure.http;

import java.time.Instant;

import br.org.durvalcrm.context.financial.domain.exception.CategoryAlreadyExistsError;
import br.org.durvalcrm.context.financial.domain.exception.CategoryNotFoundError;
import br.org.durvalcrm.context.financial.domain.exception.DomainValidationException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Converte exceções de domínio e de validação em respostas HTTP semânticas.
 *
 * <p>Hierarquia de mapeamento:</p>
 * <ul>
 *   <li>{@link CategoryNotFoundError}      → 404 Not Found</li>
 *   <li>{@link CategoryAlreadyExistsError} → 409 Conflict</li>
 *   <li>{@link DomainValidationException}  → 400 Bad Request</li>
 *   <li>{@link ConstraintViolationException} → 400 Bad Request (Bean Validation)</li>
 * </ul>
 */
@Provider
public class CategoryExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof CategoryNotFoundError e) {
            return buildError(Response.Status.NOT_FOUND, e.getMessage());
        }

        if (exception instanceof CategoryAlreadyExistsError e) {
            return buildError(Response.Status.CONFLICT, e.getMessage());
        }

        if (exception instanceof DomainValidationException e) {
            return buildError(Response.Status.BAD_REQUEST, e.getMessage());
        }

        if (exception instanceof ConstraintViolationException e) {
            String detail = e.getConstraintViolations().stream()
                    .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                    .sorted()
                    .reduce((a, b) -> a + "; " + b)
                    .orElse(e.getMessage());
            return buildError(Response.Status.BAD_REQUEST, detail);
        }

        // Fallback: 500 Internal Server Error (não vaza stack trace)
        return buildError(Response.Status.INTERNAL_SERVER_ERROR, "Erro interno inesperado.");
    }

    private Response buildError(Response.Status status, String message) {
        return Response
                .status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorBody(status.getStatusCode(), message, Instant.now().toString()))
                .build();
    }

    /**
     * Corpo padronizado de erro retornado em todas as respostas de falha.
     *
     * @param status    Código HTTP numérico.
     * @param message   Mensagem legível descrevendo o problema.
     * @param timestamp ISO-8601 do momento em que o erro ocorreu.
     */
    public record ErrorBody(int status, String message, String timestamp) {}
}
