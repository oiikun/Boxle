package br.senac.boxle.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Liga a conferência de login e perfil em todas as rotas da API. As telas (arquivos do Angular) ficam de fora
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private Permissoes permissoes;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissoes)
                .addPathPatterns("/boxes", "/boxes/**", "/clientes", "/clientes/**",
                        "/contratos", "/contratos/**", "/usuarios", "/usuarios/**");
    }
}
