// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.infrastructure.http;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import br.org.durvalcrm.context.financial.domain.exception.CategoryAlreadyExistsError;
import br.org.durvalcrm.context.financial.domain.exception.CategoryNotFoundError;
import br.org.durvalcrm.context.financial.domain.exception.DomainValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@DisplayName("CategoryExceptionMapper - Testes unitários do mapeador de exceções HTTP")
class CategoryExceptionMapperTest {

    private CategoryExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CategoryExceptionMapper();
    }

    // ────────────────────────────────────────────────────────────────────────
    // Utilitário: extrai o ErrorBody da resposta
    // ────────────────────────────────────────────────────────────────────────

    private CategoryExceptionMapper.ErrorBody bodyOf(Response response) {
        return (CategoryExceptionMapper.ErrorBody) response.getEntity();
    }

    // ────────────────────────────────────────────────────────────────────────
    // CategoryNotFoundError → 404
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CategoryNotFoundError → 404 Not Found")
    class NotFoundTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar status 404 para CategoryNotFoundError")
        void shouldReturn404() {
            Response response = mapper.toResponse(new CategoryNotFoundError("Categoria não encontrada."));
            assertEquals(404, response.getStatus());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve conter a mensagem de domínio no corpo")
        void shouldContainDomainMessageInBody() {
            Response response = mapper.toResponse(new CategoryNotFoundError("Categoria 'X' não encontrada."));
            assertEquals("Categoria 'X' não encontrada.", bodyOf(response).message());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar Content-Type application/json")
        void shouldReturnJsonContentType() {
            Response response = mapper.toResponse(new CategoryNotFoundError("msg"));
            assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve incluir status numérico 404 no corpo")
        void shouldIncludeNumericStatusInBody() {
            Response response = mapper.toResponse(new CategoryNotFoundError("msg"));
            assertEquals(404, bodyOf(response).status());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve incluir timestamp não-nulo no corpo")
        void shouldIncludeNonNullTimestamp() {
            Response response = mapper.toResponse(new CategoryNotFoundError("msg"));
            assertNotNull(bodyOf(response).timestamp());
            assertFalse(bodyOf(response).timestamp().isBlank());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // CategoryAlreadyExistsError → 409
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CategoryAlreadyExistsError → 409 Conflict")
    class ConflictTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar status 409 para CategoryAlreadyExistsError")
        void shouldReturn409() {
            Response response = mapper.toResponse(new CategoryAlreadyExistsError("Já existe."));
            assertEquals(409, response.getStatus());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve conter a mensagem de domínio no corpo")
        void shouldContainDomainMessageInBody() {
            Response response = mapper.toResponse(new CategoryAlreadyExistsError("Duplicata detectada."));
            assertEquals("Duplicata detectada.", bodyOf(response).message());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve incluir status numérico 409 no corpo")
        void shouldIncludeNumericStatusInBody() {
            Response response = mapper.toResponse(new CategoryAlreadyExistsError("msg"));
            assertEquals(409, bodyOf(response).status());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // DomainValidationException genérica → 400
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DomainValidationException → 400 Bad Request")
    class DomainValidationTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar status 400 para DomainValidationException")
        void shouldReturn400() {
            Response response = mapper.toResponse(new DomainValidationException("Dado inválido."));
            assertEquals(400, response.getStatus());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve conter a mensagem de domínio no corpo")
        void shouldContainDomainMessage() {
            Response response = mapper.toResponse(new DomainValidationException("Nome em branco."));
            assertEquals("Nome em branco.", bodyOf(response).message());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve incluir status numérico 400 no corpo")
        void shouldIncludeNumericStatus() {
            Response response = mapper.toResponse(new DomainValidationException("msg"));
            assertEquals(400, bodyOf(response).status());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // ConstraintViolationException → 400 (Bean Validation)
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ConstraintViolationException → 400 Bad Request (Bean Validation)")
    class ConstraintViolationTests {

        /** Stub mínimo de ConstraintViolation sem precisar de um validador real. */
        private ConstraintViolation<?> stubViolation(String property, String message) {
            return new ConstraintViolation<>() {
                @Override public String getMessage() { return message; }
                @Override public String getMessageTemplate() { return ""; }
                @Override public Object getRootBean() { return null; }
                @Override public Class<Object> getRootBeanClass() { return Object.class; }
                @Override public Object getLeafBean() { return null; }
                @Override public Object[] getExecutableParameters() { return new Object[0]; }
                @Override public Object getExecutableReturnValue() { return null; }
                @Override public Path getPropertyPath() {
                    return new Path() {
                        @Override public java.util.Iterator<Node> iterator() {
                            return java.util.Collections.<Node>emptyList().iterator();
                        }
                        @Override public String toString() { return property; }
                    };
                }
                @Override public Object getInvalidValue() { return null; }
                @Override public jakarta.validation.metadata.ConstraintDescriptor<?> getConstraintDescriptor() {
                    return null;
                }
                @Override public <U> U unwrap(Class<U> type) { return null; }
            };
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar status 400 para ConstraintViolationException")
        void shouldReturn400ForConstraintViolation() {
            ConstraintViolationException ex = new ConstraintViolationException(
                    Set.of(stubViolation("name", "O nome é obrigatório.")));

            Response response = mapper.toResponse(ex);

            assertEquals(400, response.getStatus());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve incluir o path e a mensagem da violação no corpo")
        void shouldIncludeViolationPathAndMessage() {
            ConstraintViolationException ex = new ConstraintViolationException(
                    Set.of(stubViolation("name", "O nome é obrigatório.")));

            Response response = mapper.toResponse(ex);
            String body = bodyOf(response).message();

            assertTrue(body.contains("name"));
            assertTrue(body.contains("O nome é obrigatório."));
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve concatenar múltiplas violações separadas por ponto-e-vírgula")
        void shouldConcatenateMultipleViolations() {
            ConstraintViolationException ex = new ConstraintViolationException(Set.of(
                    stubViolation("name", "obrigatório"),
                    stubViolation("type", "obrigatório")
            ));

            Response response = mapper.toResponse(ex);
            String body = bodyOf(response).message();

            // Duas violações → separador "; " presente
            assertTrue(body.contains(";"));
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve incluir status numérico 400 no corpo da violação")
        void shouldIncludeNumericStatus() {
            ConstraintViolationException ex = new ConstraintViolationException(
                    Set.of(stubViolation("f", "erro")));

            assertEquals(400, bodyOf(mapper.toResponse(ex)).status());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // RuntimeException genérica → 500
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("RuntimeException genérica → 500 Internal Server Error")
    class FallbackTests {

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar status 500 para RuntimeException não mapeada")
        void shouldReturn500ForUnknownRuntimeException() {
            Response response = mapper.toResponse(new RuntimeException("boom"));
            assertEquals(500, response.getStatus());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve retornar mensagem genérica e não vazar detalhes internos")
        void shouldReturnGenericMessageWithoutLeakingInternals() {
            Response response = mapper.toResponse(new RuntimeException("stack trace detail"));
            String msg = bodyOf(response).message();

            assertEquals("Erro interno inesperado.", msg);
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve incluir status numérico 500 no corpo")
        void shouldIncludeNumericStatus() {
            Response response = mapper.toResponse(new RuntimeException("x"));
            assertEquals(500, bodyOf(response).status());
        }

        @Test
        @Timeout(30)
        @DisplayName("Deve incluir timestamp não-nulo no erro 500")
        void shouldIncludeTimestampOn500() {
            Response response = mapper.toResponse(new RuntimeException("x"));
            assertNotNull(bodyOf(response).timestamp());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // ErrorBody record
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ErrorBody - estrutura do record de erro")
    class ErrorBodyTests {

        @Test
        @Timeout(30)
        @DisplayName("ErrorBody deve expor os campos status, message e timestamp corretamente")
        void shouldExposeAllFields() {
            CategoryExceptionMapper.ErrorBody body =
                    new CategoryExceptionMapper.ErrorBody(404, "não encontrado", "2026-01-01T00:00:00Z");

            assertEquals(404, body.status());
            assertEquals("não encontrado", body.message());
            assertEquals("2026-01-01T00:00:00Z", body.timestamp());
        }

        @Test
        @Timeout(30)
        @DisplayName("Dois ErrorBody com mesmos valores devem ser iguais (record equality)")
        void shouldHaveValueEquality() {
            CategoryExceptionMapper.ErrorBody a =
                    new CategoryExceptionMapper.ErrorBody(400, "erro", "ts");
            CategoryExceptionMapper.ErrorBody b =
                    new CategoryExceptionMapper.ErrorBody(400, "erro", "ts");

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }
    }
}
