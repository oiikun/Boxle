import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatButtonModule } from '@angular/material/button';
import { Box } from '../../../entities/box';
import { Cliente } from '../../../entities/cliente';
import { Contrato } from '../../../entities/contrato';
import { BoxService } from '../../../services/box.service';
import { ClienteService } from '../../../services/cliente.service';
import { ContratoService } from '../../../services/contrato.service';
import { UsuarioService } from '../../../services/usuario.service';

// Contrato novo (vindo do mapa) ou um contrato que já existe, com pagamentos e encerramento
@Component({
  selector: 'app-contrato-cadastro',
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatDatepickerModule, MatButtonModule],
  templateUrl: './contrato-cadastro.html',
  styles: [`
    .pagamentos { margin: 4px 0 16px; padding: 12px 16px; background: #f6f5f1; border-left: 5px solid var(--faixa); }
    .pagamentos h2 { margin: 0 0 6px; font-size: 16px; font-weight: 500; }
    .pagamentos p { margin: 4px 0; }
    .pagamentos .atraso { color: var(--perigo); }
    .pagamentos .saida { color: var(--saindo); }
    .subtitulo .status { margin-left: 8px; }
  `]
})
export class ContratoCadastro implements OnInit {
  contrato: Contrato = {
    box: { codigo: '', status: '' },
    cliente: {
      nome: '', cnpj: '', ie: '', cep: '', logradouro: '', numero: '', bairro: '', cidade: '', uf: '',
      responsavel: '', telefone: '', email: ''
    }
  };
  boxesLivres: Box[] = [];
  clientes: Cliente[] = [];
  boxId?: number;
  clienteId?: number;
  hoje = new Date(new Date().setHours(0, 0, 0, 0));
  dataInicio: Date = new Date(this.hoje);
  valor = '';
  editando = false;

  constructor(private router: Router, private route: ActivatedRoute, public service: ContratoService,
              public boxService: BoxService, private clienteService: ClienteService,
              public usuarioService: UsuarioService) {}

  // Último dia em que o contrato pode começar (igual ao back end)
  get inicioMaximo(): Date {
    return new Date(this.hoje.getFullYear(), this.hoje.getMonth(), this.hoje.getDate() + this.service.inicioMaximoDias);
  }

  get encerrado(): boolean {
    return this.editando && this.contrato.status == 'ENCERRADO';
  }

