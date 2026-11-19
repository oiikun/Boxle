package br.senac.boxle.util;

// Validação dos documentos pelos dígitos verificadores, como o enunciado do projeto pede
public class Documentos {

    // Tira pontos, traços, barras e espaços e deixa as letras maiúsculas
    public static String limpar(String valor) {
        if (valor == null) {
            return null;
        }
        String resultado = "";
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            if (c != '.' && c != '-' && c != '/' && c != ' ') {
                resultado = resultado + c;
            }
        }
        return resultado.toUpperCase();
    }

    private static boolean soNumeros(String valor) {
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    // Números todos iguais (111.111.111-11) passam na conta, mas não são documentos de verdade
    private static boolean todosIguais(String valor) {
        for (int i = 1; i < valor.length(); i++) {
            if (valor.charAt(i) != valor.charAt(0)) {
                return false;
            }
        }
        return true;
    }

    // CPF tem 11 números. Os 2 últimos são calculados a partir dos 9 primeiros (módulo 11)
    public static boolean cpfValido(String cpf) {
        if (cpf == null || cpf.length() != 11 || !soNumeros(cpf) || todosIguais(cpf)) {
            return false;
        }
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (cpf.charAt(i) - '0') * (10 - i);
        }
        int dv1 = (soma * 10) % 11;
        if (dv1 == 10) {
            dv1 = 0;
        }
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += (cpf.charAt(i) - '0') * (11 - i);
        }
        int dv2 = (soma * 10) % 11;
        if (dv2 == 10) {
            dv2 = 0;
        }
        return dv1 == cpf.charAt(9) - '0' && dv2 == cpf.charAt(10) - '0';
    }

    // CNPJ tem 14 caracteres. No CNPJ novo os 12 primeiros podem ter letras, e cada caractere vale o código dele menos 48
    public static boolean cnpjValido(String cnpj) {
        if (cnpj == null || cnpj.length() != 14 || todosIguais(cnpj)) {
            return false;
        }
        for (int i = 0; i < 12; i++) {
            char c = cnpj.charAt(i);
            boolean numero = c >= '0' && c <= '9';
            boolean letra = c >= 'A' && c <= 'Z';
            if (!numero && !letra) {
                return false;
            }
        }
        if (!soNumeros(cnpj.substring(12))) {
            return false;
        }
        int[] pesos = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += (cnpj.charAt(i) - '0') * pesos[i + 1];
        }
        int dv1 = 0;
        if (soma % 11 >= 2) {
            dv1 = 11 - soma % 11;
        }
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += (cnpj.charAt(i) - '0') * pesos[i];
        }
        int dv2 = 0;
        if (soma % 11 >= 2) {
            dv2 = 11 - soma % 11;
        }
        return dv1 == cnpj.charAt(12) - '0' && dv2 == cnpj.charAt(13) - '0';
    }

    // CEP precisa ter 8 números
    public static boolean cepValido(String cep) {
        return cep != null && cep.length() == 8 && soNumeros(cep);
    }

    // Inscrição estadual de SP. 12 números com dígitos na 9ª e na 12ª posição. ISENTO também vale
    public static boolean ieValida(String ie) {
        if (ie == null) {
            return false;
        }
        if (ie.equals("ISENTO")) {
            return true;
        }
        if (ie.length() != 12 || !soNumeros(ie)) {
            return false;
        }
        int[] pesos1 = {1, 3, 4, 5, 6, 7, 8, 10};
        int soma = 0;
        for (int i = 0; i < 8; i++) {
            soma += (ie.charAt(i) - '0') * pesos1[i];
        }
        int dv1 = soma % 11 % 10;
        int[] pesos2 = {3, 2, 10, 9, 8, 7, 6, 5, 4, 3, 2};
        soma = 0;
        for (int i = 0; i < 11; i++) {
            soma += (ie.charAt(i) - '0') * pesos2[i];
        }
        int dv2 = soma % 11 % 10;
        return dv1 == ie.charAt(8) - '0' && dv2 == ie.charAt(11) - '0';
    }

    // ----- Contato (não são documentos, mas ficam aqui para todas as validações estarem num lugar só) -----

    // Um @ só, algo antes dele, um ponto depois e nenhum espaço
    public static boolean emailValido(String email) {
        if (email == null) {
            return false;
        }
        int arroba = email.indexOf('@');
        int ponto = email.lastIndexOf('.');
        return arroba > 0 && arroba == email.lastIndexOf('@') && ponto > arroba + 1
                && ponto < email.length() - 1 && email.indexOf(' ') == -1;
    }

    // Telefone com DDD: 10 números (fixo) ou 11 (celular, começando com 9). O DDD vai de 11 a 99
    public static boolean telefoneValido(String telefone) {
        if (telefone == null) {
            return false;
        }
        String numeros = "";
        for (int i = 0; i < telefone.length(); i++) {
            char c = telefone.charAt(i);
            if (c >= '0' && c <= '9') {
                numeros = numeros + c;
            }
        }
        if (numeros.length() != 10 && numeros.length() != 11) {
            return false;
        }
        int ddd = Integer.parseInt(numeros.substring(0, 2));
        if (ddd < 11) {
            return false;
        }
        return numeros.length() == 10 || numeros.charAt(2) == '9';
    }
}
