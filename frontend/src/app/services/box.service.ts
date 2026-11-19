import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Box } from '../entities/box';

// Service do box. Faz as chamadas para /boxes e tem as máscaras dos campos
@Injectable({
  providedIn: 'root'
})
export class BoxService {
  baseUrl = `${environment.baseUrl}/boxes`;
  corredores = ['A', 'B', 'C'];
  // Área máxima de cada corredor, igual ao AREA_MAXIMA_CORREDOR do back end
  areaMaximaCorredor = 150;
  // Preço mínimo por m² ao mês, igual ao PRECO_MINIMO_M2 do back end (média do mercado: R$ 65 a R$ 112)
  precoMinimoM2 = 50;

  constructor(private http: HttpClient, private snack: MatSnackBar) {}

  findAll(): Observable<Box[]> {
    return this.http.get<Box[]>(this.baseUrl);
  }

  findById(id: any): Observable<Box> {
    return this.http.get<Box>(`${this.baseUrl}/${id}`);
  }

  cadastrar(box: Box): Observable<Box> {
    return this.http.post<Box>(this.baseUrl, box);
  }

  atualizar(box: Box): Observable<Box> {
    return this.http.put<Box>(`${this.baseUrl}/${box.id}`, box);
  }

  apagar(id: any): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  // Libera o box antes do fim do prazo de desocupação, quando a empresa já tirou tudo
  liberar(id: any): Observable<Box> {
    return this.http.put<Box>(`${this.baseUrl}/${id}/liberar`, {});
  }

  nomeStatus(status: string): string {
    if (status == 'LIVRE') {
      return 'Livre';
    }
    return 'Ocupado';
  }

  // Soma o tamanho dos boxes de um corredor. idIgnorado serve para não contar o próprio box na alteração
  areaUsada(boxes: Box[], letra: string, idIgnorado?: number): number {
    let soma = 0;
    for (let b of boxes) {
      if (b.codigo.charAt(0) == letra && (idIgnorado == undefined || b.id != idIgnorado) && b.tamanho) {
        soma += b.tamanho;
      }
    }
    // Arredonda para uma casa e evita sobras como 12.300000000001
    return Math.round(soma * 10) / 10;
  }

  // Preço mínimo do box pelo tamanho, arredondado em centavos
  precoMinimo(tamanho: number): number {
    return Math.round(tamanho * this.precoMinimoM2 * 100) / 100;
  }

  formatarValor(valor?: number): string {
    if (!valor) {
      return 'R$ 0,00';
    }
    return 'R$ ' + this.precoParaTexto(valor);
  }

  // Só deixa digitar de 1 a 10, sem zero na frente
  mascaraNumeroBox(valor: string): string {
    let resultado = '';
    if (!valor) {
      return resultado;
    }
    for (let c of valor) {
      let numero = c >= '0' && c <= '9';
      let zeroNaFrente = c == '0' && resultado == '';
      if (numero && !zeroNaFrente && parseInt(resultado + c) <= 10) {
        resultado += c;
      }
    }
    return resultado;
  }

  // Número até 50, com uma casa depois da vírgula
  mascaraTamanho(valor: string): string {
    let resultado = '';
    let temVirgula = false;
    let depois = 0;
    if (!valor) {
      return resultado;
    }
    for (let c of valor) {
      if (c >= '0' && c <= '9') {
        let zeroNaFrente = c == '0' && resultado == '';
        let cabe = this.textoParaNumero(resultado + c) <= 50;
        if (!temVirgula && !zeroNaFrente && cabe) {
          resultado += c;
        } else if (temVirgula && depois < 1 && cabe) {
          resultado += c;
          depois++;
        }
      } else if ((c == ',' || c == '.') && !temVirgula && resultado != '' && this.textoParaNumero(resultado) < 50) {
        resultado += ',';
        temVirgula = true;
      }
    }
    return resultado;
  }

  // Máscara de dinheiro. Os números entram pela direita (15000 vira 150,00), até 9.999,99
  mascaraPreco(valor: string): string {
    let numeros = '';
    if (valor) {
      for (let c of valor) {
        let numero = c >= '0' && c <= '9';
        let zeroNaFrente = c == '0' && numeros == '';
        if (numero && !zeroNaFrente && numeros.length < 6) {
          numeros += c;
        }
      }
    }
    if (numeros == '') {
      return '';
    }
    while (numeros.length < 3) {
      numeros = '0' + numeros;
    }
    let reais = numeros.substring(0, numeros.length - 2);
    let centavos = numeros.substring(numeros.length - 2);
    if (reais.length > 3) {
      reais = reais.substring(0, reais.length - 3) + '.' + reais.substring(reais.length - 3);
    }
    return reais + ',' + centavos;
  }

  // Converte o texto da tela (1.234,50) para número (1234.5)
  textoParaNumero(texto: string): number {
    let convertido = '';
    for (let c of texto) {
      if (c == ',') {
        convertido += '.';
      } else if (c != '.') {
        convertido += c;
      }
    }
    return parseFloat(convertido);
  }

  precoParaTexto(valor?: number): string {
    if (!valor) {
      return '';
    }
    return this.mascaraPreco('' + Math.round(valor * 100));
  }

  tamanhoParaTexto(valor?: number): string {
    let resultado = '';
    if (!valor) {
      return resultado;
    }
    for (let c of '' + valor) {
      if (c == '.') {
        resultado += ',';
      } else {
        resultado += c;
      }
    }
    return resultado;
  }

  // Aviso (snackbar) no topo da tela
  message(msg: string): void {
    this.snack.open(msg, 'OK', {
      horizontalPosition: 'center',
      verticalPosition: 'top',
      duration: 5000
    });
  }

  // Mostra a mensagem de erro que veio do back end, ou uma mensagem padrão
  mensagemErro(err: any, padrao: string): void {
    if (err.error && err.error.message) {
      this.message(err.error.message);
    } else {
      this.message(padrao);
    }
  }
}
