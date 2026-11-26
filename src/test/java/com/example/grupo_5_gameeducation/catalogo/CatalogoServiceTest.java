package com.example.grupo_5_gameeducation.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.grupo_5_gameeducation.catalogo.api.Area;
import com.example.grupo_5_gameeducation.catalogo.api.CatalogoApi;
import com.example.grupo_5_gameeducation.catalogo.api.CriarCursoRequest;
import com.example.grupo_5_gameeducation.catalogo.api.CursoResumo;
import com.example.grupo_5_gameeducation.catalogo.api.Nivel;
import com.example.grupo_5_gameeducation.comum.erro.AcessoNegadoException;
import com.example.grupo_5_gameeducation.comum.erro.ConflitoException;
import com.example.grupo_5_gameeducation.comum.erro.NaoEncontradoException;
import com.example.grupo_5_gameeducation.comum.identidade.Solicitante;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes do servico do catalogo pelo contrato publico (CatalogoApi), como outro modulo usaria.
 * Cada teste roda numa transacao desfeita no fim, entao o seed fica intacto.
 */
@SpringBootTest
@Transactional
class CatalogoServiceTest {

    private static final Solicitante ADMIN = Solicitante.dosHeaders("admin", "ADMIN", "");
    private static final Solicitante ANA = Solicitante.dosHeaders("ana", "COLABORADOR", "1");

    @Autowired
    private CatalogoApi catalogo;

    @Test
    @DisplayName("Seed cria 10 cursos cobrindo as 5 areas")
    void seedCriaCursos() {
        List<CursoResumo> cursos = catalogo.listar(null);

        assertThat(cursos).hasSize(10);
        assertThat(cursos).extracting(CursoResumo::area).contains(Area.values());
        assertThat(catalogo.existe("Fundamentos de Agile Testing")).isTrue();
    }

    @Test
    @DisplayName("ADMIN cadastra curso e ele passa a existir no catalogo")
    void adminCadastraCurso() {
        CursoResumo criado = catalogo.cadastrar(
                new CriarCursoRequest("Terraform Essencial", Area.DEVOPS, Nivel.INTERMEDIARIO, 24), ADMIN);

        assertThat(criado.id()).isNotNull();
        assertThat(criado.titulo()).isEqualTo("Terraform Essencial");
        assertThat(catalogo.buscarPorId(criado.id()).area()).isEqualTo(Area.DEVOPS);
        assertThat(catalogo.existe("terraform essencial")).isTrue();
    }

    @Test
    @DisplayName("Listar por area devolve so cursos daquela area")
    void listarPorArea() {
        List<CursoResumo> devops = catalogo.listar(Area.DEVOPS);

        assertThat(devops).isNotEmpty();
        assertThat(devops).allMatch(curso -> curso.area() == Area.DEVOPS);
        assertThat(devops).extracting(CursoResumo::titulo).contains("Docker na Prática");
    }

    @Test
    @DisplayName("COLABORADOR nao cadastra curso (403), mesmo chamando o Core direto")
    void colaboradorNaoCadastra() {
        CriarCursoRequest request = new CriarCursoRequest("Curso X", Area.QA, Nivel.INICIANTE, 8);

        assertThatThrownBy(() -> catalogo.cadastrar(request, ANA)).isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("Titulo repetido (sem diferenciar maiusculas) da 409")
    void tituloRepetido() {
        CriarCursoRequest repetido = new CriarCursoRequest("docker na prática", Area.DEVOPS, Nivel.INICIANTE, 10);

        assertThatThrownBy(() -> catalogo.cadastrar(repetido, ADMIN)).isInstanceOf(ConflitoException.class);
    }

    @Test
    @DisplayName("Curso inexistente da 404")
    void cursoInexistente() {
        assertThatThrownBy(() -> catalogo.buscarPorId(999_999L)).isInstanceOf(NaoEncontradoException.class);
    }

    @Test
    @DisplayName("Busca por titulo ignora maiusculas e devolve o titulo oficial do catalogo")
    void buscaPorTituloDevolveTituloOficial() {
        assertThat(catalogo.buscarPorTitulo("  DOCKER NA PRÁTICA "))
                .get().extracting(CursoResumo::titulo).isEqualTo("Docker na Prática");
        assertThat(catalogo.buscarPorTitulo("Curso que nao existe")).isEmpty();
    }
}
