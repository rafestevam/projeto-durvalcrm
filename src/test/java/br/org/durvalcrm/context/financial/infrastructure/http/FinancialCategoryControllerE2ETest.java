// SPDX-FileCopyrightText: 2026 DurvalCRM Contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package br.org.durvalcrm.context.financial.infrastructure.http;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import br.org.durvalcrm.context.financial.domain.entity.FinancialCategory;
import br.org.durvalcrm.context.financial.domain.enums.CategoryType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.UUID;

/**
 * Testes E2E (ponta a ponta) dos endpoints de Categorias Financeiras.
 *
 * <p>Sobe o contexto Quarkus completo (REST + CDI + JPA + H2 em memória) e
 * dispara requisições HTTP reais via RestAssured contra
 * {@code http://localhost:8081/api/v1/financial/categories}.</p>
 *
 * <h3>Isolamento de estado</h3>
 * <p>O {@link E2ETestFixtureHelper} é um CDI bean real: seus métodos
 * {@code @Transactional} são interceptados e commitam antes que o
 * RestAssured dispare a requisição — garantindo que os dados de fixture
 * estejam visíveis para o servidor HTTP na mesma JVM.
 * O {@code @AfterEach} inativa os artefatos criados para evitar
 * contaminação entre testes.</p>
 *
 * <h3>Bean Validation — formato de erro</h3>
 * <p>O RESTEasy Reactive do Quarkus possui um mapper nativo para
 * {@code ConstraintViolationException} de prioridade mais alta que o
 * {@link CategoryExceptionMapper}. O corpo retornado segue o formato:
 * <pre>{"title":"Constraint Violation","status":400,"violations":[{"field":"...","message":"..."}]}</pre>
 * Os testes de validação de payload assertam sobre esse formato real.</p>
 */
@QuarkusTest
@DisplayName("FinancialCategoryController - Testes E2E dos endpoints HTTP")
class FinancialCategoryControllerE2ETest {

    private static final String BASE_PATH = "/api/v1/financial/categories";

    @Inject
    E2ETestFixtureHelper fixture;

    // ── Limpeza pós-teste ─────────────────────────────────────────────────────

