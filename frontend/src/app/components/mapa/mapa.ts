import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Box } from '../../entities/box';
import { Contrato } from '../../entities/contrato';
import { BoxService } from '../../services/box.service';
import { ContratoService } from '../../services/contrato.service';

// Tela inicial com o mapa do galpão. Mostra os corredores A, B e C e cada box livre ou ocupado.
// Box de contrato encerrado aparece em laranja com o último dia para a empresa desocupar
@Component({
  selector: 'app-mapa',
  imports: [CommonModule],
  templateUrl: './mapa.html',
  styleUrl: './mapa.scss'
})
export class Mapa implements OnInit {
  corredores: { letra: string; boxes: Box[]; areaUsada: number }[] = [];
  contratos: Contrato[] = [];
  livres = 0;
  ocupados = 0;

  constructor(public boxService: BoxService, private contratoService: ContratoService,
              private router: Router) {}

  // Busca os boxes e os contratos para saber quem está em cada box
  ngOnInit(): void {
    this.boxService.findAll().subscribe((boxes) => {
      this.contratoService.findAll().subscribe((contratos) => {
        this.contratos = contratos;
        this.montarMapa(boxes);
      });
    });
  }

  montarMapa(boxes: Box[]): void {
    for (let letra of this.boxService.corredores) {
      this.corredores.push({
        letra: letra,
        boxes: boxes.filter(b => b.codigo.charAt(0) == letra),
        areaUsada: this.boxService.areaUsada(boxes, letra)
      });
    }
    for (let box of boxes) {
      if (box.status == 'LIVRE') {
        this.livres++;
      } else {
        this.ocupados++;
      }
    }
  }

  // Contrato ativo do box. Contrato sem status é de antes da atualização e conta como ativo
  contratoDoBox(box: Box): Contrato | undefined {
    return this.contratos.filter(c => c.box.id == box.id && (c.status == 'ATIVO' || !c.status))[0];
  }

  // Contrato encerrado cuja empresa ainda está no prazo para tirar as coisas
  saindo(box: Box): Contrato | undefined {
    if (box.status == 'LIVRE') {
      return undefined;
    }
    return this.contratos.filter(c => c.box.id == box.id && c.desocupando)[0];
  }

  emAtraso(box: Box): boolean {
    let contrato = this.contratoDoBox(box);
    return !!contrato && !!contrato.parcelasEmAtraso;
  }

  rotulo(box: Box): string {
    let contrato = this.contratoDoBox(box);
    if (contrato) {
      return contrato.cliente.nome;
    }
    let saindo = this.saindo(box);
    if (saindo) {
      return `Desocupar até ${this.contratoService.dataParaTexto(saindo.prazoDesocupacao).substring(0, 5)}`;
    }
    return box.status == 'LIVRE' ? 'Livre' : 'Ocupado';
  }

  dica(box: Box): string {
    let saindo = this.saindo(box);
    if (saindo) {
      return `Box ${box.codigo}: ${saindo.cliente.nome} tem até ` +
        `${this.contratoService.dataParaTexto(saindo.prazoDesocupacao)} para retirar as coisas`;
    }
    let contrato = this.contratoDoBox(box);
    if (!contrato) {
      return `Box ${box.codigo}`;
    }
    let texto = `Box ${box.codigo}, paga todo dia ${contrato.diaVencimento}`;
    if (contrato.parcelasEmAtraso) {
      texto += `. ${contrato.parcelasEmAtraso} parcela(s) em atraso`;
    } else if (contrato.proximoVencimento) {
      texto += `. Próximo vencimento: ${this.contratoService.dataParaTexto(contrato.proximoVencimento)}`;
    }
    return texto;
  }

  // Box maior aparece mais largo no mapa
  largura(box: Box): number {
    let tamanho = 0;
    if (box.tamanho) {
      tamanho = box.tamanho;
    }
    return 80 + tamanho * 14;
  }

  // Ocupado (ou desocupando) abre o contrato. Livre abre um contrato novo já com o box escolhido
  abrir(box: Box): void {
    let contrato = this.contratoDoBox(box) || this.saindo(box);
    if (contrato) {
      this.router.navigate(['/contratos', contrato.id]);
    } else if (box.status == 'LIVRE') {
      this.router.navigate(['/contratos/novo', box.id]);
    }
  }
}
