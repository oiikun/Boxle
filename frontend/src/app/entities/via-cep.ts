// Resposta do ViaCEP. Quando o CEP não existe, vem só erro: true
export interface ViaCep {
  cep: string;
  logradouro: string;
  bairro: string;
  localidade: string;
  uf: string;
  erro?: any;
}
