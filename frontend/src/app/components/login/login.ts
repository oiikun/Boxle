import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { UsuarioService } from '../../services/usuario.service';

// Tela de login. Manda e-mail e senha para a API e, se der certo, guarda o usuário na sessão
@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  email = '';
  senha = '';
  erro = '';

  constructor(private usuarioService: UsuarioService, private router: Router) {}

  entrar(): void {
    this.erro = '';
    if (this.email == '' || this.senha == '') {
      this.erro = 'Preencha o e-mail e a senha.';
      return;
    }
    this.usuarioService.login(this.email, this.senha).subscribe((usuario) => {
      this.usuarioService.guardarSessao(usuario);
      this.router.navigate(['']);
    }, (err) => {
      if (err.status == 0) {
        this.erro = 'Servidor fora do ar.';
      } else {
        this.erro = 'E-mail ou senha inválidos.';
      }
      this.senha = '';
    });
  }
}
