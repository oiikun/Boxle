package br.senac.boxle.services;

import br.senac.boxle.entities.Box;
import br.senac.boxle.entities.Cliente;
import br.senac.boxle.entities.Contrato;
import br.senac.boxle.repositories.BoxRepository;
import br.senac.boxle.repositories.ContratoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Regras do contrato. É aqui que o box passa de LIVRE para OCUPADO e, no fim do prazo de desocupação, volta a LIVRE
@Service
public class ContratoService {

    // Dias que a empresa tem para retirar as coisas do box depois de encerrar o contrato
    public static final int PRAZO_DESOCUPACAO_DIAS = 7;

    // O contrato pode começar no futuro (reserva), mas no máximo este tanto de dias depois de hoje
    public static final int INICIO_MAXIMO_DIAS = 60;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private BoxRepository boxRepository;

    @Autowired
    private BoxService boxService;

    @Autowired
    private ClienteService clienteService;

    public List<Contrato> findAll() {
        return contratoRepository.findAllByOrderByIdDesc();
    }

    public Contrato findById(Integer id) {
        Optional<Contrato> contrato = contratoRepository.findById(id);
        if (contrato.orElse(null) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato não encontrado");
        }
        return contrato.get();
    }

    // Só aluga box livre. Sem data, usa a de hoje. Sem valor, usa o preço do box.
    // O dia de pagamento vem do cadastro do cliente. Valor diferente do preço do box (desconto) só o administrador dá
    public Contrato gravar(Contrato contrato, boolean admin) {
        if (contrato.getBox() == null || contrato.getBox().getId() == null) {
            throw erro("Escolha o box do contrato");
        }
        if (contrato.getCliente() == null || contrato.getCliente().getId() == null) {
            throw erro("Escolha o cliente do contrato");
        }
        Box box = boxService.findById(contrato.getBox().getId());
        Cliente cliente = clienteService.findById(contrato.getCliente().getId());
        Contrato saindo = contratoRepository.findByBoxIdAndStatus(box.getId(), "ENCERRADO").stream()
                .filter(Contrato::isDesocupando).findFirst().orElse(null);
        if (saindo != null) {
            throw erro("O box " + box.getCodigo() + " está sendo desocupado até " + data(saindo.getPrazoDesocupacao()));
        }
        if (!box.getStatus().equals("LIVRE")) {
            throw erro("O box " + box.getCodigo() + " já está alugado");
        }
        contrato.setId(null);
        contrato.setBox(box);
        contrato.setCliente(cliente);
        if (contrato.getDataInicio() == null) {
            contrato.setDataInicio(LocalDate.now());
        }
        if (contrato.getDataInicio().isBefore(LocalDate.now())) {
            throw erro("A data de início não pode ser antes de hoje");
        }
        if (contrato.getDataInicio().isAfter(LocalDate.now().plusDays(INICIO_MAXIMO_DIAS))) {
            throw erro("A data de início pode ser no máximo " + INICIO_MAXIMO_DIAS + " dias depois de hoje");
        }
        if (cliente.getDiaVencimento() == null) {
            throw erro("Escolha o dia de pagamento no cadastro do cliente " + cliente.getNome());
        }
        contrato.setDiaVencimento(cliente.getDiaVencimento());
        if (contrato.getValorMensal() == null) {
            contrato.setValorMensal(box.getPrecoMensal());
        }
        if (!admin && Math.round(contrato.getValorMensal() * 100) != Math.round(box.getPrecoMensal() * 100)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só o administrador pode mudar o valor. O box "
                    + box.getCodigo() + " custa R$ " + dinheiro(box.getPrecoMensal()) + " por mês");
        }
        validar(contrato);
        // A 1ª parcela é paga na assinatura
        contrato.setParcelasPagas(1);
        contrato.setStatus("ATIVO");
        contrato.setDataEncerramento(null);
        // Alugou, o box fica ocupado
        box.setStatus("OCUPADO");
        boxRepository.save(box);
        return contratoRepository.save(contrato);
    }

    // Na alteração só o valor muda. A data de início e o vencimento ficam travados
    public Contrato atualizar(Integer id, Contrato dados) {
        Contrato contrato = findById(id);
        exigirAtivo(contrato);
        if (dados.getValorMensal() != null) {
            contrato.setValorMensal(dados.getValorMensal());
        }
        validar(contrato);
        return contratoRepository.save(contrato);
    }

    // Registra o pagamento de uma parcela. Dá para adiantar só a próxima, não várias
    public Contrato registrarPagamento(Integer id) {
        Contrato contrato = findById(id);
        exigirAtivo(contrato);
        int vencidas = contrato.parcelasVencidasAte(LocalDate.now());
        if (contrato.getParcelasPagas() > vencidas) {
            throw erro("A parcela que vence em " + data(contrato.vencimentoDaParcela(contrato.getParcelasPagas()))
                    + " já foi paga adiantada");
        }
        contrato.setParcelasPagas(contrato.getParcelasPagas() + 1);
        return contratoRepository.save(contrato);
    }

    // Encerramento igual ao de banco: só fecha se não houver parcela vencida em aberto.
    // Cancelando antes do próximo vencimento, a parcela nova não é cobrada.
    // O contrato fica guardado como ENCERRADO e a empresa tem PRAZO_DESOCUPACAO_DIAS para tirar as coisas.
    // O box continua ocupado até o fim do prazo e depois fica livre sozinho
    public Contrato encerrar(Integer id) {
        Contrato contrato = findById(id);
        exigirAtivo(contrato);
        LocalDate hoje = LocalDate.now();
        int emAtraso = contrato.parcelasVencidasAte(hoje) - contrato.getParcelasPagas();
        if (emAtraso > 0) {
            LocalDate primeira = contrato.vencimentoDaParcela(contrato.getParcelasPagas() + 1);
            String parcelas = emAtraso == 1 ? "1 parcela vencida" : emAtraso + " parcelas vencidas";
            throw erro("O contrato tem " + parcelas + " desde " + data(primeira) + " (R$ "
                    + dinheiro(emAtraso * contrato.getValorMensal())
                    + "). Registre o pagamento antes de encerrar");
        }
        contrato.setStatus("ENCERRADO");
        contrato.setDataEncerramento(hoje);
        contrato.setPrazoDesocupacao(hoje.plusDays(PRAZO_DESOCUPACAO_DIAS));
        return contratoRepository.save(contrato);
    }

    private void exigirAtivo(Contrato contrato) {
        if (!"ATIVO".equals(contrato.getStatus())) {
            throw erro("Este contrato já foi encerrado");
        }
    }

    // O valor do contrato segue as mesmas regras do preço do box: o mínimo por m² e o máximo de R$ 9.999,99.
    // Assim o desconto não fura o preço mínimo
    private void validar(Contrato contrato) {
        if (contrato.getValorMensal() == null || contrato.getValorMensal() <= 0) {
            throw erro("O valor mensal precisa ser maior que zero");
        }
        if (contrato.getValorMensal() > 9999.99) {
            throw erro("O valor mensal vai até R$ 9.999,99");
        }
        Double tamanho = contrato.getBox().getTamanho();
        if (tamanho != null) {
            long minimoCentavos = Math.round(tamanho * BoxService.PRECO_MINIMO_M2 * 100);
            if (Math.round(contrato.getValorMensal() * 100) < minimoCentavos) {
                throw erro("Para o box " + contrato.getBox().getCodigo() + " (" + String.valueOf(tamanho).replace('.', ',')
                        .replace(",0", "") + " m²) o valor mínimo é R$ " + dinheiro(minimoCentavos / 100.0)
                        + " por mês (R$ " + dinheiro(BoxService.PRECO_MINIMO_M2) + " por m²)");
            }
        }
    }

    // 2026-12-05 vira 05/12/2026
    private String data(LocalDate data) {
        return String.format("%02d/%02d/%d", data.getDayOfMonth(), data.getMonthValue(), data.getYear());
    }

    // 1250.5 vira 1.250,50
    private String dinheiro(double valor) {
        long centavos = Math.round(valor * 100);
        String reais = String.valueOf(centavos / 100);
        if (reais.length() > 3) {
            reais = reais.substring(0, reais.length() - 3) + "." + reais.substring(reais.length() - 3);
        }
        return reais + "," + String.format("%02d", centavos % 100);
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
