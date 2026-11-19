import { Box } from './box';
import { Cliente } from './cliente';

// Formato do contrato que vem da API, já com o box e a empresa dentro
export interface Contrato {
  id?: number;
  box: Box;
  cliente: Cliente;
  dataInicio?: string;
  valorMensal?: number;
  diaVencimento?: number;
  parcelasPagas?: number;
  // ATIVO ou ENCERRADO
  status?: string;
  dataEncerramento?: string;
  // Último dia para a empresa tirar as coisas do box depois de encerrar
  prazoDesocupacao?: string;
  // Encerrado, mas ainda dentro do prazo de desocupação
  desocupando?: boolean;
  // Calculados pelo back end
  proximoVencimento?: string;
  parcelasEmAtraso?: number;
  valorEmAtraso?: number;
}
