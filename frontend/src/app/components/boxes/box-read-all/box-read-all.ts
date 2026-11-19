import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Box } from '../../../entities/box';
import { BoxService } from '../../../services/box.service';
import { UsuarioService } from '../../../services/usuario.service';

// Lista de boxes, com editar e apagar
@Component({
  selector: 'app-box-read-all',
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './box-read-all.html'
})
export class BoxReadAll implements OnInit {
  list: Box[] = [];

  constructor(public service: BoxService, public usuarioService: UsuarioService) {}

  ngOnInit(): void {
    this.findAll();
  }

  findAll(): void {
    this.service.findAll().subscribe((resposta) => {
      this.list = resposta;
    });
  }

  nomeStatus(status: string): string {
    return this.service.nomeStatus(status);
  }

  apagar(box: Box): void {
    if (!confirm(`Apagar o box ${box.codigo}?`)) {
      return;
    }
    this.service.apagar(box.id).subscribe((resposta) => {
      this.service.message('Box apagado com sucesso');
      this.list = this.list.filter(b => b.id != box.id);
    }, (err) => {
      this.service.mensagemErro(err, 'Não foi possível apagar o box');
    });
  }
}
