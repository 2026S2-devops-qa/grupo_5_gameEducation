package com.example.aiservice.adapters.out.persistence;

import com.example.aiservice.domain.AcaoAgente;
import com.example.aiservice.domain.StatusAcao;
import com.example.aiservice.domain.TipoAcao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/** Tabela das acoes do agente (H2). Converte de e para o dominio, que nao conhece JPA. */
@Entity
@Table(name = "acao_agente", indexes = @Index(name = "idx_acao_solicitado_por", columnList = "solicitadoPor"))
class AcaoAgenteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAcao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusAcao status;

    private Long colaboradorId;

    @Column(nullable = false, length = 100)
    private String solicitadoPor;

    @Column(length = 300)
    private String descricao;

    @Column(length = 1000)
    private String motivo;

    private Long cursoId;
    private Integer dias;

    @Column(nullable = false)
    private Instant criadoEm;

    @Column(length = 100)
    private String autorizadoPor;

    private Instant autorizadoEm;

    @Column(length = 500)
    private String resultado;

    protected AcaoAgenteEntity() {
    }

    static AcaoAgenteEntity de(AcaoAgente acao) {
        AcaoAgenteEntity e = new AcaoAgenteEntity();
        e.id = acao.getId();
        e.tipo = acao.getTipo();
        e.status = acao.getStatus();
        e.colaboradorId = acao.getColaboradorId();
        e.solicitadoPor = acao.getSolicitadoPor();
        e.descricao = acao.getDescricao();
        e.motivo = limitar(acao.getMotivo(), 1000);
        e.cursoId = acao.getCursoId();
        e.dias = acao.getDias();
        e.criadoEm = acao.getCriadoEm();
        e.autorizadoPor = acao.getAutorizadoPor();
        e.autorizadoEm = acao.getAutorizadoEm();
        e.resultado = acao.getResultado();
        return e;
    }

    AcaoAgente paraDominio() {
        return AcaoAgente.restaurar(id, tipo, status, colaboradorId, solicitadoPor, descricao, motivo,
                cursoId, dias, criadoEm, autorizadoPor, autorizadoEm, resultado);
    }

    private static String limitar(String texto, int tamanho) {
        return texto == null || texto.length() <= tamanho ? texto : texto.substring(0, tamanho);
    }
}
