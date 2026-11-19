import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Cliente } from '../../../entities/cliente';
import { Contrato } from '../../../entities/contrato';
import { ClienteService } from '../../../services/cliente.service';
import { ContratoService } from '../../../services/contrato.service';
import { UsuarioService } from '../../../services/usuario.service';

// Lista de empresas, com documentos, endereço e os boxes que cada uma aluga
@Component({
  selector: 'app-cliente-read-all',
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './cliente-read-all.html'
})
export class ClienteReadAll implements OnInit {
  list: Cliente[] = [];
  contratos: Contrato[] = [];

  constructor(public service: ClienteService, private contratoService: ContratoService,
              public usuarioService: UsuarioService) {}

  ngOnInit(): void {
    this.findAll();
  }

  findAll(): void {
    this.service.findAll().subscribe((resposta) => {
      this.list = resposta;
    });
    this.contratoService.findAll().subscribe((resposta) => {
      this.contratos = resposta;
    });
  }

  resumo(): string {
    if (this.list.length == 1) {
      return '1 empresa cadastrada';
    }
    return `${this.list.length} empresas cadastradas`;
  }

  // Junta os contratos ativos da empresa para mostrar quais boxes ela aluga
  boxesDoCliente(cliente: Cliente): string {
    let codigos = '';
    let total = 0;
    for (let contrato of this.contratos) {
      if (contrato.cliente.id == cliente.id && contrato.status != 'ENCERRADO') {
        if (codigos != '') {
          codigos = ', ' + codigos;
        }
        codigos = contrato.box.codigo + codigos;
        total++;
      }
    }
    if (total == 0) {
      return 'Nenhum box alugado';
    }
    if (total == 1) {
      return `1 box alugado: ${codigos}`;
    }
    return `${total} boxes alugados: ${codigos}`;
  }

  documento(cliente: Cliente): string {
    let ie = this.service.mascaraIe(cliente.ie);
    if (ie == 'ISENTO') {
      ie = 'Isento';
    }
    return `CNPJ ${this.service.mascaraCnpj(cliente.cnpj)}\nIE ${ie}`;
  }

  endereco(cliente: Cliente): string {
    let texto = `${cliente.logradouro}, ${cliente.numero}`;
    if (cliente.bairro) {
      texto += `, ${cliente.bairro}`;
    }
    return `${texto}\n${cliente.cidade}/${cliente.uf}, CEP ${this.service.mascaraCep(cliente.cep)}`;
  }

  apagar(cliente: Cliente): void {
    if (!confirm(`Apagar o cliente ${cliente.nome}?`)) {
      return;
    }
    this.service.apagar(cliente.id).subscribe((resposta) => {
      this.service.message('Cliente apagado com sucesso');
      this.list = this.list.filter(c => c.id != cliente.id);
    }, (err) => {
      this.service.mensagemErro(err, 'Não foi possível apagar o cliente');
    });
  }
}
