import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { Cliente } from '../../../entities/cliente';
import { ClienteService } from '../../../services/cliente.service';

// Cadastro e alteração de empresa
@Component({
  selector: 'app-cliente-cadastro',
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './cliente-cadastro.html'
})
export class ClienteCadastro implements OnInit {
  cliente: Cliente = {
    nome: '',
    cnpj: '',
    ie: '',
    cep: '',
    logradouro: '',
    numero: '',
    bairro: '',
    cidade: '',
    uf: '',
    responsavel: '',
    diaVencimento: 10,
    telefone: '',
    email: ''
  };
  dias: number[] = Array.from({ length: 28 }, (_, i) => i + 1);
  editando = false;
  cepConferido = '';
  buscandoCep = false;

  constructor(private router: Router, private route: ActivatedRoute, private service: ClienteService) {}

  ngOnInit(): void {
    let id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editando = true;
      this.service.findById(id).subscribe((resposta) => {
        this.cliente = resposta;
        this.cepConferido = resposta.cep;
        this.formatar();
      }, (err) => {
        this.service.mensagemErro(err, 'Cliente não encontrado');
        this.router.navigate(['/clientes']);
      });
    }
  }

  cepOk(): boolean {
    return this.cepConferido != '' && this.cepConferido == this.service.somenteNumeros(this.cliente.cep, 8);
  }

  // Quando o CEP tem 8 números, consulta o ViaCEP e preenche o endereço
  conferirCep(): void {
    this.formatar();
    let cep = this.service.somenteNumeros(this.cliente.cep, 8);
    if (cep.length != 8 || cep == this.cepConferido || this.buscandoCep) {
      return;
    }
    this.buscandoCep = true;
    this.service.buscarCep(cep).subscribe((resposta) => {
      this.buscandoCep = false;
      if (resposta.erro) {
        this.cepConferido = '';
        this.cliente.logradouro = '';
        this.cliente.bairro = '';
        this.cliente.cidade = '';
        this.cliente.uf = '';
        this.service.message('CEP não encontrado nos Correios');
        return;
      }
      this.cepConferido = cep;
      this.cliente.logradouro = resposta.logradouro;
      this.cliente.bairro = resposta.bairro;
      this.cliente.cidade = resposta.localidade;
      this.cliente.uf = resposta.uf;
    }, (err) => {
      this.buscandoCep = false;
      this.cepConferido = '';
      this.service.message('Não foi possível consultar o CEP. Confira a internet.');
    });
  }

  formatar(): void {
    this.cliente.cep = this.service.mascaraCep(this.cliente.cep);
    this.cliente.cnpj = this.service.mascaraCnpj(this.cliente.cnpj);
    this.cliente.ie = this.service.mascaraIe(this.cliente.ie);
    this.cliente.telefone = this.service.mascaraTelefone(this.cliente.telefone);
  }

  // Aqui confere o tamanho dos campos. Os dígitos verificadores são conferidos no back end
  validar(): string {
    if (this.cliente.nome.trim() == '') {
      return 'Informe a razão social';
    }
    if (this.cliente.cnpj.length != 18) {
      return 'O CNPJ precisa ter 14 caracteres';
    }
    if (this.cliente.ie != 'ISENTO' && this.service.somenteNumeros(this.cliente.ie, 12).length != 12) {
      return 'A inscrição estadual precisa ter 12 números, ou ISENTO';
    }
    let cep = this.service.somenteNumeros(this.cliente.cep, 8);
    if (cep.length != 8) {
      return 'O CEP precisa ter 8 números';
    }
    if (cep != this.cepConferido) {
      return 'O CEP precisa ser encontrado nos Correios';
    }
    if (this.cliente.logradouro.trim() == '') {
      return 'Informe a rua da empresa';
    }
    if (this.cliente.numero.trim() == '') {
      return 'Informe o número do endereço';
    }
    if (this.cliente.responsavel.trim() == '') {
      return 'Informe o nome do contato na empresa';
    }
    if (!this.cliente.diaVencimento) {
      return 'Escolha o dia do pagamento';
    }
    if (this.service.somenteNumeros(this.cliente.telefone, 11).length < 10) {
      return 'Informe o telefone com DDD';
    }
    let email = this.cliente.email.trim();
    let arroba = email.indexOf('@');
    let ponto = email.lastIndexOf('.');
    if (arroba < 1 || arroba != email.lastIndexOf('@') || ponto < arroba + 2 || ponto == email.length - 1 || email.indexOf(' ') != -1) {
      return 'Informe um e-mail válido';
    }
    return '';
  }

  cancelar(): void {
    this.router.navigate(['/clientes']);
  }

  gravar(): void {
    this.formatar();
    let erro = this.validar();
    if (erro != '') {
      this.service.message(erro);
      return;
    }
    if (this.editando) {
      this.service.atualizar(this.cliente).subscribe((resposta) => {
        this.service.message('Cliente atualizado com sucesso');
        this.router.navigate(['/clientes']);
      }, (err) => {
        this.service.mensagemErro(err, 'Erro ao atualizar o cliente');
      });
    } else {
      this.service.cadastrar(this.cliente).subscribe((resposta) => {
        this.service.message('Cliente cadastrado com sucesso');
        this.router.navigate(['/clientes']);
      }, (err) => {
        this.service.mensagemErro(err, 'Erro ao cadastrar o cliente');
      });
    }
  }
}
