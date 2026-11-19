package br.senac.boxle.repositories;

import br.senac.boxle.entities.Box;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repository acessa o banco. O Spring Data cria as consultas a partir do nome dos métodos
@Repository
public interface BoxRepository extends JpaRepository<Box, Integer> {

    // Ordena pela letra e depois pelo número, para o A10 vir depois do A9 e não depois do A1
    @Query("SELECT boxes FROM Boxes boxes "
            + "ORDER BY SUBSTRING(boxes.codigo, 1, 1), LENGTH(boxes.codigo), boxes.codigo")
    List<Box> listarEmOrdem();

    Optional<Box> findByCodigo(String codigo);
}
