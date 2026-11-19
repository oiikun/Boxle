import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Contrato } from '../entities/contrato';

// Service do contrato. Chamadas para /contratos e conversão de datas
@Injectable({
  providedIn: 'root'
})
export class ContratoService {
  baseUrl = `${environment.baseUrl}/contratos`;
  // Igual ao PRAZO_DESOCUPACAO_DIAS do back end
  prazoDesocupacaoDias = 7;
  // Igual ao INICIO_MAXIMO_DIAS do back end
  inicioMaximoDias = 60;

  constructor(private http: HttpClient, private snack: MatSnackBar) {}

  findAll(): Observable<Contrato[]> {
    return this.http.get<Contrato[]>(this.baseUrl);
  }

  findById(id: any): Observable<Contrato> {
    return this.http.get<Contrato>(`${this.baseUrl}/${id}`);
  }

  cadastrar(contrato: Contrato): Observable<Contrato> {
    return this.http.post<Contrato>(this.baseUrl, contrato);
  }

  atualizar(contrato: Contrato): Observable<Contrato> {
    return this.http.put<Contrato>(`${this.baseUrl}/${contrato.id}`, contrato);
  }

  registrarPagamento(id: any): Observable<Contrato> {
    return this.http.post<Contrato>(`${this.baseUrl}/${id}/pagamento`, {});
  }

  // Encerra o contrato (fica no histórico) e abre o prazo para a empresa desocupar o box
  encerrar(id: any): Observable<Contrato> {
    return this.http.post<Contrato>(`${this.baseUrl}/${id}/encerrar`, {});
  }

  // 2026-12-05 vira 05/12/2026
  dataParaTexto(texto?: string): string {
    if (!texto) {
      return '';
    }
    let partes = texto.split('-');
    return `${partes[2]}/${partes[1]}/${partes[0]}`;
  }

  // Monta a data com o dia do computador. O toISOString usaria o horário de Londres e, depois das 21h, gravaria o dia seguinte
  formatarData(data: Date): string {
    let d = new Date(data);
    let mes = '' + (d.getMonth() + 1);
    let dia = '' + d.getDate();
    if (mes.length < 2) {
      mes = '0' + mes;
    }
    if (dia.length < 2) {
      dia = '0' + dia;
    }
    return `${d.getFullYear()}-${mes}-${dia}`;
  }

  // Lê a data que vem da API (2026-11-17) como meia-noite no horário local
  lerData(texto: string): Date {
    return new Date(texto + 'T00:00:00');
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
