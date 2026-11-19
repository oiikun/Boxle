package br.senac.boxle.resources;

import br.senac.boxle.entities.Box;
import br.senac.boxle.services.BoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Resource é o Controller do MVC. Recebe as requisições do front em /boxes e chama o BoxService
@CrossOrigin(origins = "*", maxAge = 33600)
@RestController
@RequestMapping(value = "/boxes")
public class BoxResource {

    @Autowired
    private BoxService boxService;

    // GET /boxes lista todos
    @GetMapping
    public ResponseEntity<List<Box>> findAll() {
        return ResponseEntity.ok().body(boxService.findAll());
    }

    // GET /boxes/id busca um pelo id
    @GetMapping(value = "/{id}")
    public ResponseEntity<Box> findById(@PathVariable Integer id) {
        return ResponseEntity.ok().body(boxService.findById(id));
    }

    // POST /boxes cadastra e responde 201
    @PostMapping
    public ResponseEntity<Box> gravar(@RequestBody Box box) {
        box = boxService.gravar(box);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(box.getId()).toUri();
        return ResponseEntity.created(uri).body(box);
    }

    // PUT /boxes/id altera
    @PutMapping(value = "/{id}")
    public ResponseEntity<Box> atualizar(@PathVariable Integer id, @RequestBody Box box) {
        return ResponseEntity.ok().body(boxService.atualizar(id, box));
    }

    // PUT /boxes/id/liberar deixa o box livre antes do fim do prazo, quando a empresa já tirou tudo
    @PutMapping(value = "/{id}/liberar")
    public ResponseEntity<Box> liberar(@PathVariable Integer id) {
        return ResponseEntity.ok().body(boxService.liberar(id));
    }

    // DELETE /boxes/id apaga e responde 204
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> apagar(@PathVariable Integer id) {
        boxService.apagar(id);
        return ResponseEntity.noContent().build();
    }
}
