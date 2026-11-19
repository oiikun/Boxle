package br.senac.boxle.resources;

import br.senac.boxle.entities.Contrato;
import br.senac.boxle.entities.Usuario;
import br.senac.boxle.services.ContratoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Resource é o Controller do MVC. Recebe as requisições do front em /contratos e chama o ContratoService
@CrossOrigin(origins = "*", maxAge = 33600)
@RestController
@RequestMapping(value = "/contratos")
public class ContratoResource {

    @Autowired
    private ContratoService contratoService;

    // GET /contratos lista todos
    @GetMapping
    public ResponseEntity<List<Contrato>> findAll() {
        return ResponseEntity.ok().body(contratoService.findAll());
    }

    // GET /contratos/id busca um pelo id
    @GetMapping(value = "/{id}")
    public ResponseEntity<Contrato> findById(@PathVariable Integer id) {
        return ResponseEntity.ok().body(contratoService.findById(id));
    }

    // POST /contratos cadastra e responde 201. O perfil de quem está logado decide se pode dar desconto
    @PostMapping
    public ResponseEntity<Contrato> gravar(@RequestBody Contrato contrato,
                                           @RequestAttribute(name = "usuarioLogado", required = false) Usuario usuario) {
        boolean admin = usuario != null && "ADMIN".equals(usuario.getPerfil());
        contrato = contratoService.gravar(contrato, admin);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(contrato.getId()).toUri();
        return ResponseEntity.created(uri).body(contrato);
    }

    // PUT /contratos/id altera
    @PutMapping(value = "/{id}")
    public ResponseEntity<Contrato> atualizar(@PathVariable Integer id, @RequestBody Contrato contrato) {
        return ResponseEntity.ok().body(contratoService.atualizar(id, contrato));
    }

    // POST /contratos/id/pagamento registra o pagamento de uma parcela
    @PostMapping(value = "/{id}/pagamento")
    public ResponseEntity<Contrato> registrarPagamento(@PathVariable Integer id) {
        return ResponseEntity.ok().body(contratoService.registrarPagamento(id));
    }

    // POST /contratos/id/encerrar encerra o contrato (fica no histórico) e abre o prazo de desocupação
    @PostMapping(value = "/{id}/encerrar")
    public ResponseEntity<Contrato> encerrar(@PathVariable Integer id) {
        return ResponseEntity.ok().body(contratoService.encerrar(id));
    }
}
