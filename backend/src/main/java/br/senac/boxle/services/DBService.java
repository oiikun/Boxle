package br.senac.boxle.services;

import br.senac.boxle.entities.Box;
import br.senac.boxle.entities.Cliente;
import br.senac.boxle.entities.Contrato;
import br.senac.boxle.entities.Usuario;
import br.senac.boxle.repositories.BoxRepository;
import br.senac.boxle.repositories.ClienteRepository;
import br.senac.boxle.repositories.ContratoRepository;
import br.senac.boxle.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DBService {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private BoxRepository boxRepository;

    public void instanciarDB() {
        atualizarClientesAntigos();
        atualizarContratosAntigos();
        atualizarBoxesAntigos();
        // Cria o administrador e o atendente Luis só na primeira vez, quando o banco está vazio
        if (usuarioRepository.count() > 0) {
            return;
        }
        // CPFs de teste (passam no dígito verificador, mas não são de ninguém). Troque pelos de verdade
        usuarioService.gravar(new Usuario("Administrador", "admin@boxle.com", "admin", "ADMIN", "52998224725"));
        usuarioService.gravar(new Usuario("Luis", "luis@boxle.com", "luis", "ATENDENTE", "11144477735"));
    }

    // Clientes gravados antes de existir o dia de pagamento ficam com o dia 10.
    // Dá para trocar depois no cadastro do cliente
    private void atualizarClientesAntigos() {
        for (Cliente cliente : clienteRepository.findAll()) {
            if (cliente.getDiaVencimento() == null) {
                cliente.setDiaVencimento(10);
                clienteRepository.save(cliente);
            }
        }
    }

    // A fase de limpeza saiu: box que ficou em LIMPEZA volta a LIVRE
    private void atualizarBoxesAntigos() {
        for (Box box : boxRepository.findAll()) {
            if ("LIMPEZA".equals(box.getStatus())) {
                box.setStatus("LIVRE");
                boxRepository.save(box);
            }
        }
    }

    // Contratos gravados antes de existir status, vencimento e parcelas ficam ATIVOS,
    // vencendo no dia de pagamento do cliente e considerados em dia até hoje
    private void atualizarContratosAntigos() {
        for (Contrato contrato : contratoRepository.findByStatusIsNull()) {
            if (contrato.getDataInicio() == null) {
                contrato.setDataInicio(LocalDate.now());
            }
            contrato.setStatus("ATIVO");
            Integer diaDoCliente = contrato.getCliente() == null ? null : contrato.getCliente().getDiaVencimento();
            contrato.setDiaVencimento(diaDoCliente != null ? diaDoCliente
                    : Math.min(contrato.getDataInicio().getDayOfMonth(), 28));
            contrato.setParcelasPagas(Math.max(1, contrato.parcelasVencidasAte(LocalDate.now())));
            contratoRepository.save(contrato);
        }
    }
}
