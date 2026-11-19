// Formato do cliente (empresa) que vem da API
export interface Cliente {
  id?: number;
  nome: string;
  cnpj: string;
  ie: string;
  cep: string;
  logradouro: string;
  numero: string;
  bairro: string;
  cidade: string;
  uf: string;
  // Nome da pessoa de contato na empresa
  responsavel: string;
  // Dia do mês em que a empresa paga (1 a 28). Os contratos dela vencem nesse dia
  diaVencimento?: number;
  telefone: string;
  email: string;
}
