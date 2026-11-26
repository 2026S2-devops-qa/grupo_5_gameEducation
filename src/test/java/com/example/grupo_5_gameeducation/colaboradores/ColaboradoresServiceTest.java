package com.example.grupo_5_gameeducation.colaboradores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.grupo_5_gameeducation.catalogo.api.Area;
import com.example.grupo_5_gameeducation.colaboradores.api.ColaboradorResponse;
import com.example.grupo_5_gameeducation.colaboradores.api.ColaboradoresApi;
import com.example.grupo_5_gameeducation.colaboradores.api.CriarColaboradorRequest;
import com.example.grupo_5_gameeducation.colaboradores.api.PerfilColaborador;
import com.example.grupo_5_gameeducation.comum.erro.AcessoNegadoException;
import com.example.grupo_5_gameeducation.comum.erro.ConflitoException;
import com.example.grupo_5_gameeducation.comum.erro.NaoEncontradoException;
import com.example.grupo_5_gameeducation.comum.identidade.Solicitante;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes do servico de colaboradores pelo contrato publico (ColaboradoresApi): regras de
 * acesso a dados pessoais (RNF03) e ids do seed batendo com os usuarios do gateway.
 */
@SpringBootTest
@Transactional
class ColaboradoresServiceTest {

    private static final Solicitante ADMIN = Solicitante.dosHeaders("admin", "ADMIN", "");
    private static final Solicitante ANA = Solicitante.dosHeaders("ana", "COLABORADOR", "1");

    @Autowired
    private ColaboradoresApi colaboradores;

    @Test
    @DisplayName("Seed: ana e o colaborador 1 e carlos o 2, como no gateway")
    void idsDoSeedBatemComOGateway() {
        assertThat(colaboradores.buscarPerfil(1L).nome()).isEqualTo("Ana Souza");
        assertThat(colaboradores.buscarPerfil(2L).nome()).isEqualTo("Carlos Lima");
    }

    @Test
    @DisplayName("Perfil para outros modulos traz os dados do ML (contrato do RF02)")
    void perfilParaOutrosModulos() {
        PerfilColaborador ana = colaboradores.buscarPerfil(1L);

        assertThat(ana.area()).isEqualTo(Area.QA);
        assertThat(ana.anosExperiencia()).isEqualTo(3);
        assertThat(ana.cursosConcluidos()).isEqualTo(4);
        assertThat(ana.mediaNotas()).isEqualTo(8.1);
    }

    @Test
    @DisplayName("Colaborador ve o proprio perfil com email mascarado")
    void colaboradorVeProprioPerfilMascarado() {
        ColaboradorResponse ana = colaboradores.buscar(1L, ANA);

        assertThat(ana.nome()).isEqualTo("Ana Souza");
        assertThat(ana.email()).isEqualTo("a***@empresa.com");
    }

    @Test
    @DisplayName("ADMIN ve o email completo")
    void adminVeEmailCompleto() {
        assertThat(colaboradores.buscar(1L, ADMIN).email()).isEqualTo("ana@empresa.com");
    }

    @Test
    @DisplayName("Colaborador acessando outro colaborador da 403")
    void colaboradorAcessandoOutroDa403() {
        assertThatThrownBy(() -> colaboradores.buscar(2L, ANA)).isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("Colaborador pedindo id inexistente tambem da 403 (nao revela quais ids existem)")
    void colaboradorIdInexistenteDa403() {
        assertThatThrownBy(() -> colaboradores.buscar(999_999L, ANA)).isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("Colaborador nao encontrado da 404 (para o ADMIN)")
    void colaboradorNaoEncontradoDa404() {
        assertThatThrownBy(() -> colaboradores.buscar(999_999L, ADMIN)).isInstanceOf(NaoEncontradoException.class);
        assertThatThrownBy(() -> colaboradores.buscarPerfil(999_999L)).isInstanceOf(NaoEncontradoException.class);
    }

    @Test
    @DisplayName("Listar todos: so ADMIN")
    void listarSoAdmin() {
        assertThat(colaboradores.listar(ADMIN)).hasSizeGreaterThanOrEqualTo(2);
        assertThatThrownBy(() -> colaboradores.listar(ANA)).isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("ADMIN cadastra colaborador; email repetido da 409; COLABORADOR nao cadastra")
    void cadastro() {
        CriarColaboradorRequest nova = new CriarColaboradorRequest(
                "Daniela Prado", "Daniela@Empresa.com", "Cientista de Dados", Area.DADOS, 2, 3, 8.7);

        ColaboradorResponse criada = colaboradores.cadastrar(nova, ADMIN);

        assertThat(criada.id()).isNotNull();
        assertThat(criada.email()).isEqualTo("daniela@empresa.com");
        assertThatThrownBy(() -> colaboradores.cadastrar(nova, ADMIN)).isInstanceOf(ConflitoException.class);
        assertThatThrownBy(() -> colaboradores.cadastrar(nova, ANA)).isInstanceOf(AcessoNegadoException.class);
    }
}
