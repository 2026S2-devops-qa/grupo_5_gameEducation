package com.example.grupo_5_gameeducation.comum.identidade;

import java.util.List;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Permite que qualquer controller receba um parametro {@link Solicitante} ja montado a partir
 * dos headers X-User-* do gateway, sem repetir @RequestHeader em cada metodo.
 */
@Configuration
public class SolicitanteConfig implements WebMvcConfigurer {

    static {
        // No Swagger o Solicitante nao aparece como parametro: quem preenche e o gateway.
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(Solicitante.class);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new SolicitanteResolver());
    }

    static final class SolicitanteResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return Solicitante.class.equals(parameter.getParameterType());
        }

        @Override
        public Solicitante resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                NativeWebRequest request, WebDataBinderFactory binderFactory) {
            return Solicitante.dosHeaders(
                    request.getHeader(Solicitante.HEADER_USER_ID),
                    request.getHeader(Solicitante.HEADER_ROLES),
                    request.getHeader(Solicitante.HEADER_COLABORADOR_ID));
        }
    }
}
