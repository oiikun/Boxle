package br.senac.boxle.repositories;

import br.senac.boxle.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repository acessa o banco. O Spring Data cria as consultas a partir do nome dos métodos
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    List<Usuario> findAllByOrderByNomeAsc();

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByCpf(String cpf);
}
