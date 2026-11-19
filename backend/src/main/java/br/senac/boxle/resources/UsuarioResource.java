package br.senac.boxle.resources;

import br.senac.boxle.entities.Usuario;
import br.senac.boxle.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Resource é o Controller do MVC. Recebe as requisições do front em /usuarios e chama o UsuarioService
@CrossOrigin(origins = "*", maxAge = 33600)
@RestController
@RequestMapping(value = "/usuarios")
public class UsuarioResource {

    @Autowired
    private UsuarioService usuarioService;

    // POST /usuarios/login confere e-mail e senha na hora de entrar
    @PostMapping(value = "/login")
    public ResponseEntity<Usuario> login(@RequestBody Usuario usuario) {
        return ResponseEntity.ok().body(usuarioService.login(usuario));
    }

    // POST /usuarios/sair fecha a sessão de quem está logado
    @PostMapping(value = "/sair")
    public ResponseEntity<Void> sair(@RequestHeader(value = "Authorization", required = false) String autorizacao) {
        if (autorizacao != null && autorizacao.startsWith("Bearer ")) {
            usuarioService.sair(autorizacao.substring(7));
        }
        return ResponseEntity.noContent().build();
    }

    // GET /usuarios lista todos
    @GetMapping
    public ResponseEntity<List<Usuario>> findAll() {
        return ResponseEntity.ok().body(usuarioService.findAll());
    }

    // GET /usuarios/id busca um pelo id
    @GetMapping(value = "/{id}")
    public ResponseEntity<Usuario> findById(@PathVariable Integer id) {
        return ResponseEntity.ok().body(usuarioService.findById(id));
    }

    // POST /usuarios cadastra e responde 201
    @PostMapping
    public ResponseEntity<Usuario> gravar(@RequestBody Usuario usuario) {
        usuario = usuarioService.gravar(usuario);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(uri).body(usuario);
    }

    // PUT /usuarios/id altera
    @PutMapping(value = "/{id}")
    public ResponseEntity<Usuario> atualizar(@PathVariable Integer id, @RequestBody Usuario usuario) {
        return ResponseEntity.ok().body(usuarioService.atualizar(id, usuario));
    }

    // DELETE /usuarios/id apaga e responde 204
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> apagar(@PathVariable Integer id) {
        usuarioService.apagar(id);
        return ResponseEntity.noContent().build();
    }
}
