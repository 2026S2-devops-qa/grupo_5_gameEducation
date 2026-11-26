package com.example.grupo_5_gameeducation.colaboradores.internal;

import com.example.grupo_5_gameeducation.colaboradores.api.ColaboradorResponse;
import com.example.grupo_5_gameeducation.colaboradores.api.ColaboradoresApi;
import com.example.grupo_5_gameeducation.colaboradores.api.CriarColaboradorRequest;
import com.example.grupo_5_gameeducation.colaboradores.api.PerfilColaborador;
import com.example.grupo_5_gameeducation.comum.erro.AcessoNegadoException;
import com.example.grupo_5_gameeducation.comum.erro.ConflitoException;
import com.example.grupo_5_gameeducation.comum.erro.NaoEncontradoException;
import com.example.grupo_5_gameeducation.comum.identidade.Solicitante;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servico de aplicacao dos colaboradores: implementa {@link ColaboradoresApi} e concentra as
 * regras de acesso a dados pessoais (RNF03).
 */
@Service
@Transactional(readOnly = true)
public class ColaboradoresService implements ColaboradoresApi {

    private final ColaboradorRepository repository;

    public ColaboradoresService(ColaboradorRepository repository) {
        this.repository = repository;
    }

    @Override
    public PerfilColaborador buscarPerfil(Long id) {
        return buscarEntidade(id).paraPerfil();
    }

    @Override
    public ColaboradorResponse buscar(Long id, Solicitante solicitante) {
        // A permissao e conferida ANTES de buscar: um colaborador pedindo o id de outra pessoa
        // recebe 403 mesmo que o id nao exista, entao nao da para descobrir quais ids existem.
        if (!solicitante.podeAcessarColaborador(id)) {
            throw new AcessoNegadoException("Voce so pode consultar o seu proprio perfil.");
        }
        return paraResponse(buscarEntidade(id), solicitante);
    }

    @Override
    public List<ColaboradorResponse> listar(Solicitante solicitante) {
        exigirAdmin(solicitante, "Apenas ADMIN pode listar todos os colaboradores.");
        return repository.findAllByOrderByIdAsc().stream()
                .map(colaborador -> paraResponse(colaborador, solicitante))
                .toList();
    }

    @Override
    @Transactional
    public ColaboradorResponse cadastrar(CriarColaboradorRequest request, Solicitante solicitante) {
        exigirAdmin(solicitante, "Apenas ADMIN pode cadastrar colaboradores.");
        String email = request.email().trim().toLowerCase();
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Ja existe um colaborador com o email informado.");
        }
        ColaboradorEntity novo = new ColaboradorEntity(
                request.nome().trim(), email, request.cargo().trim(), request.area(),
                request.anosExperiencia(), request.cursosConcluidos(), request.mediaNotas());
        return paraResponse(repository.save(novo), solicitante);
    }

    private ColaboradorEntity buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Colaborador nao encontrado: " + id));
    }

    /** Defesa em profundidade: o gateway ja barra, mas o Core confere de novo. */
    private static void exigirAdmin(Solicitante solicitante, String mensagem) {
        if (!solicitante.isAdmin()) {
            throw new AcessoNegadoException(mensagem);
        }
    }

    /** Email completo so para ADMIN. Para os demais, mascarado (RNF03). */
    private static ColaboradorResponse paraResponse(ColaboradorEntity c, Solicitante solicitante) {
        String email = solicitante.isAdmin() ? c.getEmail() : MascaraEmail.mascarar(c.getEmail());
        return new ColaboradorResponse(c.getId(), c.getNome(), email, c.getCargo(), c.getArea(),
                c.getAnosExperiencia(), c.getCursosConcluidos(), c.getMediaNotas());
    }
}
