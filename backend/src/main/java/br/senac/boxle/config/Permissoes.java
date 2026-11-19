package br.senac.boxle.config;

import br.senac.boxle.entities.Usuario;
import br.senac.boxle.repositories.UsuarioRepository;
import br.senac.boxle.util.Sessoes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

// Confere, antes de cada chamada da API, se a pessoa está logada e se o perfil dela pode fazer aquilo.
//
// ATENDENTE (balcão): vê tudo, cadastra e edita clientes, faz contratos, registra pagamentos,
//                     encerra contratos e libera box desocupado.
// ADMIN: tudo do atendente e mais as decisões do negócio: cria, edita e apaga boxes (preço e tamanho),
//        muda o valor de um contrato (desconto), apaga clientes e cuida dos usuários.
@Component
public class Permissoes implements HandlerInterceptor {

    @Autowired
    private Sessoes sessoes;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String metodo = request.getMethod();
        String caminho = request.getRequestURI();
        if (metodo.equals("OPTIONS") || caminho.equals("/usuarios/login") || caminho.equals("/usuarios/sair")) {
            return true;
        }
        Usuario usuario = usuarioLogado(request);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sua sessão acabou. Entre no sistema de novo");
        }
        if (soAdmin(metodo, caminho) && !"ADMIN".equals(usuario.getPerfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só o administrador pode fazer isso");
        }
        // Deixa o usuário disponível para as regras que dependem do perfil (ex.: desconto no contrato)
        request.setAttribute("usuarioLogado", usuario);
        return true;
    }

    private Usuario usuarioLogado(HttpServletRequest request) {
        String autorizacao = request.getHeader("Authorization");
        if (autorizacao == null || !autorizacao.startsWith("Bearer ")) {
            return null;
        }
        Integer id = sessoes.usuarioDo(autorizacao.substring(7));
        if (id == null) {
            return null;
        }
        return usuarioRepository.findById(id).orElse(null);
    }

    private boolean soAdmin(String metodo, String caminho) {
        boolean leitura = metodo.equals("GET");
        // Usuários: tudo é do administrador
        if (caminho.startsWith("/usuarios")) {
            return true;
        }
        // Boxes: ver e liberar box desocupado é do balcão. Criar, alterar e apagar é do administrador
        if (caminho.startsWith("/boxes")) {
            return !leitura && !caminho.endsWith("/liberar");
        }
        // Clientes: só apagar é do administrador
        if (caminho.startsWith("/clientes")) {
            return metodo.equals("DELETE");
        }
        // Contratos: mudar o valor (PUT /contratos/id) é do administrador.
        // Criar, pagar e encerrar são do balcão
        if (caminho.startsWith("/contratos")) {
            return metodo.equals("PUT");
        }
        return false;
    }
}
