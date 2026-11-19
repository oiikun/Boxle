import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { UsuarioService } from './usuario.service';

// Telas que só o administrador abre (usuários e cadastro de box). O atendente volta para o mapa.
// O back end confere de novo, então digitar o endereço na mão não adianta
export const adminGuard: CanActivateFn = () => {
  let usuarioService = inject(UsuarioService);
  if (usuarioService.isAdmin()) {
    return true;
  }
  usuarioService.message('Só o administrador pode abrir esta tela');
  return inject(Router).parseUrl('/');
};
