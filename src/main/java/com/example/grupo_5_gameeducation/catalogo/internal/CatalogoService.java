package com.example.grupo_5_gameeducation.catalogo.internal;

import com.example.grupo_5_gameeducation.catalogo.api.Area;
import com.example.grupo_5_gameeducation.catalogo.api.CatalogoApi;
import com.example.grupo_5_gameeducation.catalogo.api.CriarCursoRequest;
import com.example.grupo_5_gameeducation.catalogo.api.CursoResumo;
import com.example.grupo_5_gameeducation.comum.erro.AcessoNegadoException;
import com.example.grupo_5_gameeducation.comum.erro.ConflitoException;
import com.example.grupo_5_gameeducation.comum.erro.NaoEncontradoException;
import com.example.grupo_5_gameeducation.comum.identidade.Solicitante;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Servico de aplicacao do catalogo: implementa o contrato publico {@link CatalogoApi}. */
@Service
@Transactional(readOnly = true)
public class CatalogoService implements CatalogoApi {

    private final CursoCatalogoRepository repository;

    public CatalogoService(CursoCatalogoRepository repository) {
        this.repository = repository;
    }

    @Override
    public CursoResumo buscarPorId(Long id) {
        return repository.findById(id)
                .map(CursoCatalogoEntity::paraResumo)
                .orElseThrow(() -> new NaoEncontradoException("Curso nao encontrado: " + id));
    }

    @Override
    public List<CursoResumo> listar(Area area) {
        List<CursoCatalogoEntity> cursos = area == null
                ? repository.findAllByOrderByIdAsc()
                : repository.findByAreaOrderByIdAsc(area);
        return cursos.stream().map(CursoCatalogoEntity::paraResumo).toList();
    }

    @Override
    public boolean existe(String titulo) {
        return titulo != null && repository.existsByTituloIgnoreCase(titulo.trim());
    }

    @Override
    public Optional<CursoResumo> buscarPorTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return Optional.empty();
        }
        return repository.findFirstByTituloIgnoreCase(titulo.trim()).map(CursoCatalogoEntity::paraResumo);
    }

    @Override
    @Transactional
    public CursoResumo cadastrar(CriarCursoRequest request, Solicitante solicitante) {
        // Defesa em profundidade: o gateway ja barra, mas o Core confere de novo.
        if (!solicitante.isAdmin()) {
            throw new AcessoNegadoException("Apenas ADMIN pode cadastrar cursos.");
        }
        String titulo = request.titulo().trim();
        if (existe(titulo)) {
            throw new ConflitoException("Ja existe um curso com o titulo '" + titulo + "'.");
        }
        CursoCatalogoEntity novo = new CursoCatalogoEntity(
                titulo, request.area(), request.nivel(), request.cargaHoraria());
        return repository.save(novo).paraResumo();
    }
}
