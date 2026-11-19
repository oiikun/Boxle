package br.senac.boxle.services;

import br.senac.boxle.entities.Usuario;
import br.senac.boxle.repositories.UsuarioRepository;
import br.senac.boxle.util.Documentos;
import br.senac.boxle.util.Sessoes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

// Regras dos usuários e do login
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private Sessoes sessoes;

    public List<Usuario> findAll() {
        return usuarioRepository.findAllByOrderByNomeAsc();
    }

    public Usuario findById(Integer id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.orElse(null) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }
        return usuario.get();
    }

    // Confere e-mail e senha. Se não bater, responde 401. Se bater, abre uma sessão e devolve a chave dela
    public Usuario login(Usuario dados) {
        String email = "";
        if (dados.getEmail() != null) {
            email = dados.getEmail().trim().toLowerCase();
        }
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null || !usuario.getSenha().equals(dados.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
        }
        usuario.setToken(sessoes.abrir(usuario.getId()));
        return usuario;
    }

    public void sair(String token) {
        sessoes.fechar(token);
    }

    public Usuario gravar(Usuario usuario) {
        usuario.setId(null);
        validar(usuario);
        if (usuario.getSenha() == null || usuario.getSenha().length() < 4) {
            throw erro("A senha precisa ter pelo menos 4 caracteres");
        }
        return usuarioRepository.save(usuario);
    }

    // Senha em branco na alteração mantém a senha antiga
    public Usuario atualizar(Integer id, Usuario dados) {
        Usuario usuario = findById(id);
        dados.setId(id);
        validar(dados);
        if (usuario.getPerfil().equals("ADMIN") && !dados.getPerfil().equals("ADMIN") && ultimoAdmin()) {
            throw erro("É preciso manter pelo menos um administrador");
        }
        if (dados.getSenha() != null && !dados.getSenha().isEmpty() && dados.getSenha().length() < 4) {
            throw erro("A senha precisa ter pelo menos 4 caracteres");
        }
        usuario.setNome(dados.getNome());
        usuario.setEmail(dados.getEmail());
        usuario.setPerfil(dados.getPerfil());
        usuario.setCpf(dados.getCpf());
        if (dados.getSenha() != null && !dados.getSenha().isEmpty()) {
            usuario.setSenha(dados.getSenha());
        }
        return usuarioRepository.save(usuario);
    }

    public void apagar(Integer id) {
        Usuario usuario = findById(id);
        if (usuario.getPerfil().equals("ADMIN") && ultimoAdmin()) {
            throw erro("É preciso manter pelo menos um administrador");
        }
        usuarioRepository.deleteById(id);
        sessoes.fecharDoUsuario(id);
    }

    // Sempre precisa sobrar pelo menos um administrador
    private boolean ultimoAdmin() {
        int admins = 0;
        for (Usuario u : usuarioRepository.findAll()) {
            if (u.getPerfil().equals("ADMIN")) {
                admins++;
            }
        }
        return admins == 1;
    }

    private void validar(Usuario usuario) {
        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw erro("Informe o nome do usuário");
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw erro("Informe o e-mail do usuário");
        }
        usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        if (!Documentos.emailValido(usuario.getEmail())) {
            throw erro("Informe um e-mail válido");
        }
        String perfil = usuario.getPerfil();
        if (perfil == null || !(perfil.equals("ADMIN") || perfil.equals("ATENDENTE"))) {
            throw erro("Informe o perfil do usuário");
        }
        Usuario mesmoEmail = usuarioRepository.findByEmail(usuario.getEmail()).orElse(null);
        if (mesmoEmail != null && !mesmoEmail.getId().equals(usuario.getId())) {
            throw erro("Já existe um usuário com o e-mail " + usuario.getEmail());
        }
        // CPF identifica o funcionário. Confere pelos dígitos verificadores e não deixa repetir
        usuario.setCpf(Documentos.limpar(usuario.getCpf()));
        if (usuario.getCpf() == null || usuario.getCpf().isEmpty()) {
            throw erro("Informe o CPF do funcionário");
        }
        if (!Documentos.cpfValido(usuario.getCpf())) {
            throw erro("CPF inválido");
        }
        Usuario mesmoCpf = usuarioRepository.findByCpf(usuario.getCpf()).orElse(null);
        if (mesmoCpf != null && !mesmoCpf.getId().equals(usuario.getId())) {
            throw erro("Já existe um usuário com este CPF (" + mesmoCpf.getNome() + ")");
        }
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
