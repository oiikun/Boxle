import { Routes } from '@angular/router';
import { Mapa } from './components/mapa/mapa';
import { BoxReadAll } from './components/boxes/box-read-all/box-read-all';
import { BoxCadastro } from './components/boxes/box-cadastro/box-cadastro';
import { ClienteReadAll } from './components/clientes/cliente-read-all/cliente-read-all';
import { ClienteCadastro } from './components/clientes/cliente-cadastro/cliente-cadastro';
import { ContratoCadastro } from './components/contratos/contrato-cadastro/contrato-cadastro';
import { UsuarioReadAll } from './components/usuarios/usuario-read-all/usuario-read-all';
import { UsuarioCadastro } from './components/usuarios/usuario-cadastro/usuario-cadastro';
import { adminGuard } from './services/admin.guard';

// Cada endereço abre uma tela. Os contratos abrem pelo mapa, por isso não ficam no menu.
// As telas com adminGuard são só do administrador
export const routes: Routes = [
  { path: '', component: Mapa },
  { path: 'boxes', component: BoxReadAll },
  { path: 'boxes/novo', component: BoxCadastro, canActivate: [adminGuard] },
  { path: 'boxes/:id', component: BoxCadastro, canActivate: [adminGuard] },
  { path: 'clientes', component: ClienteReadAll },
  { path: 'clientes/novo', component: ClienteCadastro },
  { path: 'clientes/:id', component: ClienteCadastro },
  { path: 'contratos/novo', component: ContratoCadastro },
  { path: 'contratos/novo/:box', component: ContratoCadastro },
  { path: 'contratos/:id', component: ContratoCadastro },
  { path: 'usuarios', component: UsuarioReadAll, canActivate: [adminGuard] },
  { path: 'usuarios/novo', component: UsuarioCadastro, canActivate: [adminGuard] },
  { path: 'usuarios/:id', component: UsuarioCadastro, canActivate: [adminGuard] },
  { path: '**', redirectTo: '' }
];
