import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Usuario } from '../entities/usuario';

// Service do usuário. Login, sessão e chamadas para /usuarios
@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  baseUrl = `${environment.baseUrl}/usuarios`;
  usuarioLogado?: Usuario;

  // Se a página foi recarregada (F5), recupera quem estava logado
  constructor(private http: HttpClient, private snack: MatSnackBar) {
    let salvo = sessionStorage.getItem('usuarioLogado');
    if (salvo) {
      this.usuarioLogado = JSON.parse(salvo);
    }
  }

  // Guarda o usuário na sessão do navegador. Some quando a aba fecha, e a senha não vai junto.
  // Ao alterar o próprio cadastro, a resposta vem sem a chave da sessão, então a chave antiga é mantida
  guardarSessao(usuario: Usuario): void {
    if (!usuario.token && this.usuarioLogado) {
      usuario.token = this.usuarioLogado.token;
    }
    this.usuarioLogado = usuario;
    sessionStorage.setItem('usuarioLogado', JSON.stringify(usuario));
  }

  login(email: string, senha: string): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.baseUrl}/login`, { email: email, senha: senha });
  }

  // Só o administrador vê e usa a tela de Usuários
  isAdmin(): boolean {
    if (this.usuarioLogado) {
      return this.usuarioLogado.perfil === 'ADMIN';
    }
    return false;
  }

  // Avisa a API para fechar a sessão e limpa o navegador
  sair(): void {
    if (this.usuarioLogado) {
      this.http.post(`${this.baseUrl}/sair`, {}).subscribe({ error: () => {} });
    }
    this.usuarioLogado = undefined;
    sessionStorage.removeItem('usuarioLogado');
  }

  nomePerfil(perfil?: string): string {
    return perfil == 'ADMIN' ? 'Administrador' : 'Atendente';
  }

  // Só números, até o limite
  somenteNumeros(valor: string | undefined, limite: number): string {
    let resultado = '';
    for (let c of valor || '') {
      if (c >= '0' && c <= '9' && resultado.length < limite) {
        resultado += c;
      }
    }
    return resultado;
  }

  // 12345678909 vira 123.456.789-09
  mascaraCpf(valor?: string): string {
    let numeros = this.somenteNumeros(valor, 11);
    let resultado = '';
    for (let i = 0; i < numeros.length; i++) {
      if (i == 3 || i == 6) resultado += '.';
      if (i == 9) resultado += '-';
      resultado += numeros[i];
    }
    return resultado;
  }

  findAll(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.baseUrl);
  }

  findById(id: any): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.baseUrl}/${id}`);
  }

  cadastrar(usuario: Usuario): Observable<Usuario> {
    return this.http.post<Usuario>(this.baseUrl, usuario);
  }

  atualizar(usuario: Usuario): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.baseUrl}/${usuario.id}`, usuario);
  }

  apagar(id: any): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  message(msg: string): void {
    this.snack.open(msg, 'OK', {
      horizontalPosition: 'center',
      verticalPosition: 'top',
      duration: 5000
    });
  }

  mensagemErro(err: any, padrao: string): void {
    if (err.error && err.error.message) {
      this.message(err.error.message);
    } else {
      this.message(padrao);
    }
  }
}
