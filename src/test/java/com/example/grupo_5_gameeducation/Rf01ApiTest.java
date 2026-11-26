package com.example.grupo_5_gameeducation;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes HTTP do RF01 no Core, mandando os headers X-User-* exatamente como o gateway manda.
 * Conferem status, formato {"erro": "..."} e a mascara de email no JSON.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Rf01ApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("GET /api/cursos?area=DEVOPS devolve so cursos de DevOps")
    void listarCursosPorArea() throws Exception {
        mvc.perform(comoAna(get("/api/cursos").param("area", "DEVOPS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].area", everyItem(is("DEVOPS"))))
                .andExpect(jsonPath("$[0].titulo").isNotEmpty())
                .andExpect(jsonPath("$[0].cargaHoraria").isNumber());
    }

    @Test
    @DisplayName("Area invalida da 400 {erro}")
    void areaInvalida() throws Exception {
        mvc.perform(comoAna(get("/api/cursos").param("area", "MARKETING")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("area")));
    }

    @Test
    @DisplayName("GET /api/cursos/{id} inexistente da 404 {erro}")
    void cursoInexistente() throws Exception {
        mvc.perform(comoAna(get("/api/cursos/999999")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test
    @DisplayName("ADMIN cadastra curso: 201 com o CursoResponse do contrato")
    void adminCadastraCurso() throws Exception {
        mvc.perform(comoAdmin(post("/api/cursos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Observabilidade com Grafana","area":"DEVOPS","nivel":"AVANCADO","cargaHoraria":18}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Observabilidade com Grafana"))
                .andExpect(jsonPath("$.area").value("DEVOPS"))
                .andExpect(jsonPath("$.nivel").value("AVANCADO"))
                .andExpect(jsonPath("$.cargaHoraria").value(18));
    }

    @Test
    @DisplayName("COLABORADOR cadastrando curso da 403 {erro} (defesa em profundidade no Core)")
    void colaboradorCadastraCursoDa403() throws Exception {
        mvc.perform(comoAna(post("/api/cursos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"X","area":"QA","nivel":"INICIANTE","cargaHoraria":8}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test
    @DisplayName("Corpo invalido da 400 com os campos que falharam")
    void corpoInvalido() throws Exception {
        mvc.perform(comoAdmin(post("/api/cursos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"","area":"QA","nivel":"INICIANTE","cargaHoraria":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("titulo")))
                .andExpect(jsonPath("$.erro", containsString("cargaHoraria")));
    }

    @Test
    @DisplayName("ana ve o proprio perfil com email mascarado")
    void anaVeProprioPerfil() throws Exception {
        mvc.perform(comoAna(get("/api/colaboradores/1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.email").value("a***@empresa.com"))
                .andExpect(jsonPath("$.cargo").value("Analista de QA"))
                .andExpect(jsonPath("$.area").value("QA"))
                .andExpect(jsonPath("$.anosExperiencia").value(3))
                .andExpect(jsonPath("$.cursosConcluidos").value(4))
                .andExpect(jsonPath("$.mediaNotas").value(8.1));
    }

    @Test
    @DisplayName("ana no perfil do carlos da 403 {erro}")
    void anaNoPerfilDoCarlosDa403() throws Exception {
        mvc.perform(comoAna(get("/api/colaboradores/2")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test
    @DisplayName("ADMIN ve email completo e recebe 404 em id inexistente")
    void adminVeEmailCompleto() throws Exception {
        mvc.perform(comoAdmin(get("/api/colaboradores/1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@empresa.com"));
        mvc.perform(comoAdmin(get("/api/colaboradores/999999")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test
    @DisplayName("Listar colaboradores: ADMIN 200, COLABORADOR 403")
    void listarColaboradores() throws Exception {
        mvc.perform(comoAdmin(get("/api/colaboradores")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
        mvc.perform(comoAna(get("/api/colaboradores")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN cadastra colaborador: 201; email invalido da 400")
    void adminCadastraColaborador() throws Exception {
        mvc.perform(comoAdmin(post("/api/colaboradores"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Eduardo Reis","email":"eduardo@empresa.com","cargo":"Analista de Dados",
                                 "area":"DADOS","anosExperiencia":1,"cursosConcluidos":2,"mediaNotas":7.9}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("eduardo@empresa.com"));
        mvc.perform(comoAdmin(post("/api/colaboradores"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Sem Email","email":"nao-e-email","cargo":"X",
                                 "area":"DADOS","anosExperiencia":1,"cursosConcluidos":2,"mediaNotas":7.9}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("email")));
    }

    @Test
    @DisplayName("US1: concluir curso fora do catalogo da 400 {erro}")
    void conclusaoForaDoCatalogo() throws Exception {
        String aluno = mvc.perform(comoAna(post("/api/alunos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Bruno\",\"plano\":\"BASICO\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = aluno.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(comoAna(post("/api/alunos/" + id + "/conclusoes"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tituloCurso\":\"Curso Inventado\",\"media\":9.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("nao existe no catalogo")));
    }

    @Test
    @DisplayName("Aluno inexistente da 404 {erro} (antes era o JSON padrao do Spring)")
    void alunoInexistente() throws Exception {
        mvc.perform(comoAna(get("/api/alunos/999999")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Aluno nao encontrado: 999999"));
    }

    private static MockHttpServletRequestBuilder comoAna(MockHttpServletRequestBuilder req) {
        return req.header("X-User-Id", "ana").header("X-User-Roles", "COLABORADOR").header("X-Colaborador-Id", "1");
    }

    private static MockHttpServletRequestBuilder comoAdmin(MockHttpServletRequestBuilder req) {
        return req.header("X-User-Id", "admin").header("X-User-Roles", "ADMIN").header("X-Colaborador-Id", "");
    }
}
