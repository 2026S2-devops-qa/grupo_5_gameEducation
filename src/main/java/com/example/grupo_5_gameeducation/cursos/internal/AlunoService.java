package com.example.grupo_5_gameeducation.cursos.internal;

import com.example.grupo_5_gameeducation.cursos.api.AlunoResponse;
import com.example.grupo_5_gameeducation.cursos.api.ConcluirCursoRequest;
import com.example.grupo_5_gameeducation.cursos.api.CriarAlunoRequest;
import com.example.grupo_5_gameeducation.domain.Aluno;
import com.example.grupo_5_gameeducation.domain.Curso;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public AlunoResponse criar(CriarAlunoRequest request) {
        AlunoEntity entity = new AlunoEntity(request.nome(), request.plano());
        return AlunoResponse.from(alunoRepository.save(entity));
    }

    public List<AlunoResponse> listar() {
        return alunoRepository.findAll().stream().map(AlunoResponse::from).toList();
    }

    public AlunoResponse buscarPorId(Long id) {
        return AlunoResponse.from(buscarEntidade(id));
    }

    public AlunoResponse concluirCurso(Long id, ConcluirCursoRequest request) {
        AlunoEntity entity = buscarEntidade(id);
        Aluno aluno = entity.paraDominio();
        aluno.concluir(new Curso(request.tituloCurso()), request.media());
        entity.atualizarEstado(aluno);
        return AlunoResponse.from(alunoRepository.save(entity));
    }

    private AlunoEntity buscarEntidade(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new AlunoNaoEncontradoException(id));
    }
}
