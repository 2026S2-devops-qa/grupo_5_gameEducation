package com.example.aiservice.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Acao proposta pelo agente de IA (RF04). Regra central: NENHUMA acao muda nada antes de o usuario
 * autorizar. O agente so consegue criar a acao em AGUARDANDO_AUTORIZACAO; executar ou cancelar
 * depende de {@link #autorizar} ou {@link #negar}, chamados pelo dono da acao (ou por um ADMIN).
 * Quem decidiu e quando fica registrado (autorizadoPor, autorizadoEm) para auditoria.
 */
public class AcaoAgente {

    public static final int DIAS_MINIMO = 1;
    public static final int DIAS_MAXIMO = 180;
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private Long id;
    private final TipoAcao tipo;
    private StatusAcao status;
    private final Long colaboradorId;
    private final String solicitadoPor;
    private final String descricao;
    private final String motivo;
    private final Long cursoId;
    private final Integer dias;
    private final Instant criadoEm;
    private String autorizadoPor;
    private Instant autorizadoEm;
    private String resultado;

    private AcaoAgente(Long id, TipoAcao tipo, StatusAcao status, Long colaboradorId, String solicitadoPor,
                       String descricao, String motivo, Long cursoId, Integer dias, Instant criadoEm,
                       String autorizadoPor, Instant autorizadoEm, String resultado) {
        this.id = id;
        this.tipo = Objects.requireNonNull(tipo, "tipo");
        this.status = Objects.requireNonNull(status, "status");
        this.colaboradorId = colaboradorId;
        this.solicitadoPor = Objects.requireNonNull(solicitadoPor, "solicitadoPor");
        this.descricao = descricao;
        this.motivo = motivo;
        this.cursoId = cursoId;
        this.dias = dias;
        this.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm");
        this.autorizadoPor = autorizadoPor;
        this.autorizadoEm = autorizadoEm;
        this.resultado = resultado;
    }

    /** Proposta de inscricao num curso. Sempre nasce aguardando autorizacao. */
    public static AcaoAgente proporInscricao(Solicitante solicitante, CursoCatalogo curso, String motivo, Instant agora) {
        exigirColaborador(solicitante);
        Objects.requireNonNull(curso, "curso");
        return new AcaoAgente(null, TipoAcao.INSCRICAO, StatusAcao.AGUARDANDO_AUTORIZACAO,
                solicitante.colaboradorId(), solicitante.login(),
                "Inscrever no curso " + curso.titulo(), motivo, curso.id(), null, agora,
                null, null, null);
    }

    /** Proposta de acompanhamento daqui a {@code dias} dias. Sempre nasce aguardando autorizacao. */
    public static AcaoAgente proporAcompanhamento(Solicitante solicitante, int dias, String motivo, Instant agora) {
        exigirColaborador(solicitante);
        if (dias < DIAS_MINIMO || dias > DIAS_MAXIMO) {
            throw new IllegalArgumentException(
                    "O acompanhamento deve ser entre " + DIAS_MINIMO + " e " + DIAS_MAXIMO + " dias.");
        }
        return new AcaoAgente(null, TipoAcao.ACOMPANHAMENTO, StatusAcao.AGUARDANDO_AUTORIZACAO,
                solicitante.colaboradorId(), solicitante.login(),
                "Agendar acompanhamento em " + dias + " dias", motivo, null, dias, agora,
                null, null, null);
    }

    /** Reconstroi uma acao ja salva (usado so pelo adaptador de persistencia). */
    public static AcaoAgente restaurar(Long id, TipoAcao tipo, StatusAcao status, Long colaboradorId,
                                       String solicitadoPor, String descricao, String motivo, Long cursoId,
                                       Integer dias, Instant criadoEm, String autorizadoPor,
                                       Instant autorizadoEm, String resultado) {
        return new AcaoAgente(id, tipo, status, colaboradorId, solicitadoPor, descricao, motivo, cursoId,
                dias, criadoEm, autorizadoPor, autorizadoEm, resultado);
    }

    /** So o dono da acao ou um ADMIN podem ve-la e decidi-la. */
    public boolean podeSerDecididaPor(Solicitante solicitante) {
        return solicitante.admin() || solicitadoPor.equals(solicitante.login());
    }

    /** Executa a acao: so o dono (ou ADMIN), e so se ainda estiver aguardando. */
    public void autorizar(Solicitante solicitante, Instant agora) {
        decidir(solicitante, agora);
        status = StatusAcao.EXECUTADA;
        resultado = switch (tipo) {
            case INSCRICAO -> "Registrado no plano de estudos: " + descricao.replace("Inscrever no curso ", "") + ".";
            case ACOMPANHAMENTO -> "Lembrete de acompanhamento agendado para "
                    + LocalDate.ofInstant(agora.plus(dias, ChronoUnit.DAYS), FUSO) + ".";
        };
    }

    /** Cancela a acao sem executar nada: so o dono (ou ADMIN), e so se ainda estiver aguardando. */
    public void negar(Solicitante solicitante, Instant agora) {
        decidir(solicitante, agora);
        status = StatusAcao.CANCELADA;
        resultado = "Negada pelo usuário. Nada foi executado.";
    }

    private void decidir(Solicitante solicitante, Instant agora) {
        if (!podeSerDecididaPor(solicitante)) {
            throw new AcessoNegadoException("Você só pode autorizar ou negar as suas próprias ações.");
        }
        if (status != StatusAcao.AGUARDANDO_AUTORIZACAO) {
            throw new AcaoJaDecididaException("Esta ação já foi decidida (" + status + ").");
        }
        autorizadoPor = solicitante.login();
        autorizadoEm = Objects.requireNonNull(agora, "agora");
    }

    private static void exigirColaborador(Solicitante solicitante) {
        if (solicitante.colaboradorId() == null) {
            throw new IllegalArgumentException("Ações do agente são para colaboradores: este usuário não tem colaborador ligado.");
        }
    }

    public void definirId(Long id) {
        this.id = id;
    }

    public Long getId() { return id; }
    public TipoAcao getTipo() { return tipo; }
    public StatusAcao getStatus() { return status; }
    public Long getColaboradorId() { return colaboradorId; }
    public String getSolicitadoPor() { return solicitadoPor; }
    public String getDescricao() { return descricao; }
    public String getMotivo() { return motivo; }
    public Long getCursoId() { return cursoId; }
    public Integer getDias() { return dias; }
    public Instant getCriadoEm() { return criadoEm; }
    public String getAutorizadoPor() { return autorizadoPor; }
    public Instant getAutorizadoEm() { return autorizadoEm; }
    public String getResultado() { return resultado; }
}
