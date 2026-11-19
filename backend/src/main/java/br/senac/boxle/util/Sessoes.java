package br.senac.boxle.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Guarda quem está logado. No login cada pessoa recebe uma chave (token) aleatória,
// e o front manda essa chave em toda requisição. Fica na memória: ao reiniciar o sistema, todos entram de novo
@Component
public class Sessoes {

    private final Map<String, Integer> usuarioPorToken = new ConcurrentHashMap<>();

    public String abrir(Integer usuarioId) {
        String token = UUID.randomUUID().toString();
        usuarioPorToken.put(token, usuarioId);
        return token;
    }

    // Id do usuário dono da chave, ou null se a chave não existe
    public Integer usuarioDo(String token) {
        if (token == null) {
            return null;
        }
        return usuarioPorToken.get(token);
    }

    public void fechar(String token) {
        if (token != null) {
            usuarioPorToken.remove(token);
        }
    }

    // Usuário apagado perde as sessões abertas
    public void fecharDoUsuario(Integer usuarioId) {
        usuarioPorToken.values().removeIf(id -> id.equals(usuarioId));
    }
}
