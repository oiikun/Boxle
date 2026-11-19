import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { Header } from './components/header/header';
import { Login } from './components/login/login';
import { UsuarioService } from './services/usuario.service';

// Componente principal. Mostra o login até alguém entrar e depois as telas das rotas
@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, Header, Login],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  constructor(public usuarioService: UsuarioService) {}
}
