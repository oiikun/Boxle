package br.senac.boxle.services;

import br.senac.boxle.entities.Box;
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

// Service guarda as regras de negócio do box. O Resource chama o Service, e o Service usa o Repository
@Service
public class BoxService {

    // Área máxima de cada corredor (A, B e C). A soma dos boxes de um corredor não pode passar disso
    public static final double AREA_MAXIMA_CORREDOR = 150.0;

    // Preço mínimo por m² ao mês. O mercado de self-storage no Brasil cobra em média
    // de R$ 65 (Abrass, 2022) a R$ 112 (Asbrass, 1º tri de 2023) por m². R$ 50 fica abaixo
    // da média para dar margem a descontos, mas impede alugar um box por um valor irreal
    public static final double PRECO_MINIMO_M2 = 50.0;

    @Autowired
    private BoxRepository boxRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    public List<Box> findAll() {
        liberarPrazosVencidos();
        return boxRepository.listarEmOrdem();
    }

    // Box de contrato encerrado fica ocupado até o fim do prazo de desocupação. No dia seguinte fica livre.
    // Só libera se o box não tiver contrato ativo nem outro contrato encerrado ainda dentro do prazo
    public void liberarPrazosVencidos() {
        for (Box box : boxRepository.findAll()) {
            if (box.getStatus().equals("LIVRE") || contratoRepository.existsByBoxIdAndStatus(box.getId(), "ATIVO")) {
                continue;
            }
            // Também corrige box marcado como ocupado sem nenhum contrato
            List<Contrato> encerrados = contratoRepository.findByBoxIdAndStatus(box.getId(), "ENCERRADO");
            boolean algumNoPrazo = encerrados.stream().anyMatch(Contrato::isDesocupando);
            if (!algumNoPrazo) {
                box.setStatus("LIVRE");
                boxRepository.save(box);
            }
        }
    }

    public Box findById(Integer id) {
        liberarPrazosVencidos();
        Optional<Box> box = boxRepository.findById(id);
        if (box.orElse(null) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Box não encontrado");
        }
        return box.get();
    }

    // Box novo sempre começa LIVRE
    public Box gravar(Box box) {
        box.setId(null);
        box.setStatus("LIVRE");
        validar(box);
        return boxRepository.save(box);
    }

    // Na alteração muda o código, o tamanho e o preço. O status continua por conta do contrato.
    // Box alugado (ou sendo desocupado) não muda de lugar nem de tamanho: o contrato foi feito para aquele espaço.
    // O preço pode mudar, porque vale só para os próximos contratos (o contrato guarda o valor dele)
    public Box atualizar(Integer id, Box dados) {
        Box box = findById(id);
        dados.setId(id);
        validar(dados);
        if (!box.getStatus().equals("LIVRE")) {
            if (!box.getCodigo().equals(dados.getCodigo())) {
                throw erro("O box " + box.getCodigo() + " está alugado. Não dá para mudar o código dele agora");
            }
            if (Math.round(box.getTamanho() * 10) != Math.round(dados.getTamanho() * 10)) {
                throw erro("O box " + box.getCodigo() + " está alugado. Não dá para mudar o tamanho dele agora");
            }
        }
        box.setCodigo(dados.getCodigo());
        box.setTamanho(dados.getTamanho());
        box.setPrecoMensal(dados.getPrecoMensal());
        return boxRepository.save(box);
    }

    // Box alugado ou sendo desocupado não pode ser apagado. Os contratos encerrados dele saem junto
    public void apagar(Integer id) {
        Box box = findById(id);
        if (!box.getStatus().equals("LIVRE") || contratoRepository.existsByBoxIdAndStatus(id, "ATIVO")) {
            throw erro("Este box está alugado ou sendo desocupado e não pode ser apagado");
        }
        contratoRepository.deleteAll(contratoRepository.findByBoxId(id));
        boxRepository.deleteById(id);
    }

    // A empresa tirou as coisas antes do fim do prazo: o box já pode ficar livre.
    // O prazo do contrato encerrado passa a ser ontem, para não segurar mais o box
    public Box liberar(Integer id) {
        Box box = findById(id);
        if (contratoRepository.existsByBoxIdAndStatus(id, "ATIVO")) {
            throw erro("Este box tem contrato ativo. Encerre o contrato antes");
        }
        if (box.getStatus().equals("LIVRE")) {
            throw erro("Este box já está livre");
        }
        for (Contrato contrato : contratoRepository.findByBoxIdAndStatus(id, "ENCERRADO")) {
            if (contrato.isDesocupando()) {
                contrato.setPrazoDesocupacao(LocalDate.now().minusDays(1));
                contratoRepository.save(contrato);
            }
        }
        box.setStatus("LIVRE");
        return boxRepository.save(box);
    }

