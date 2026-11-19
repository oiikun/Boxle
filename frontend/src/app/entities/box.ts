// Formato do box que vem da API, igual à entidade do back end
export interface Box {
  id?: number;
  codigo: string;
  tamanho?: number;
  precoMensal?: number;
  status: string;
}
