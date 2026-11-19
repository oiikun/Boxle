import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { Box } from '../../../entities/box';
import { BoxService } from '../../../services/box.service';

// Cadastro e alteração de box. O mesmo formulário serve para os dois
@Component({
  selector: 'app-box-cadastro',
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './box-cadastro.html'
})
export class BoxCadastro implements OnInit {
  box: Box = {
    codigo: '',
    status: 'LIVRE'
  };
  boxes: Box[] = [];
  corredor = 'A';
  numero = '';
  tamanho = '';
  preco = '';
  editando = false;

  constructor(private router: Router, private route: ActivatedRoute, public service: BoxService) {}

  // Box alugado não muda de código nem de tamanho (o back end também confere)
  get alugado(): boolean {
    return this.editando && this.box.status != 'LIVRE';
  }

  ngOnInit(): void {
    this.service.findAll().subscribe((resposta) => {
      this.boxes = resposta;
    });
    let id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editando = true;
      this.service.findById(id).subscribe((resposta) => {
        this.box = resposta;
        this.corredor = resposta.codigo.charAt(0);
        this.numero = resposta.codigo.substring(1);
        this.tamanho = this.service.tamanhoParaTexto(resposta.tamanho);
        this.preco = this.service.precoParaTexto(resposta.precoMensal);
      }, (err) => {
        this.service.mensagemErro(err, 'Box não encontrado');
        this.router.navigate(['/boxes']);
      });
    }
  }

  // Aplica as máscaras enquanto a pessoa digita
  formatar(): void {
    this.numero = this.service.mascaraNumeroBox(this.numero);
    this.tamanho = this.service.mascaraTamanho(this.tamanho);
    this.preco = this.service.mascaraPreco(this.preco);
  }

  // Confere antes de mandar para o back end, que valida de novo
  validar(): string {
    if (this.numero == '') {
      return 'Informe o número do box';
    }
    let codigo = this.corredor + this.numero;
    let repetidos = this.boxes.filter(b => b.codigo == codigo && b.id != this.box.id);
    if (repetidos.length > 0) {
      return `Já existe um box com o código ${codigo}`;
    }
    if (this.tamanho == '') {
      return 'Informe o tamanho do box';
    }
    let tamanho = this.service.textoParaNumero(this.tamanho);
    if (tamanho < 1 || tamanho > 50) {
      return 'O tamanho precisa ser de 1 a 50 m²';
    }
    let usada = this.service.areaUsada(this.boxes, this.corredor, this.box.id);
    let maxima = this.service.areaMaximaCorredor;
    if (Math.round((usada + tamanho) * 10) > maxima * 10) {
      let sobra = Math.max(0, Math.round((maxima - usada) * 10) / 10);
      return `O corredor ${this.corredor} tem ${maxima} m² e já usa ${this.service.tamanhoParaTexto(usada) || '0'} m². ` +
        `Cabem no máximo ${this.service.tamanhoParaTexto(sobra) || '0'} m² neste box`;
    }
    if (this.preco == '') {
      return 'Informe o preço por mês';
    }
    let minimo = this.service.precoMinimo(tamanho);
    if (Math.round(this.service.textoParaNumero(this.preco) * 100) < Math.round(minimo * 100)) {
      return `Para ${this.tamanho} m² o preço mínimo é ${this.service.formatarValor(minimo)} por mês ` +
        `(${this.service.formatarValor(this.service.precoMinimoM2)} por m²)`;
    }
    return '';
  }

  // Quanto ainda cabe no corredor escolhido, para mostrar embaixo do campo de tamanho
  areaLivre(): string {
    let usada = this.service.areaUsada(this.boxes, this.corredor, this.box.id);
    let sobra = Math.max(0, Math.round((this.service.areaMaximaCorredor - usada) * 10) / 10);
    return this.service.tamanhoParaTexto(sobra) || '0';
  }

  // Mostra embaixo do campo de preço o mínimo para o tamanho digitado
  textoPrecoMinimo(): string {
    let tamanho = this.tamanho == '' ? 0 : this.service.textoParaNumero(this.tamanho);
    if (!tamanho) {
      return `Mínimo de ${this.service.formatarValor(this.service.precoMinimoM2)} por m²`;
    }
    return `Mínimo para ${this.tamanho} m²: ${this.service.formatarValor(this.service.precoMinimo(tamanho))}`;
  }

  cancelar(): void {
    this.router.navigate(['/boxes']);
  }

  gravar(): void {
    this.formatar();
    let erro = this.validar();
    if (erro != '') {
      this.service.message(erro);
      return;
    }
    this.box.codigo = this.corredor + this.numero;
    this.box.tamanho = this.service.textoParaNumero(this.tamanho);
    this.box.precoMensal = this.service.textoParaNumero(this.preco);
    if (this.editando) {
      this.service.atualizar(this.box).subscribe((resposta) => {
        this.service.message('Box atualizado com sucesso');
        this.router.navigate(['/boxes']);
      }, (err) => {
        this.service.mensagemErro(err, 'Erro ao atualizar o box');
      });
    } else {
      this.service.cadastrar(this.box).subscribe((resposta) => {
        this.service.message('Box cadastrado com sucesso');
        this.router.navigate(['/boxes']);
      }, (err) => {
        this.service.mensagemErro(err, 'Erro ao cadastrar o box');
      });
    }
  }
}
