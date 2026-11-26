package com.example.grupo_5_gameeducation.colaboradores.api;

import com.example.grupo_5_gameeducation.comum.identidade.Solicitante;
import java.util.List;

/**
 * Contrato publico do modulo colaboradores (porta de entrada da arquitetura hexagonal).
 * Outros modulos usam SO esta interface, nunca o colaboradores.internal (o ArchUnit garante).
 */
public interface ColaboradoresApi {

    /**
     * Perfil profissional para outros modulos (ex.: competencias no RF02). Nao aplica regra de
     * acesso: quem chama decide quem pode ver (use {@link Solicitante#podeAcessarColaborador(Long)}).
     *
     * @throws com.example.grupo_5_gameeducation.comum.erro.NaoEncontradoException se nao existir (404)
     */
    PerfilColaborador buscarPerfil(Long id);

    /**
     * Dados do colaborador para a tela. So ADMIN ou o proprio colaborador. Email mascarado
     * para quem nao e ADMIN.
     *
     * @throws com.example.grupo_5_gameeducation.comum.erro.AcessoNegadoException outro colaborador (403)
     * @throws com.example.grupo_5_gameeducation.comum.erro.NaoEncontradoException se nao existir (404)
     */
    ColaboradorResponse buscar(Long id, Solicitante solicitante);

    /** Lista todos. So ADMIN (403 para os demais). */
    List<ColaboradorResponse> listar(Solicitante solicitante);

    /** Cadastra. So ADMIN (403). Email repetido da 409. */
    ColaboradorResponse cadastrar(CriarColaboradorRequest request, Solicitante solicitante);
}