  ngOnInit(): void {
    let id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editando = true;
      this.service.findById(id).subscribe((resposta) => {
        this.mostrar(resposta);
      }, (err) => {
        this.service.mensagemErro(err, 'Contrato não encontrado');
        this.router.navigate(['']);
      });
    } else {
      this.clienteService.findAll().subscribe((resposta) => {
        this.clientes = resposta;
      });
      this.boxService.findAll().subscribe((resposta) => {
        this.boxesLivres = resposta.filter(b => b.status == 'LIVRE');
        let boxDoMapa = this.route.snapshot.paramMap.get('box');
        if (boxDoMapa) {
          this.boxId = parseInt(boxDoMapa);
          this.escolherBox();
        }
      });
    }
  }

  mostrar(contrato: Contrato): void {
    // Contrato sem status é de antes da atualização e conta como ativo
    if (!contrato.status) {
      contrato.status = 'ATIVO';
    }
    this.contrato = contrato;
    this.valor = this.boxService.precoParaTexto(contrato.valorMensal);
    if (contrato.dataInicio) {
      this.dataInicio = this.service.lerData(contrato.dataInicio);
    }
  }

  // Ao escolher o box, o valor já vem com o preço dele
  escolherBox(): void {
    for (let box of this.boxesLivres) {
      if (box.id == this.boxId) {
        this.contrato.box = box;
        this.valor = this.boxService.precoParaTexto(box.precoMensal);
      }
    }
  }

  formatar(): void {
    this.valor = this.boxService.mascaraPreco(this.valor);
  }

  escolherCliente(): void {
    for (let cliente of this.clientes) {
      if (cliente.id == this.clienteId) {
        this.contrato.cliente = cliente;
      }
    }
  }

  cancelar(): void {
    this.router.navigate(['']);
  }

  registrarPagamento(): void {
    let vencimento = this.service.dataParaTexto(this.contrato.proximoVencimento);
    let valor = this.boxService.formatarValor(this.contrato.valorMensal);
    if (!confirm(`Registrar o pagamento da parcela de ${vencimento} (${valor})?`)) {
      return;
    }
    this.service.registrarPagamento(this.contrato.id).subscribe((resposta) => {
      this.mostrar(resposta);
      this.service.message(`Pagamento de ${vencimento} registrado`);
    }, (err) => {
      this.service.mensagemErro(err, 'Não foi possível registrar o pagamento');
    });
  }

  // Encerrar só fecha sem parcela vencida em aberto. A empresa ganha um prazo para tirar as coisas do box
  encerrar(): void {
    if (this.contrato.parcelasEmAtraso) {
      this.service.message(`Há ${this.contrato.parcelasEmAtraso} parcela(s) vencida(s) ` +
        `(${this.boxService.formatarValor(this.contrato.valorEmAtraso)}). Registre o pagamento antes de encerrar`);
      return;
    }
    let vencimento = this.service.dataParaTexto(this.contrato.proximoVencimento);
    let prazo = new Date(this.hoje);
    prazo.setDate(prazo.getDate() + this.service.prazoDesocupacaoDias);
    let textoPrazo = this.service.dataParaTexto(this.service.formatarData(prazo));
    if (!confirm(`Encerrar o contrato do box ${this.contrato.box.codigo}? A parcela de ${vencimento} não será cobrada. ` +
      `A empresa tem até ${textoPrazo} para retirar as coisas do box.`)) {
      return;
    }
    this.service.encerrar(this.contrato.id).subscribe((resposta) => {
      this.service.message(`Contrato encerrado. ${this.contrato.cliente.nome} tem até ` +
        `${this.service.dataParaTexto(resposta.prazoDesocupacao)} para desocupar o box ${this.contrato.box.codigo}`);
      this.router.navigate(['']);
    }, (err) => {
      this.service.mensagemErro(err, 'Não foi possível encerrar o contrato');
    });
  }

  // A empresa já tirou tudo antes do fim do prazo: libera o box para alugar
  liberarAgora(): void {
    if (!confirm(`A empresa já retirou tudo do box ${this.contrato.box.codigo}? Ele fica livre para alugar.`)) {
      return;
    }
    this.boxService.liberar(this.contrato.box.id).subscribe(() => {
      this.service.message(`Box ${this.contrato.box.codigo} liberado`);
      this.router.navigate(['']);
    }, (err) => {
      this.service.mensagemErro(err, 'Não foi possível liberar o box');
    });
  }

  gravar(): void {
    this.formatar();
    if (!this.editando && !this.boxId) {
      this.service.message('Escolha o box do contrato');
      return;
    }
    if (!this.editando && !this.clienteId) {
      this.service.message('Escolha o cliente do contrato');
      return;
    }
    if (!this.editando && (!this.dataInicio || new Date(this.dataInicio) < this.hoje)) {
      this.service.message('A data de início não pode ser antes de hoje');
      return;
    }
    if (this.valor == '') {
      this.service.message('Informe o valor por mês');
      return;
    }
    if (!this.editando && new Date(this.dataInicio) > this.inicioMaximo) {
      this.service.message(`A data de início pode ser no máximo ${this.service.inicioMaximoDias} dias depois de hoje`);
      return;
    }
    // O desconto não pode passar do preço mínimo por m², igual ao back end
    let tamanho = this.contrato.box.tamanho;
    if (tamanho) {
      let minimo = this.boxService.precoMinimo(tamanho);
      if (Math.round(this.boxService.textoParaNumero(this.valor) * 100) < Math.round(minimo * 100)) {
        this.service.message(`Para o box ${this.contrato.box.codigo} o valor mínimo é ${this.boxService.formatarValor(minimo)} por mês`);
        return;
      }
    }
    this.contrato.valorMensal = this.boxService.textoParaNumero(this.valor);
    this.contrato.dataInicio = this.service.formatarData(this.dataInicio);
    if (this.editando) {
      this.service.atualizar(this.contrato).subscribe((resposta) => {
        this.service.message('Contrato atualizado com sucesso');
        this.router.navigate(['']);
      }, (err) => {
        this.service.mensagemErro(err, 'Erro ao atualizar o contrato');
      });
    } else {
      this.service.cadastrar(this.contrato).subscribe((resposta) => {
        this.service.message('Contrato cadastrado com sucesso');
        this.router.navigate(['']);
      }, (err) => {
        this.service.mensagemErro(err, 'Erro ao cadastrar o contrato');
      });
    }
  }
}
