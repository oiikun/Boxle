import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Cliente } from '../entities/cliente';
import { ViaCep } from '../entities/via-cep';

// Service do cliente. Chamadas para /clientes, consulta do CEP e máscaras dos documentos
@Injectable({
  providedIn: 'root'
})
export class ClienteService {
  baseUrl = `${environment.baseUrl}/clientes`;

  constructor(private http: HttpClient, private snack: MatSnackBar) {}

  findAll(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(this.baseUrl);
  }

  findById(id: any): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.baseUrl}/${id}`);
  }

  cadastrar(cliente: Cliente): Observable<Cliente> {
    return this.http.post<Cliente>(this.baseUrl, cliente);
  }

  atualizar(cliente: Cliente): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.baseUrl}/${cliente.id}`, cliente);
  }

  apagar(id: any): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  // Consulta o CEP no ViaCEP, que usa a base dos Correios
  buscarCep(cep: string): Observable<ViaCep> {
    return this.http.get<ViaCep>(`https://viacep.com.br/ws/${cep}/json/`);
  }

  somenteNumeros(valor: string, limite: number): string {
    let resultado = '';
    if (!valor) {
      return resultado;
    }
    for (let c of valor) {
      if (c >= '0' && c <= '9' && resultado.length < limite) {
        resultado += c;
      }
    }
    return resultado;
  }

  // Aceita letras, porque o CNPJ novo pode ter letras
  mascaraCnpj(valor: string): string {
    let letras = '';
    if (valor) {
      for (let c of valor.toUpperCase()) {
        let letraOuNumero = (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z');
        if (letraOuNumero && letras.length < 14) {
          letras += c;
        }
      }
    }
    let resultado = '';
    for (let i = 0; i < letras.length; i++) {
      if (i == 2 || i == 5) resultado += '.';
      if (i == 8) resultado += '/';
      if (i == 12) resultado += '-';
      resultado += letras[i];
    }
    return resultado;
  }

  // ISENTO ou 000.000.000.000
  mascaraIe(valor: string): string {
    if (valor && valor.length > 0 && valor[0].toUpperCase() >= 'A' && valor[0].toUpperCase() <= 'Z') {
      return valor.toUpperCase().substring(0, 6);
    }
    let numeros = this.somenteNumeros(valor, 12);
    let resultado = '';
    for (let i = 0; i < numeros.length; i++) {
      if (i == 3 || i == 6 || i == 9) resultado += '.';
      resultado += numeros[i];
    }
    return resultado;
  }

  mascaraCep(valor: string): string {
    let numeros = this.somenteNumeros(valor, 8);
    let resultado = '';
    for (let i = 0; i < numeros.length; i++) {
      if (i == 5) resultado += '-';
      resultado += numeros[i];
    }
    return resultado;
  }

  mascaraTelefone(valor: string): string {
    let numeros = this.somenteNumeros(valor, 11);
    let resultado = '';
    for (let i = 0; i < numeros.length; i++) {
      if (i == 0) resultado += '(';
      if (i == 2) resultado += ') ';
      if ((numeros.length == 11 && i == 7) || (numeros.length < 11 && i == 6)) resultado += '-';
      resultado += numeros[i];
    }
    return resultado;
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