    // Validações que valem para cadastrar e para alterar
    private void validar(Box box) {
        if (box.getCodigo() == null || box.getCodigo().trim().isEmpty()) {
            throw erro("Informe o código do box");
        }
        box.setCodigo(box.getCodigo().trim().toUpperCase());
        if (!codigoValido(box.getCodigo())) {
            throw erro("O código do box é A, B ou C seguido de um número de 1 a 10");
        }
        if (box.getTamanho() == null || box.getTamanho() < 1 || box.getTamanho() > 50) {
            throw erro("O tamanho precisa ser de 1 a 50 m²");
        }
        if (box.getPrecoMensal() == null || box.getPrecoMensal() <= 0) {
            throw erro("Informe o preço mensal do box");
        }
        if (box.getPrecoMensal() > 9999.99) {
            throw erro("O preço mensal vai até R$ 9.999,99");
        }
        // Compara em centavos para não sofrer com arredondamento do double
        long minimoCentavos = Math.round(box.getTamanho() * PRECO_MINIMO_M2 * 100);
        if (Math.round(box.getPrecoMensal() * 100) < minimoCentavos) {
            throw erro("Para " + formatarArea(box.getTamanho()) + " m² o preço mínimo é R$ "
                    + formatarDinheiro(minimoCentavos) + " por mês (R$ "
                    + formatarDinheiro(Math.round(PRECO_MINIMO_M2 * 100)) + " por m²)");
        }
        Box mesmoCodigo = boxRepository.findByCodigo(box.getCodigo()).orElse(null);
        if (mesmoCodigo != null && !mesmoCodigo.getId().equals(box.getId())) {
            throw erro("Já existe um box com o código " + box.getCodigo());
        }
        validarAreaDoCorredor(box);
    }

    // A soma do tamanho dos boxes do corredor (sem contar o próprio box, na alteração)
    // mais o tamanho novo não pode passar da área do corredor
    private void validarAreaDoCorredor(Box box) {
        char letra = box.getCodigo().charAt(0);
        double usada = 0;
        for (Box outro : boxRepository.findAll()) {
            boolean mesmoCorredor = outro.getCodigo() != null && outro.getCodigo().charAt(0) == letra;
            boolean mesmoBox = box.getId() != null && box.getId().equals(outro.getId());
            if (mesmoCorredor && !mesmoBox && outro.getTamanho() != null) {
                usada += outro.getTamanho();
            }
        }
        // Compara em décimos de m² para não sofrer com arredondamento do double
        long usadaDecimos = Math.round(usada * 10);
        long novaDecimos = Math.round(box.getTamanho() * 10);
        long maximaDecimos = Math.round(AREA_MAXIMA_CORREDOR * 10);
        if (usadaDecimos + novaDecimos > maximaDecimos) {
            throw erro("O corredor " + letra + " tem " + formatarArea(AREA_MAXIMA_CORREDOR)
                    + " m² e já usa " + formatarArea(usadaDecimos / 10.0) + " m². Cabem no máximo "
                    + formatarArea(Math.max(0, maximaDecimos - usadaDecimos) / 10.0) + " m² neste box");
        }
    }

    // 125050 centavos vira "1.250,50"
    private String formatarDinheiro(long centavos) {
        String reais = String.valueOf(centavos / 100);
        if (reais.length() > 3) {
            reais = reais.substring(0, reais.length() - 3) + "." + reais.substring(reais.length() - 3);
        }
        return reais + "," + String.format("%02d", centavos % 100);
    }

    // 12.5 vira "12,5" e 150.0 vira "150"
    private String formatarArea(double valor) {
        if (valor == Math.floor(valor)) {
            return String.valueOf((long) valor);
        }
        return String.valueOf(valor).replace('.', ',');
    }

    // Código válido é A, B ou C seguido de 1 a 10, sem zero na frente (A1 até C10)
    private boolean codigoValido(String codigo) {
        if (codigo.length() < 2 || codigo.length() > 3) {
            return false;
        }
        char letra = codigo.charAt(0);
        if (letra != 'A' && letra != 'B' && letra != 'C') {
            return false;
        }
        String numero = codigo.substring(1);
        for (int i = 0; i < numero.length(); i++) {
            char c = numero.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        int valor = Integer.parseInt(numero);
        return valor >= 1 && valor <= 10 && numero.charAt(0) != '0';
    }

    // Erro 400 com a mensagem que aparece na tela
    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
