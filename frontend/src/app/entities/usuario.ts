// Formato do usuário. A senha só vai da tela para a API, nunca volta
export interface Usuario {
  id?: number;
  nome: string;
  email: string;
  senha?: string;
  perfil: string;
  // CPF do funcionário (só números na API)
  cpf?: string;
  // Chave da sessão, recebida no login
  token?: string;
}
