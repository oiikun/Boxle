package br.senac.boxle.resources;

import br.senac.boxle.entities.Cliente;
import br.senac.boxle.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Resource é o Controller do MVC. Recebe as requisições do front em /clientes e chama o ClienteService
@CrossOrigin(origins = "*", maxAge = 33600)
@RestController
@RequestMapping(value = "/clientes")
public class ClienteResource {

    @Autowired
    private ClienteService clienteService;

    // GET /clientes lista todos
    @GetMapping
    public ResponseEntity<List<Cliente>> findAll() {
        return ResponseEntity.ok().body(clienteService.findAll());
    }

    // GET /clientes/id busca um pelo id
    @GetMapping(value = "/{id}")
    public ResponseEntity<Cliente> findById(@PathVariable Integer id) {
        return ResponseEntity.ok().body(clienteService.findById(id));
    }

    // POST /clientes cadastra e responde 201
    @PostMapping
    public ResponseEntity<Cliente> gravar(@RequestBody Cliente cliente) {
        cliente = clienteService.gravar(cliente);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(cliente.getId()).toUri();
        return ResponseEntity.created(uri).body(cliente);
    }

    // PUT /clientes/id altera
    @PutMapping(value = "/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable Integer id, @RequestBody Cliente cliente) {
        return ResponseEntity.ok().body(clienteService.atualizar(id, cliente));
    }

    // DELETE /clientes/id apaga e responde 204
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> apagar(@PathVariable Integer id) {
        clienteService.apagar(id);
        return ResponseEntity.noContent().build();
    }
}
