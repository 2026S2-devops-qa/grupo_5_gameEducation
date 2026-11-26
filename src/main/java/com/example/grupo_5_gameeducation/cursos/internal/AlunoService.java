package com.example.grupo_5_gameeducation.cursos.internal;

import com.example.grupo_5_gameeducation.catalogo.api.CatalogoApi;
import com.example.grupo_5_gameeducation.catalogo.api.CursoResumo;
import com.example.grupo_5_gameeducation.comum.erro.RequisicaoInvalidaException;
import com.example.grupo_5_gameeducation.cursos.api.AlunoResponse;
import com.example.grupo_5_gameeducation.cursos.api.ConcluirCursoRequest;
import com.example.grupo_5_gameeducation.cursos.api.CriarAlunoRequest;
import com.example.grupo_5_gameeducation.cursos.api.CursosApi;
import com.example.grupo_5_gameeducation.domain.Aluno;
import com.example.grupo_5_gameeducation.domain.Curso;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servico de aplicacao da US1: implementa o contrato {@link CursosApi}.
 *
 * Usa o modulo catalogo so pelo contrato publico {@link CatalogoApi} (nunca pelo
 * catalogo.internal): e o exemplo de contrato entre modulos do monolito modular.
 */
@Service
@Transactional(readOnly = true)
public class AlunoService implements CursosApi {

    private final AlunoRepository alunoRepository;
    private final CatalogoApi catalogo;

    public AlunoService(AlunoRepository alunoRepository, CatalogoApi catalogo) {
        this.alunoRepository = alunoRepository;
        this.catalogo = catalogo;
    }

    @Override
    @Transactional
    public AlunoResponse criar(CriarAlunoRequest request) {
        AlunoEntity entity = new AlunoEntity(request.nome().trim(), request.plano());
        return AlunoMapper.paraResponse(alunoRepository.save(entity));
    }

    @Override
    public List<AlunoResponse> listar() {
        return alunoRepository.findAll().stream().map(AlunoMapper::paraResponse).toList();
    }

    @Override
    public AlunoResponse buscarPorId(Long id) {
        return AlunoMapper.paraResponse(buscarEntidade(id));
    }

    @Override
    @Transactional
    public AlunoResponse concluirCurso(Long id, ConcluirCursoRequest request) {
        AlunoEntity entity = buscarEntidade(id);
        // So aceita curso que existe no catalogo. Usa o titulo oficial do catalogo, assim
        // "docker na pratica" e "Docker na Prática" contam como o mesmo curso na guarda da US1.
        CursoResumo curso = catalogo.buscarPorTitulo(request.tituloCurso())
                .orElseThrow(() -> new RequisicaoInvalidaException(
                        "Curso '" + request.tituloCurso().trim() + "' nao existe no catalogo."));
        Aluno aluno = entity.paraDominio();
        aluno.concluir(new Curso(curso.titulo()), request.media());
        entity.atualizarEstado(aluno);
        return AlunoMapper.paraResponse(alunoRepository.save(entity));
    }

    private AlunoEntity buscarEntidade(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new AlunoNaoEncontradoException(id));
    }
}