    @AfterEach
    void cleanUp() {
        for (String name : new String[]{
                "E2E Doações", "E2E Aluguel", "E2E Cantina", "E2E Luz",
                "E2E Dup", "E2E Rename", "E2E Rename Atualizado",
                "E2E Cantina Renomeada", "E2E Delete", "E2E Patch"
        }) {
            fixture.deleteByName(name);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // POST /api/v1/financial/categories
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST - Cadastrar categoria")
    class PostTests {

        @Test
        @Timeout(30)
        @DisplayName("201 Created ao cadastrar categoria RECEITA com payload válido")
        void shouldReturn201WhenCreatingReceita() {
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "E2E Doações", "type": "RECEITA"}
                    """)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(201)
                .contentType(MediaType.APPLICATION_JSON)
                .body("id",        notNullValue())
                .body("name",      equalTo("E2E Doações"))
                .body("type",      equalTo("RECEITA"))
                .body("active",    equalTo(true))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
        }

        @Test
        @Timeout(30)
        @DisplayName("201 Created ao cadastrar categoria DESPESA com payload válido")
        void shouldReturn201WhenCreatingDespesa() {
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "E2E Aluguel", "type": "DESPESA"}
                    """)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(201)
                .body("type", equalTo("DESPESA"))
                .body("name", equalTo("E2E Aluguel"));
        }

        @Test
        @Timeout(30)
        @DisplayName("400 Bad Request (Constraint Violation) quando name está em branco")
        void shouldReturn400WhenNameIsBlank() {
            // Quarkus RESTEasy Reactive usa seu próprio ConstraintViolationExceptionMapper
            // com prioridade mais alta → formato: {title, status, violations:[{field, message}]}
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "  ", "type": "RECEITA"}
                    """)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(400)
                .body("status",         equalTo(400))
                .body("title",          equalTo("Constraint Violation"))
                .body("violations",     notNullValue())
                .body("violations[0].message", not(emptyOrNullString()));
        }

        @Test
        @Timeout(30)
        @DisplayName("400 Bad Request quando name está ausente no payload")
        void shouldReturn400WhenNameIsMissing() {
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"type": "RECEITA"}
                    """)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("violations", notNullValue());
        }

        @Test
        @Timeout(30)
        @DisplayName("400 Bad Request quando type está ausente no payload")
        void shouldReturn400WhenTypeIsMissing() {
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "Sem Tipo"}
                    """)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("violations", notNullValue());
        }

        @Test
        @Timeout(30)
        @DisplayName("400 Bad Request com múltiplas violations quando name e type estão ausentes")
        void shouldReturn400WithMultipleViolationsWhenBodyIsEmpty() {
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(400)
                .body("status",     equalTo(400))
                .body("violations", hasSize(greaterThanOrEqualTo(2)));
        }

        @Test
        @Timeout(30)
        @DisplayName("409 Conflict ao tentar criar categoria com nome e tipo já existentes e ativos")
        void shouldReturn409WhenActiveDuplicateExists() {
            // fixture.persistActive usa @Transactional em bean CDI real → commit garantido
            fixture.persistActive("E2E Dup", CategoryType.RECEITA);

            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "E2E Dup", "type": "RECEITA"}
                    """)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(409)
                .body("status",    equalTo(409))
                .body("message",   not(emptyOrNullString()))
                .body("timestamp", notNullValue());
        }

        @Test
        @Timeout(30)
        @DisplayName("Corpo de erro de domínio deve conter status, message e timestamp")
        void domainErrorBodyShouldHaveThreeFields() {
            // Usa um ID inexistente para disparar CategoryNotFoundError via domínio
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "E2E Dup", "type": "RECEITA"}
                    """)
            .when()
                .put(BASE_PATH + "/{id}", UUID.randomUUID())
            .then()
                .statusCode(404)
                .body("status",    equalTo(404))
                .body("message",   notNullValue())
                .body("timestamp", notNullValue());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GET /api/v1/financial/categories
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET - Listar categorias")
    class GetTests {

        @Test
        @Timeout(30)
        @DisplayName("200 OK retorna array JSON")
        void shouldReturn200WithJsonArray() {
            given()
            .when()
                .get(BASE_PATH)
            .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body("$", notNullValue());
        }

        @Test
        @Timeout(30)
        @DisplayName("200 OK — categoria recém-criada aparece na listagem sem filtros")
        void shouldListNewCategoryWithoutFilters() {
            fixture.persistActive("E2E Cantina", CategoryType.RECEITA);

            given()
            .when()
                .get(BASE_PATH)
            .then()
                .statusCode(200)
                .body("name", hasItem("E2E Cantina"));
        }

        @Test
        @Timeout(30)
        @DisplayName("200 OK — ?status=active retorna apenas categorias ativas (nenhuma com active=false)")
        void shouldReturnOnlyActiveCategoriesWhenStatusIsActive() {
            fixture.persistActive("E2E Cantina", CategoryType.RECEITA);
            fixture.persistInactive("E2E Luz",   CategoryType.DESPESA);

            given()
                .queryParam("status", "active")
            .when()
                .get(BASE_PATH)
            .then()
                .statusCode(200)
                .body("findAll { it.active == false }", hasSize(0));
        }

        @Test
        @Timeout(30)
        @DisplayName("200 OK — ?type=RECEITA retorna apenas categorias de receita")
        void shouldReturnOnlyReceitaWhenTypeParamIsReceita() {
            fixture.persistActive("E2E Cantina", CategoryType.RECEITA);
            fixture.persistActive("E2E Luz",     CategoryType.DESPESA);

            given()
                .queryParam("type", "RECEITA")
            .when()
                .get(BASE_PATH)
            .then()
                .statusCode(200)
                .body("findAll { it.type == 'DESPESA' }",  hasSize(0))
                .body("findAll { it.type == 'RECEITA' }.size()", greaterThanOrEqualTo(1));
        }

        @Test
        @Timeout(30)
        @DisplayName("200 OK — ?type=DESPESA retorna apenas categorias de despesa")
        void shouldReturnOnlyDespesaWhenTypeParamIsDespesa() {
            fixture.persistActive("E2E Luz", CategoryType.DESPESA);

            given()
                .queryParam("type", "DESPESA")
            .when()
                .get(BASE_PATH)
            .then()
                .statusCode(200)
                .body("findAll { it.type == 'RECEITA' }",  hasSize(0))
                .body("findAll { it.type == 'DESPESA' }.size()", greaterThanOrEqualTo(1));
        }

        @Test
        @Timeout(30)
        @DisplayName("200 OK — ?type=RECEITA&status=active retorna apenas RECEITA ativas")
        void shouldFilterByTypeAndActiveStatus() {
            fixture.persistActive("E2E Cantina", CategoryType.RECEITA);
            fixture.persistInactive("E2E Luz",   CategoryType.DESPESA);

            given()
                .queryParam("type",   "RECEITA")
                .queryParam("status", "active")
            .when()
                .get(BASE_PATH)
            .then()
                .statusCode(200)
                .body("findAll { it.type != 'RECEITA' }",  hasSize(0))
                .body("findAll { it.active == false }",     hasSize(0));
        }

        @Test
        @Timeout(30)
        @DisplayName("200 OK — ?status=all inclui categorias inativas")
        void shouldIncludeInactiveCategoriesWhenStatusIsAll() {
            fixture.persistInactive("E2E Luz", CategoryType.DESPESA);

            given()
                .queryParam("status", "all")
            .when()
                .get(BASE_PATH)
            .then()
                .statusCode(200)
                .body("name", hasItem("E2E Luz"));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PUT /api/v1/financial/categories/{id}
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT - Atualizar nome da categoria")
    class PutTests {

        @Test
        @Timeout(30)
        @DisplayName("200 OK ao renomear uma categoria existente")
        void shouldReturn200WhenRenaming() {
            FinancialCategory cat = fixture.persistActive("E2E Rename", CategoryType.RECEITA);

            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "E2E Rename Atualizado"}
                    """)
            .when()
                .put(BASE_PATH + "/{id}", cat.getId())
            .then()
                .statusCode(200)
                .body("id",   equalTo(cat.getId().toString()))
                .body("name", equalTo("E2E Rename Atualizado"))
                .body("type", equalTo("RECEITA"));
        }

        @Test
        @Timeout(30)
        @DisplayName("404 Not Found ao tentar atualizar ID inexistente")
        void shouldReturn404WhenUpdatingUnknownId() {
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "Qualquer Nome"}
                    """)
            .when()
                .put(BASE_PATH + "/{id}", UUID.randomUUID())
            .then()
                .statusCode(404)
                .body("status",    equalTo(404))
                .body("message",   not(emptyOrNullString()))
                .body("timestamp", notNullValue());
        }

        @Test
        @Timeout(30)
        @DisplayName("409 Conflict ao renomear para nome que já existe e está ativo no mesmo tipo")
        void shouldReturn409WhenRenamingToDuplicateName() {
            fixture.persistActive("E2E Dup",    CategoryType.RECEITA);
            FinancialCategory target = fixture.persistActive("E2E Rename", CategoryType.RECEITA);

            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "E2E Dup"}
                    """)
            .when()
                .put(BASE_PATH + "/{id}", target.getId())
            .then()
                .statusCode(409)
                .body("status", equalTo(409));
        }

        @Test
        @Timeout(30)
        @DisplayName("400 Bad Request (Constraint Violation) quando name está em branco no PUT")
        void shouldReturn400WhenNameIsBlankOnPut() {
            FinancialCategory cat = fixture.persistActive("E2E Rename", CategoryType.RECEITA);

            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": ""}
                    """)
            .when()
                .put(BASE_PATH + "/{id}", cat.getId())
            .then()
                .statusCode(400)
                .body("status",     equalTo(400))
                .body("violations", notNullValue());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DELETE /api/v1/financial/categories/{id}
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE - Inativação lógica via DELETE")
    class DeleteTests {

        @Test
        @Timeout(30)
        @DisplayName("200 OK ao inativar categoria ativa via DELETE")
        void shouldReturn200WhenDeletingActiveCategory() {
            FinancialCategory cat = fixture.persistActive("E2E Delete", CategoryType.DESPESA);

            given()
            .when()
                .delete(BASE_PATH + "/{id}", cat.getId())
            .then()
                .statusCode(200)
                .body("id",        equalTo(cat.getId().toString()))
                .body("active",    equalTo(false))
                .body("deletedAt", notNullValue());
        }

        @Test
        @Timeout(30)
        @DisplayName("404 Not Found ao tentar deletar ID inexistente")
        void shouldReturn404WhenDeletingUnknownId() {
            given()
            .when()
                .delete(BASE_PATH + "/{id}", UUID.randomUUID())
            .then()
                .statusCode(404)
                .body("status",    equalTo(404))
                .body("message",   not(emptyOrNullString()))
                .body("timestamp", notNullValue());
        }

        @Test
        @Timeout(30)
        @DisplayName("400 Bad Request ao tentar deletar categoria já inativa")
        void shouldReturn400WhenDeletingAlreadyInactiveCategory() {
            FinancialCategory cat = fixture.persistInactive("E2E Delete", CategoryType.DESPESA);

            given()
            .when()
                .delete(BASE_PATH + "/{id}", cat.getId())
            .then()
                .statusCode(400)
                .body("status",  equalTo(400))
                .body("message", not(emptyOrNullString()));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PATCH /api/v1/financial/categories/{id}/inactivate
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PATCH - Inativação lógica via PATCH /inactivate")
    class PatchTests {

        @Test
        @Timeout(30)
        @DisplayName("200 OK ao inativar categoria ativa via PATCH /inactivate")
        void shouldReturn200WhenInactivatingViaPatch() {
            FinancialCategory cat = fixture.persistActive("E2E Patch", CategoryType.RECEITA);

            given()
            .when()
                .patch(BASE_PATH + "/{id}/inactivate", cat.getId())
            .then()
                .statusCode(200)
                .body("id",        equalTo(cat.getId().toString()))
                .body("active",    equalTo(false))
                .body("deletedAt", notNullValue());
        }

        @Test
        @Timeout(30)
        @DisplayName("404 Not Found ao usar PATCH em ID inexistente")
        void shouldReturn404WhenPatchingUnknownId() {
            given()
            .when()
                .patch(BASE_PATH + "/{id}/inactivate", UUID.randomUUID())
            .then()
                .statusCode(404)
                .body("status",    equalTo(404))
                .body("message",   not(emptyOrNullString()))
                .body("timestamp", notNullValue());
        }

        @Test
        @Timeout(30)
        @DisplayName("400 Bad Request ao tentar inativar via PATCH categoria já inativa")
        void shouldReturn400WhenPatchingAlreadyInactiveCategory() {
            FinancialCategory cat = fixture.persistInactive("E2E Patch", CategoryType.RECEITA);

            given()
            .when()
                .patch(BASE_PATH + "/{id}/inactivate", cat.getId())
            .then()
                .statusCode(400)
                .body("status",  equalTo(400))
                .body("message", not(emptyOrNullString()));
        }

        @Test
        @Timeout(30)
        @DisplayName("PATCH e DELETE produzem o mesmo shape de resposta 200")
        void patchAndDeleteShouldProduceSameHttpShape() {
            FinancialCategory catDelete = fixture.persistActive("E2E Delete", CategoryType.DESPESA);
            FinancialCategory catPatch  = fixture.persistActive("E2E Patch",  CategoryType.RECEITA);

            given()
            .when()
                .delete(BASE_PATH + "/{id}", catDelete.getId())
            .then()
                .statusCode(200)
                .body("active",    equalTo(false))
                .body("deletedAt", notNullValue());

            given()
            .when()
                .patch(BASE_PATH + "/{id}/inactivate", catPatch.getId())
            .then()
                .statusCode(200)
                .body("active",    equalTo(false))
                .body("deletedAt", notNullValue());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Fluxos completos ponta a ponta
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Fluxos completos (Create → List → Update → Inactivate)")
    class FullFlowTests {

        @Test
        @Timeout(30)
        @DisplayName("Fluxo: criar → listar → renomear → inativar via DELETE → rejeitar segunda deleção")
        void fullCreateListUpdateDeleteFlow() {
            // 1. POST → 201
            String id = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "E2E Cantina", "type": "RECEITA"}
                    """)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(201)
                .extract().path("id");

            // 2. GET sem filtro → categoria presente
            given()
            .when()
                .get(BASE_PATH)
            .then()
                .statusCode(200)
                .body("id", hasItem(id));

            // 3. PUT → 200 com novo nome
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "E2E Cantina Renomeada"}
                    """)
            .when()
                .put(BASE_PATH + "/{id}", id)
            .then()
                .statusCode(200)
                .body("name", equalTo("E2E Cantina Renomeada"));

            // 4. DELETE → 200, active=false
            given()
            .when()
                .delete(BASE_PATH + "/{id}", id)
            .then()
                .statusCode(200)
                .body("active",    equalTo(false))
                .body("deletedAt", notNullValue());

            // 5. DELETE novamente → 400 (já inativa)
            given()
            .when()
                .delete(BASE_PATH + "/{id}", id)
            .then()
                .statusCode(400)
                .body("status", equalTo(400));
        }

        @Test
        @Timeout(30)
        @DisplayName("Fluxo: criar → listar com filtro de tipo → inativar via PATCH → rejeitar segundo PATCH")
        void fullCreateListInactivatePatchFlow() {
            // 1. POST → 201
            String id = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "E2E Patch", "type": "RECEITA"}
                    """)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(201)
                .extract().path("id");

            // 2. GET ?type=RECEITA → encontra pelo ID
            given()
                .queryParam("type", "RECEITA")
            .when()
                .get(BASE_PATH)
            .then()
                .statusCode(200)
                .body("id", hasItem(id));

            // 3. PATCH → 200, active=false
            given()
            .when()
                .patch(BASE_PATH + "/{id}/inactivate", id)
            .then()
                .statusCode(200)
                .body("active",    equalTo(false))
                .body("deletedAt", notNullValue());

            // 4. PATCH novamente → 400 (já inativa)
            given()
            .when()
                .patch(BASE_PATH + "/{id}/inactivate", id)
            .then()
                .statusCode(400)
                .body("status", equalTo(400));
        }

        @Test
        @Timeout(30)
        @DisplayName("Não deve permitir criar duplicata ativa após criar e inativar uma categoria de mesmo nome+tipo")
        void shouldAllowDuplicateAfterInactivation() {
            // 1. Cria via POST
            String id = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "E2E Dup", "type": "RECEITA"}
                    """)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(201)
                .extract().path("id");

            // 2. Segunda criação com mesmo nome → 409
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "E2E Dup", "type": "RECEITA"}
                    """)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(409);

            // 3. Inativa a primeira via DELETE
            given()
            .when()
                .delete(BASE_PATH + "/{id}", id)
            .then()
                .statusCode(200);

            // 4. Agora pode criar novamente (a existente está inativa)
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"name": "E2E Dup", "type": "RECEITA"}
                    """)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(201);
        }
    }
}
