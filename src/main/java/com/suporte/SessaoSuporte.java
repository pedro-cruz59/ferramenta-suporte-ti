package com.suporte; // Define que esta classe pertence ao pacote 'com.suporte', mantendo o projeto organizado em pastas.

import java.util.Arrays; // Importa a classe de utilidades do Java para podermos manipular o array da senha no método de limpeza.

public class SessaoSuporte { // Declara a classe pública.

    private static SessaoSuporte instancia; // Variável estática e privada que guarda a ÚNICA cópia dessa classe que vai existir na memória inteira do programa (Padrão Singleton).
    
    private String usuario; // Variável para armazenar o login do domínio.
    private char[] senha; // Variável para armazenar a senha. É um array de caracteres (char[]) em vez de String, pois Strings não podem ser apagadas manualmente da memória RAM.

    private SessaoSuporte() {} // Construtor privado. Impede que qualquer outra tela do programa use "new SessaoSuporte()", forçando todos a usarem o mesmo gerenciador.

    public static SessaoSuporte getInstancia() { // Método que as outras telas chamam para acessar a sessão.
        if (instancia == null) { // Verifica se é a primeira vez que o programa está pedindo a sessão.
            instancia = new SessaoSuporte(); // Se for a primeira vez, ele cria o objeto na memória.
        }
        return instancia; // Se não for a primeira vez, ele simplesmente devolve o objeto que já estava criado com os dados lá dentro.
    }

    public void setCredenciais(String usuario, char[] senhaDigitada) { // Método chamado pela TelaLogin quando o usuário clica em "Entrar".
        this.usuario = usuario; // Pega o usuário que veio da tela e guarda na variável interna da classe.
        this.senha = senhaDigitada.clone(); // Cria uma cópia independente do array da senha. Isso evita que limpar o campo de texto na tela apague a senha daqui acidentalmente.
    }

    public String getUsuario() { // Método para fornecer o usuário quando o MotorComandos precisar.
        return usuario; // Devolve o texto do usuário.
    }

    public char[] getSenha() { // Método para fornecer a senha quando o MotorComandos precisar.
        return senha; // Devolve o array com os caracteres da senha.
    }

    public void limparSessao() { // Método de segurança para "matar" a credencial sob demanda (ex: botão de logout ou ao fechar o app).
        this.usuario = null; // Remove a referência ao texto do usuário.
        if (this.senha != null) { // Proteção para garantir que o programa não dê erro tentando apagar uma senha que já está vazia.
            Arrays.fill(this.senha, '0'); // Vai direto na memória RAM e sobrescreve cada letra da senha verdadeira com o número zero.
        }
    }
}