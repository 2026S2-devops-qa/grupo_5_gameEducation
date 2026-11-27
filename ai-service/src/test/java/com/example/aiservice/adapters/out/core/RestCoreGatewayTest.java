package com.example.aiservice.adapters.out.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.aiservice.domain.AcessoNegadoException;
import com.example.aiservice.domain.CursoCatalogo;
import com.example.aiservice.domain.PerfilColaborador;
import com.example.aiservice.domain.Solicitante;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** O agente chama o Core repassando a identidade do usuario logado (RNF03). Core simulado. */
class RestCoreGatewayTest {

    private static final String CORE = "http://app:8081";
    private final Solicitante ana = new Solicitante("ana", List.of("COLABORADOR"), 1L);

    private MockRestServiceServer core;
    private RestCoreGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        core = MockRestServiceServer.bindTo(builder).build();
        gateway = new RestCoreGateway(builder, CORE);
    }

    @Test
    void repassaOsHeadersDoUsuarioLogadoAoConsultarOPerfil() {
        core.expect(requestTo(CORE + "/api/colaboradores/1"))
                .andExpect(header("X-User-Id", "ana"))
                .andExpect(header("X-User-Roles", "COLABORADOR"))
                .andExpect(header("X-Colaborador-Id", "1"))
                .andRespond(withSuccess("""
                        {"id":1,"nome":"Ana Souza","email":"a***@empresa.com","cargo":"Analista de QA","area":"QA",
                         "anosExperiencia":3,"cursosConcluidos":4,"mediaNotas":8.1}""", MediaType.APPLICATION_JSON));

        PerfilColaborador perfil = gateway.buscarPerfil(ana, 1L);

        assertThat(perfil.nome()).isEqualTo("Ana Souza");
        assertThat(perfil.area()).isEqualTo("QA");
        core.verify();
    }

    @Test
    void quandoOCoreNegaOAgenteTambemENegado() {
        core.expect(requestTo(CORE + "/api/colaboradores/2"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"erro\":\"Voce so pode consultar o seu proprio perfil.\"}"));

        assertThatThrownBy(() -> gateway.buscarPerfil(ana, 2L)).isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    void listaCursosFiltrandoPorArea() {
        core.expect(requestTo(CORE + "/api/cursos?area=DEVOPS"))
                .andExpect(header("X-User-Id", "ana"))
                .andRespond(withSuccess("""
                        [{"id":1,"titulo":"Docker na Prática","area":"DEVOPS","nivel":"INTERMEDIARIO","cargaHoraria":20}]""",
                        MediaType.APPLICATION_JSON));

        List<CursoCatalogo> cursos = gateway.listarCursos(ana, "DEVOPS");

        assertThat(cursos).extracting(CursoCatalogo::titulo).containsExactly("Docker na Prática");
    }

    @Test
    void cursoInexistenteViraVazio() {
        core.expect(requestTo(CORE + "/api/cursos/99")).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(gateway.buscarCurso(ana, 99L)).isEmpty();
    }
}
