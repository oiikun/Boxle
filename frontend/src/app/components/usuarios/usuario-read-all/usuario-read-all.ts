import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Usuario } from '../../../entities/usuario';
import { UsuarioService } from '../../../services/usuario.service';

// Lista de usuários. Só o administrador entra aqui
@Component({
  selector: 'app-usuario-read-all',
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './usuario-read-all.html'
})
export class UsuarioReadAll implements OnInit {
  list: Usuario[] = [];

  constructor(public service: UsuarioService, private router: Router) {}

  ngOnInit(): void {
    if (!this.service.isAdmin()) {
      this.router.navigate(['']);
      return;
    }
    this.findAll();
  }

  findAll(): void {
    this.service.findAll().subscribe((resposta) => {
      this.list = resposta;
    });
  }

  // Ninguém pode apagar o próprio usuário
  apagar(usuario: Usuario): void {
    let logado = this.service.usuarioLogado;
    if (logado && logado.id == usuario.id) {
      this.service.message('Você não pode apagar o próprio usuário');
      return;
    }
    if (!confirm(`Apagar o usuário ${usuario.email}?`)) {
      return;
    }
    this.service.apagar(usuario.id).subscribe((resposta) => {
      this.service.message('Usuário apagado com sucesso');
      this.list = this.list.filter(u => u.id != usuario.id);
    }, (err) => {
      this.service.mensagemErro(err, 'Não foi possível apagar o usuário');
    });
  }
}
