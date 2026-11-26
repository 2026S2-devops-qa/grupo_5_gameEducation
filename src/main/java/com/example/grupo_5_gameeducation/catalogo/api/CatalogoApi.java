package com.example.grupo_5_gameeducation.catalogo.api;

import com.example.grupo_5_gameeducation.comum.identidade.Solicitante;
import java.util.List;
import java.util.Optional;

/**
 * Contrato publico do modulo catalogo (porta de entrada da arquitetura hexagonal).
 *
 * Outros modulos usam SO esta interface, nunca o catalogo.internal (o ArchUnit garante).
 * Exemplo real: o modulo cursos chama {@link #buscarPorTitulo(String)} antes de aplicar a
 * regra da US1, para so aceitar conclusao de curso que existe no catalogo.
 */
public interface CatalogoApi {

    /** @throws com.example.grupo_5_gameeducation.comum.erro.NaoEncontradoException se nao existir (404) */
    CursoResumo buscarPorId(Long id);

    /** Lista os cursos, filtrando pela area quando ela vier preenchida (null = todas). */
    List<CursoResumo> listar(Area area);

    /** true se ja existe curso com esse titulo (sem diferenciar maiusculas e minusculas). */
    boolean existe(String titulo);

    /** Curso com esse titulo (sem diferenciar maiusculas e minusculas), com o titulo oficial do catalogo. */
    Optional<CursoResumo> buscarPorTitulo(String titulo);

    /**
     * Cadastra um curso. So ADMIN.
     *
     * @throws com.example.grupo_5_gameeducation.comum.erro.AcessoNegadoException se nao for ADMIN (403)
     * @throws com.example.grupo_5_gameeducation.comum.erro.ConflitoException se o titulo ja existir (409)
     */
    CursoResumo cadastrar(CriarCursoRequest request, Solicitante solicitante);
}
