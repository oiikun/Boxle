import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { Usuario } from '../../../entities/usuario';
import { UsuarioService } from '../../../services/usuario.service';

// Cadastro e alteração de usuário (nome, e-mail, senha e perfil)
@Component({
  selector: 'app-usuario-cadastro',
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './usuario-cadastro.html'
})
export class UsuarioCadastro implements OnInit {
  usuario: Usuario = {
    nome: '',
    email: '',
    senha: '',
    perfil: 'ATENDENTE',
    cpf: ''
  };
  editando = false;

  constructor(private router: Router, private route: ActivatedRoute, private service: UsuarioService) {}

  ngOnInit(): void {
    if (!this.service.isAdmin()) {
      this.router.navigate(['']);
      return;
    }
    let id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editando = true;
      this.service.findById(id).subscribe((resposta) => {
        this.usuario = resposta;
        this.usuario.senha = '';
        this.formatar();
      }, (err) => {
        this.service.mensagemErro(err, 'Usuário não encontrado');
        this.router.navigate(['/usuarios']);
      });
    }
  }

  formatar(): void {
    this.usuario.cpf = this.service.mascaraCpf(this.usuario.cpf);
  }

  cancelar(): void {
    this.router.navigate(['/usuarios']);
  }

  // Se a pessoa alterou o próprio usuário, atualiza o nome e o perfil na sessão
  atualizarSessao(resposta: Usuario): void {
    let logado = this.service.usuarioLogado;
    if (logado && logado.id == resposta.id) {
      this.service.guardarSessao(resposta);
    }
  }

  gravar(): void {
    this.formatar();
    if (this.service.somenteNumeros(this.usuario.cpf, 11).length != 11) {
      this.service.message('Informe o CPF do funcionário com 11 números');
      return;
    }
    if (this.editando) {
      this.service.atualizar(this.usuario).subscribe((resposta) => {
        this.atualizarSessao(resposta);
        this.service.message('Usuário atualizado com sucesso');
        this.router.navigate(['/usuarios']);
      }, (err) => {
        this.service.mensagemErro(err, 'Erro ao atualizar o usuário');
      });
    } else {
      this.service.cadastrar(this.usuario).subscribe((resposta) => {
        this.service.message('Usuário cadastrado com sucesso');
        this.router.navigate(['/usuarios']);
      }, (err) => {
        this.service.mensagemErro(err, 'Erro ao cadastrar o usuário');
      });
    }
  }
}
