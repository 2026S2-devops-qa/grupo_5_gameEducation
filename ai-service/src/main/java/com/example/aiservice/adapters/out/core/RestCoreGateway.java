package com.example.aiservice.adapters.out.core;

import com.example.aiservice.application.CoreIndisponivelException;
import com.example.aiservice.application.NaoEncontradaException;
import com.example.aiservice.application.port.CoreGateway;
import com.example.aiservice.domain.AcessoNegadoException;
import com.example.aiservice.domain.CursoCatalogo;
import com.example.aiservice.domain.PerfilColaborador;
import com.example.aiservice.domain.Solicitante;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptador da porta {@link CoreGateway} que chama o Core real (RF01) pela rede interna (CORE_URL).
 *
 * Repassa os headers do usuario logado (X-User-Id, X-User-Roles, X-Colaborador-Id). Assim o Core
 * aplica as mesmas regras de acesso que aplicaria ao proprio usuario: o agente nunca ve dados que
 * o usuario nao poderia ver (RNF03). Ativo quando ia.core.stub=false.
 */
@Component
@ConditionalOnProperty(name = "ia.core.stub", havingValue = "false")
class RestCoreGateway implements CoreGateway {

    private final RestClient restClient;

    @Autowired
    RestCoreGateway(@Value("${ia.core.url}") String coreUrl) {
        this(RestClient.builder().requestFactory(fabricaComTimeout()), coreUrl);
    }

    RestCoreGateway(RestClient.Builder builder, String coreUrl) {
        this.restClient = builder.baseUrl(coreUrl).build();
    }

    @Override
    public PerfilColaborador buscarPerfil(Solicitante solicitante, long colaboradorId) {
        ColaboradorResponse resposta = chamar(() -> restClient.get()
                .uri("/api/colaboradores/{id}", colaboradorId)
                .headers(h -> identidade(h, solicitante))
                .retrieve()
                .body(ColaboradorResponse.class), "Colaborador " + colaboradorId + " não encontrado.");
        return new PerfilColaborador(resposta.id(), resposta.nome(), resposta.cargo(), resposta.area(),
                resposta.anosExperiencia(), resposta.cursosConcluidos(), resposta.mediaNotas());
    }

    @Override
    public List<CursoCatalogo> listarCursos(Solicitante solicitante, String area) {
        CursoResponse[] cursos = chamar(() -> restClient.get()
                .uri(u -> area == null ? u.path("/api/cursos").build() : u.path("/api/cursos").queryParam("area", area).build())
                .headers(h -> identidade(h, solicitante))
                .retrieve()
                .body(CursoResponse[].class), "Cursos não encontrados.");
        return cursos == null ? List.of() : Arrays.stream(cursos).map(CursoResponse::paraDominio).toList();
    }

    @Override
    public Optional<CursoCatalogo> buscarCurso(Solicitante solicitante, long cursoId) {
        try {
            CursoResponse curso = chamar(() -> restClient.get()
                    .uri("/api/cursos/{id}", cursoId)
                    .headers(h -> identidade(h, solicitante))
                    .retrieve()
                    .body(CursoResponse.class), "Curso " + cursoId + " não encontrado.");
            return Optional.ofNullable(curso).map(CursoResponse::paraDominio);
        } catch (NaoEncontradaException e) {
            return Optional.empty();
        }
    }

    /** Os mesmos headers que o gateway mandou para o ai-service. */
    static void identidade(HttpHeaders headers, Solicitante solicitante) {
        headers.set("X-User-Id", solicitante.login());
        headers.set("X-User-Roles", String.join(",", solicitante.papeis()));
        if (solicitante.colaboradorId() != null) {
            headers.set("X-Colaborador-Id", solicitante.colaboradorId().toString());
        }
    }

    private static <T> T chamar(Supplier<T> chamada, String mensagemNaoEncontrado) {
        try {
            return chamada.get();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new AcessoNegadoException("O Core negou o acesso: o usuário logado não pode ver esses dados.");
            }
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NaoEncontradaException(mensagemNaoEncontrado);
            }
            throw new CoreIndisponivelException("O Core respondeu com erro " + e.getStatusCode().value() + ".", e);
        } catch (RestClientException e) {
            throw new CoreIndisponivelException("Não consegui falar com o Core agora.", e);
        }
    }

    private static SimpleClientHttpRequestFactory fabricaComTimeout() {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(2));
        fabrica.setReadTimeout(Duration.ofSeconds(5));
        return fabrica;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ColaboradorResponse(Long id, String nome, String cargo, String area,
                               int anosExperiencia, int cursosConcluidos, double mediaNotas) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CursoResponse(Long id, String titulo, String area, String nivel, int cargaHoraria) {
        CursoCatalogo paraDominio() {
            return new CursoCatalogo(id, titulo, area, nivel, cargaHoraria);
        }
    }
}
