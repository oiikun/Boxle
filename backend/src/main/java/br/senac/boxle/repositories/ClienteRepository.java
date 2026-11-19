package br.senac.boxle.repositories;

import br.senac.boxle.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repository acessa o banco. O Spring Data cria as consultas a partir do nome dos métodos
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    List<Cliente> findAllByOrderByNomeAsc();

    Optional<Cliente> findByCnpj(String cnpj);
}
