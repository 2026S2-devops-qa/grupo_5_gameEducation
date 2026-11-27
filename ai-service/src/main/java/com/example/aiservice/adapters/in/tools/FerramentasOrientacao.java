package com.example.aiservice.adapters.in.tools;

import com.example.aiservice.application.AcoesService;
import com.example.aiservice.application.port.BaseConhecimento;
import com.example.aiservice.application.port.CoreGateway;
import com.example.aiservice.application.port.FabricaDeFerramentas;
import com.example.aiservice.domain.AcaoAgente;
import com.example.aiservice.domain.CursoCatalogo;
import com.example.aiservice.domain.PerfilColaborador;
import com.example.aiservice.domain.Solicitante;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * Tools do agente de orientacao (adaptador de entrada: o LLM chama o nucleo por aqui).
 *
 * Uma instancia por requisicao, presa ao usuario logado. Nenhuma tool recebe colaboradorId:
 * o perfil consultado e as acoes propostas sao sempre do colaborador do header X-Colaborador-Id.
 *
 * - Leitura (o agente usa sozinho): consultarPerfil, listarCursos, buscarDocumentos.
 * - Acao (so PROPOE, o usuario autoriza depois): proporInscricao, proporAcompanhamento.
 */
public class FerramentasOrientacao implements FabricaDeFerramentas.Sessao {

    private static final Logger log = LoggerFactory.getLogger(FerramentasOrientacao.class);

    private final Solicitante solicitante;
    private final CoreGateway coreGateway;
    private final BaseConhecimento baseConhecimento;
    private final AcoesService acoesService;
    private final List<String> toolsUsadas = Collections.synchronizedList(new ArrayList<>());
    private final List<AcaoAgente> acoesPropostas = Collections.synchronizedList(new ArrayList<>());

    FerramentasOrientacao(Solicitante solicitante, CoreGateway coreGateway,
                          BaseConhecimento baseConhecimento, AcoesService acoesService) {
        this.solicitante = solicitante;
        this.coreGateway = coreGateway;
        this.baseConhecimento = baseConhecimento;
        this.acoesService = acoesService;
    }

    @Tool(description = "Consulta o perfil do colaborador logado: cargo, área, anos de experiência, "
            + "cursos concluídos e média de notas. Só existe acesso ao colaborador logado.")
    public PerfilColaborador consultarPerfil() {
        registrar("consultarPerfil", "");
        return coreGateway.buscarPerfil(solicitante, solicitante.colaboradorId());
    }

    @Tool(description = "Lista os cursos do catálogo da empresa, com id, título, área, nível e carga horária.")
    public List<CursoCatalogo> listarCursos(
            @ToolParam(description = "Área: DEVOPS, QA, DESENVOLVIMENTO, DADOS ou GESTAO. Vazio para todas.",
                    required = false) String area) {
        String filtro = area == null || area.isBlank() ? null : area.strip().toUpperCase();
        registrar("listarCursos", "area=" + filtro);
        return coreGateway.listarCursos(solicitante, filtro);
    }

    @Tool(description = "Busca trechos dos documentos institucionais (trilhas, matriz de competências, "
            + "política de desenvolvimento, catálogo, FAQ do RH) relacionados à pergunta.")
    public List<String> buscarDocumentos(@ToolParam(description = "O que procurar nos documentos") String pergunta) {
        registrar("buscarDocumentos", "pergunta=" + pergunta);
        if (!baseConhecimento.pronta()) {
            return List.of("A base de documentos não está disponível agora.");
        }
        return baseConhecimento.buscar(pergunta, 4).stream()
                .map(t -> "[" + t.fonte() + "] " + t.texto().strip())
                .toList();
    }

    @Tool(description = "PROPÕE inscrever o colaborador logado em um curso do catálogo. Não executa: "
            + "a ação fica aguardando a autorização do colaborador.")
    public String proporInscricao(
            @ToolParam(description = "id do curso, como devolvido por listarCursos") Long cursoId,
            @ToolParam(description = "por que este curso ajuda no objetivo do colaborador") String motivo) {
        registrar("proporInscricao", "cursoId=" + cursoId);
        AcaoAgente acao = acoesService.proporInscricao(solicitante, cursoId, motivo);
        acoesPropostas.add(acao);
        return "Ação " + acao.getId() + " proposta (" + acao.getDescricao()
                + "). Status: aguardando autorização do colaborador. Nada foi executado.";
    }

    @Tool(description = "PROPÕE agendar um acompanhamento do plano do colaborador logado daqui a alguns dias. "
            + "Não executa: a ação fica aguardando a autorização do colaborador.")
    public String proporAcompanhamento(
            @ToolParam(description = "daqui a quantos dias (1 a 180)") Integer dias,
            @ToolParam(description = "o que deve ser acompanhado") String motivo) {
        registrar("proporAcompanhamento", "dias=" + dias);
        AcaoAgente acao = acoesService.proporAcompanhamento(solicitante, dias == null ? 30 : dias, motivo);
        acoesPropostas.add(acao);
        return "Ação " + acao.getId() + " proposta (" + acao.getDescricao()
                + "). Status: aguardando autorização do colaborador. Nada foi executado.";
    }

    private void registrar(String tool, String argumentos) {
        toolsUsadas.add(tool);
        log.info("AGENTE tool={} usuario={} colaborador={} {}", tool, solicitante.login(),
                solicitante.colaboradorId(), argumentos);
    }

    @Override
    public Object ferramentas() {
        return this;
    }

    @Override
    public List<String> toolsUsadas() {
        return List.copyOf(toolsUsadas);
    }

    @Override
    public List<AcaoAgente> acoesPropostas() {
        return List.copyOf(acoesPropostas);
    }
}
