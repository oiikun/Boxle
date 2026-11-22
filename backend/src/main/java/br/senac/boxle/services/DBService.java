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

    // CPFs dos usuários iniciais. Passam no dígito verificador, mas são de exemplo. Troque pelos de verdade
    private static final String CPF_ADMIN = "52998224725";
    private static final String CPF_LUIS = "11144477735";
    private static final String CPF_CAMILA = "24813579655";

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
        atualizarUsuariosAntigos();
        // Primeira execução (banco vazio): cria os funcionários e os dados de exemplo
        if (usuarioRepository.count() > 0) {
            return;
        }
        usuarioService.gravar(new Usuario("Administrador", "admin@boxle.com", "admin", "ADMIN", CPF_ADMIN));
        usuarioService.gravar(new Usuario("Luis Fernando Costa", "luis@boxle.com", "luis", "ATENDENTE", CPF_LUIS));
        usuarioService.gravar(new Usuario("Camila Rocha", "camila@boxle.com", "camila", "ATENDENTE", CPF_CAMILA));
        criarDadosDeExemplo();
    }

    // ----- Dados de exemplo da primeira execução -----

    // Um galpão pequeno já em funcionamento: 3 boxes por corredor, pequenos no A, médios no B e grandes no C.
    // O preço por m² cai conforme o box cresce, como no mercado de self-storage, e nunca fica abaixo do mínimo.
    // As empresas são fictícias. CNPJ e IE passam no dígito verificador, e os CEPs são de ruas reais
    private void criarDadosDeExemplo() {
        if (boxRepository.count() > 0 || clienteRepository.count() > 0) {
            return;
        }
        // Corredor A: pequenos (R$ 95 por m²)
        criarBoxes("A", new double[]{2, 4, 6}, new double[]{190, 380, 570});
        // Corredor B: médios (R$ 80 por m²)
        criarBoxes("B", new double[]{8, 10, 12}, new double[]{640, 800, 960});
        // Corredor C: grandes (R$ 65 por m²)
        criarBoxes("C", new double[]{15, 20, 30}, new double[]{980, 1300, 1950});

        Cliente alvorada = clienteRepository.save(cliente("Alvorada Distribuidora de Embalagens Ltda",
                "47391826000192", "148720360113", "03015000", "Avenida Celso Garcia", "1420", "Brás", "São Paulo",
                "Mariana Lopes", 5, "(11) 3326-4180", "financeiro@alvoradaembalagens.com.br"));
        Cliente casaNobre = clienteRepository.save(cliente("Casa Nobre Móveis Planejados Ltda",
                "28506417000140", "257194836111", "05425070", "Avenida Doutora Ruth Cardoso", "7221", "Pinheiros",
                "São Paulo", "Ricardo Menezes", 10, "(11) 3031-7725", "compras@casanobremoveis.com.br"));

        LocalDate hoje = LocalDate.now();
        // Alvorada: um box em dia e outro com uma parcela em atraso
        contratoAtivo("A2", alvorada, hoje.minusMonths(4), 0);
        contratoAtivo("B2", alvorada, hoje.minusMonths(3), 1);
        // Casa Nobre: um box em dia e outro encerrado há 2 dias, ainda no prazo para desocupar
        contratoAtivo("C2", casaNobre, hoje.minusMonths(7), 0);
        contratoEncerrado("C3", casaNobre, hoje.minusMonths(6), hoje.minusDays(2));
    }

    private void criarBoxes(String corredor, double[] tamanhos, double[] precos) {
        for (int i = 0; i < tamanhos.length; i++) {
            boxRepository.save(new Box(corredor + (i + 1), tamanhos[i], precos[i], "LIVRE"));
        }
    }

    private Cliente cliente(String nome, String cnpj, String ie, String cep, String logradouro, String numero,
                            String bairro, String cidade, String contato, int diaVencimento, String telefone,
                            String email) {
        Cliente cliente = new Cliente(nome, cnpj, ie, contato, diaVencimento, telefone, email);
        cliente.setCep(cep);
        cliente.setLogradouro(logradouro);
        cliente.setNumero(numero);
        cliente.setBairro(bairro);
        cliente.setCidade(cidade);
        cliente.setUf("SP");
        return cliente;
    }

    // Contrato que começou no passado, com as parcelas pagas até hoje (menos as em atraso)
    private void contratoAtivo(String codigo, Cliente cliente, LocalDate inicio, int parcelasEmAtraso) {
        Contrato contrato = novoContrato(codigo, cliente, inicio);
        contrato.setStatus("ATIVO");
        contrato.setParcelasPagas(Math.max(1, contrato.parcelasVencidasAte(LocalDate.now()) - parcelasEmAtraso));
        contratoRepository.save(contrato);
    }

    private void contratoEncerrado(String codigo, Cliente cliente, LocalDate inicio, LocalDate encerramento) {
        Contrato contrato = novoContrato(codigo, cliente, inicio);
        contrato.setStatus("ENCERRADO");
        contrato.setParcelasPagas(contrato.parcelasVencidasAte(encerramento));
        contrato.setDataEncerramento(encerramento);
        contrato.setPrazoDesocupacao(encerramento.plusDays(ContratoService.PRAZO_DESOCUPACAO_DIAS));
        contratoRepository.save(contrato);
    }

    private Contrato novoContrato(String codigo, Cliente cliente, LocalDate inicio) {
        Box box = boxRepository.findByCodigo(codigo).orElseThrow();
        box.setStatus("OCUPADO");
        boxRepository.save(box);
        Contrato contrato = new Contrato(box, cliente, inicio);
        contrato.setValorMensal(box.getPrecoMensal());
        contrato.setDiaVencimento(cliente.getDiaVencimento());
        return contrato;
    }

    // ----- Atualização de bancos criados por versões antigas -----

    // Os usuários iniciais criados antes de existir o CPF recebem o CPF deles
    private void atualizarUsuariosAntigos() {
        for (Usuario usuario : usuarioRepository.findAll()) {
            if (usuario.getCpf() != null && !usuario.getCpf().isEmpty()) {
                continue;
            }
            String cpf = null;
            if ("admin@boxle.com".equals(usuario.getEmail())) {
                cpf = CPF_ADMIN;
            } else if ("luis@boxle.com".equals(usuario.getEmail())) {
                cpf = CPF_LUIS;
            } else if ("camila@boxle.com".equals(usuario.getEmail())) {
                cpf = CPF_CAMILA;
            }
            if (cpf != null && usuarioRepository.findByCpf(cpf).isEmpty()) {
                usuario.setCpf(cpf);
                usuarioRepository.save(usuario);
            }
        }
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
