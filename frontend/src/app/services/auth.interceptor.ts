import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

// Coloca a chave da sessão (recebida no login) em toda chamada para a API.
// Se a API responder 401 (sessão acabou, por exemplo depois de reiniciar o sistema), volta para o login.
// Lê direto do sessionStorage para não depender do UsuarioService, que usa o HttpClient
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  let token = '';
  try {
    let salvo = sessionStorage.getItem('usuarioLogado');
    if (salvo) {
      token = JSON.parse(salvo).token || '';
    }
  } catch (e) {
    token = '';
  }
  let pedido = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(pedido).pipe(
    catchError((err: HttpErrorResponse) => {
      let ehLogin = req.url.endsWith('/usuarios/login');
      if (err.status == 401 && !ehLogin) {
        sessionStorage.removeItem('usuarioLogado');
        location.reload();
      }
      return throwError(() => err);
    })
  );
};
