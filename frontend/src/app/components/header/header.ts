import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { UsuarioService } from '../../services/usuario.service';

// Barra de cima com o menu, o nome de quem está logado e o botão de sair
@Component({
  selector: 'app-header',
  imports: [CommonModule, MatToolbarModule, MatButtonModule, MatIconModule, RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.scss'
})
export class Header {
  constructor(public usuarioService: UsuarioService, private router: Router) {}

  sair(): void {
    this.usuarioService.sair();
    this.router.navigate(['']);
  }
}
