package br.senac.boxle.repositories;

import br.senac.boxle.entities.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository acessa o banco. O Spring Data cria as consultas a partir do nome dos métodos
@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Integer> {

    List<Contrato> findAllByOrderByIdDesc();

    // Usados para não deixar apagar box ou empresa que tem contrato ativo
    boolean existsByBoxIdAndStatus(Integer boxId, String status);

    boolean existsByClienteIdAndStatus(Integer clienteId, String status);

    // Histórico (contratos encerrados) que sai junto quando o box ou a empresa é apagada
    List<Contrato> findByBoxId(Integer boxId);

    List<Contrato> findByClienteId(Integer clienteId);

    List<Contrato> findByBoxIdAndStatus(Integer boxId, String status);

    List<Contrato> findByStatus(String status);

    // Contratos criados antes de existir status, vencimento e parcelas
    List<Contrato> findByStatusIsNull();
}
