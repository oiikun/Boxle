package br.senac.boxle.services;

import br.senac.boxle.entities.Cliente;
import br.senac.boxle.entities.Contrato;
import br.senac.boxle.repositories.ClienteRepository;
import br.senac.boxle.repositories.ContratoRepository;
import br.senac.boxle.util.Documentos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

// Regras do cliente, que é a empresa. Documentos, endereço e CNPJ sem repetir
@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    public List<Cliente> findAll() {
        return clienteRepository.findAllByOrderByNomeAsc();
    }

    public Cliente findById(Integer id) {
        Optional<Cliente> cliente = clienteRepository.findById(id);
        if (cliente.orElse(null) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado");
        }
        return cliente.get();
    }

    public Cliente gravar(Cliente cliente) {
        cliente.setId(null);
        validar(cliente);
        return clienteRepository.save(cliente);
    }

    // O dia de pagamento é do cliente: se mudar, os contratos ativos dele passam a vencer no dia novo
    public Cliente atualizar(Integer id, Cliente dados) {
        findById(id);
        dados.setId(id);
        validar(dados);
        Cliente salvo = clienteRepository.save(dados);
        for (Contrato contrato : contratoRepository.findByClienteId(id)) {
            if ("ATIVO".equals(contrato.getStatus()) && !salvo.getDiaVencimento().equals(contrato.getDiaVencimento())) {
                contrato.setDiaVencimento(salvo.getDiaVencimento());
                contratoRepository.save(contrato);
            }
        }
        return salvo;
    }

    // Empresa com contrato ativo não pode ser apagada. Os contratos encerrados dela saem junto
    public void apagar(Integer id) {
        findById(id);
        if (contratoRepository.existsByClienteIdAndStatus(id, "ATIVO")) {
            throw erro("Este cliente tem contrato ativo e não pode ser apagado");
        }
        contratoRepository.deleteAll(contratoRepository.findByClienteId(id));
        clienteRepository.deleteById(id);
    }

    // Tira a máscara (pontos, traços e barra) e confere cada documento pelo dígito verificador
    private void validar(Cliente cliente) {
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw erro("Informe a razão social");
        }
        cliente.setCnpj(Documentos.limpar(cliente.getCnpj()));
        cliente.setIe(Documentos.limpar(cliente.getIe()));
        if (!Documentos.cnpjValido(cliente.getCnpj())) {
            throw erro("CNPJ inválido");
        }
        if (!Documentos.ieValida(cliente.getIe())) {
            throw erro("Inscrição estadual inválida");
        }
        // O mesmo CNPJ não pode ser cadastrado duas vezes
        Cliente mesmoCnpj = clienteRepository.findByCnpj(cliente.getCnpj()).orElse(null);
        if (mesmoCnpj != null && !mesmoCnpj.getId().equals(cliente.getId())) {
            throw erro("Já existe um cliente com este CNPJ");
        }
        // O CEP já foi conferido nos Correios pela tela. Aqui o back end confere o formato e os campos do endereço
        cliente.setCep(Documentos.limpar(cliente.getCep()));
        if (!Documentos.cepValido(cliente.getCep())) {
            throw erro("CEP inválido");
        }
        if (vazio(cliente.getLogradouro())) {
            throw erro("Informe a rua da empresa");
        }
        if (vazio(cliente.getNumero())) {
            throw erro("Informe o número do endereço");
        }
        if (vazio(cliente.getCidade()) || vazio(cliente.getUf())) {
            throw erro("Informe a cidade e o estado");
        }
        // Contato na empresa. Empresa não precisa de CPF nem RG, os documentos dela são CNPJ e IE
        if (cliente.getResponsavel() == null || cliente.getResponsavel().trim().isEmpty()) {
            throw erro("Informe o nome do contato na empresa");
        }
        if (!Documentos.telefoneValido(cliente.getTelefone())) {
            throw erro("Informe o telefone com DDD");
        }
        if (cliente.getEmail() != null) {
            cliente.setEmail(cliente.getEmail().trim().toLowerCase());
        }
        if (!Documentos.emailValido(cliente.getEmail())) {
            throw erro("Informe um e-mail válido");
        }
        // Dia de pagamento combinado com a empresa. Vai de 1 a 28 para existir em todos os meses
        if (cliente.getDiaVencimento() == null || cliente.getDiaVencimento() < 1 || cliente.getDiaVencimento() > 28) {
            throw erro("Escolha o dia de pagamento, de 1 a 28");
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
