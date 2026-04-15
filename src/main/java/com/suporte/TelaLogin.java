package com.suporte; // Define o namespace para organização estrutural do projeto.

// Importações do AWT (Abstract Window Toolkit) - Base para interfaces visuais e eventos
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Arrays;

// Importações do Swing - Componentes visuais modernos construídos sobre o AWT
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

// Importação da biblioteca externa FlatLaf para renderização do Dark Mode
import com.formdev.flatlaf.FlatDarkLaf;

public class TelaLogin extends JFrame { // Herda de JFrame, tornando esta classe uma janela independente do sistema operacional.

    // Declaração dos componentes da interface gráfica em escopo de classe para serem acessados pelos métodos.
    private JTextField campoUsuario;
    private JPasswordField campoSenha; // Usado especificamente para mascarar a digitação e retornar char[].
    private JButton botaoEntrar;

    // Construtor da classe: inicializa e desenha a interface assim que o objeto é instanciado.
    public TelaLogin() {
        // --- CONFIGURAÇÕES BASE DA JANELA ---
        setTitle("Acesso - Ferramentas de Suporte"); // Título da barra superior.
        setSize(380, 240); // Dimensões absolutas (Largura x Altura) em pixels.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Encerra a JVM (Processo do Java) ao fechar esta janela.
        setLocationRelativeTo(null); // O parâmetro 'null' centraliza a janela no monitor do usuário.
        setResizable(false); // Trava o redimensionamento para evitar quebras no layout projetado.
        
        // Define o layout principal como BorderLayout (Divide a tela em Norte, Sul, Leste, Oeste e Centro).
        setLayout(new BorderLayout(10, 10));

        // --- PAINEL DO FORMULÁRIO (Usando GridBagLayout para alinhamento matricial preciso) ---
        JPanel painelForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(); // Objeto de restrições que dita as regras de cada célula da grade.
        gbc.insets = new Insets(5, 5, 5, 5); // Define o padding (margem interna) de 5 pixels (Cima, Esquerda, Baixo, Direita).
        gbc.anchor = GridBagConstraints.WEST; // Alinha os componentes à esquerda da sua célula (Oeste).

        // Linha 0, Coluna 0: Label de instrução para o Usuário
        gbc.gridx = 0; gbc.gridy = 0;
        painelForm.add(new JLabel("Usuário (Domínio):"), gbc);

        // Linha 0, Coluna 1: Input de texto do Usuário
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Permite que o campo estique horizontalmente para preencher o espaço.
        campoUsuario = new JTextField(15); // Campo com largura sugestiva de 15 colunas.
        painelForm.add(campoUsuario, gbc);

        // Linha 1, Coluna 0: Label de instrução para a Senha
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE; // Reseta o estiramento para que o Label não fique distorcido.
        painelForm.add(new JLabel("Senha:"), gbc);

        // Linha 1, Coluna 1: Input de Senha mascarado
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Volta a permitir o estiramento horizontal.
        campoSenha = new JPasswordField(15);
        painelForm.add(campoSenha, gbc);

        // Linha 2, Coluna 1: Checkbox para alternar a visibilidade da senha
        gbc.gridx = 1; gbc.gridy = 2;
        JCheckBox checkMostrarSenha = new JCheckBox("Mostrar senha");
        
        // Expressão Lambda para ouvir cliques no Checkbox.
        checkMostrarSenha.addActionListener(e -> {
            if (checkMostrarSenha.isSelected()) {
                campoSenha.setEchoChar((char) 0); // (char) 0 anula a máscara, exibindo o texto em plain-text.
            } else {
                campoSenha.setEchoChar('•'); // Restaura o caractere de censura padrão (bullet/bolinha).
            }
        });
        painelForm.add(checkMostrarSenha, gbc); // Adiciona o checkbox no painel.

        // --- PAINEL INFERIOR (Agrupa o Botão de Login e os Créditos de Autor) ---
        JPanel painelInferior = new JPanel(new BorderLayout());
        
        JPanel painelBotao = new JPanel(); // FlowLayout implícito para centralizar o botão.
        botaoEntrar = new JButton("Entrar");
        
        // Delega a ação de clique do botão para o método realizarLogin().
        botaoEntrar.addActionListener(e -> realizarLogin());
        painelBotao.add(botaoEntrar);

        // --- LABEL COM HYPERLINK ---
        // Usa suporte nativo a HTML do JLabel para sublinhar o texto simulando um link.
        JLabel labelCreditos = new JLabel("<html><a href=''>Desenvolvido por Pedro Henrique Gontijo da Cruz</a></html>");
        labelCreditos.setFont(new Font("Segoe UI", Font.ITALIC, 11)); // Fonte tipográfica moderna.
        labelCreditos.setForeground(Color.GRAY);
        labelCreditos.setHorizontalAlignment(SwingConstants.CENTER); // Centraliza horizontalmente.
        labelCreditos.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Padding superior e inferior.
        labelCreditos.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Altera visualmente o cursor do mouse para interatividade.
        
        // Listener de eventos do mouse para capturar o clique no texto.
        labelCreditos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    // Invoca a API Desktop do SO para abrir a URI no navegador padrão (Chrome, Edge, etc).
                    Desktop.getDesktop().browse(new URI("http://www.teste.com")); 
                } catch (Exception ex) {
                    ex.printStackTrace(); // Loga a exceção no console caso não consiga abrir o navegador.
                }
            }
        });

        // Montagem final do Painel Inferior
        painelInferior.add(painelBotao, BorderLayout.CENTER);
        painelInferior.add(labelCreditos, BorderLayout.SOUTH);

        // Adiciona os sub-painéis à janela principal usando as âncoras do BorderLayout.
        add(painelForm, BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);
        
        // Aplica uma margem invisível nas bordas internas da própria janela (Evita que o conteúdo encoste na borda do Windows).
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
    }

    // Método responsável por validar dados, gerenciar a sessão e transitar as telas.
    private void realizarLogin() {
        String usuario = campoUsuario.getText();
        char[] senha = campoSenha.getPassword(); // Recupera a senha como array bruto para maior segurança (evita String Pool).

        // Validação básica de campos vazios (Impede nulos ou arrays vazios).
        if (usuario.isEmpty() || senha.length == 0) {
            JOptionPane.showMessageDialog(this, "Preencha usuário e senha!", "Erro", JOptionPane.ERROR_MESSAGE);
            return; // Interrompe o fluxo se a validação falhar.
        }

        // Envia as credenciais para o Singleton que gerencia a sessão global na RAM.
        SessaoSuporte.getInstancia().setCredenciais(usuario, senha);

        // Ação de Sanitização de Memória: Sobrescreve o array original da senha local com zeros imediatamente após o uso.
        Arrays.fill(senha, '0');
        // Limpa visualmente o campo da GUI.
        campoSenha.setText("");

        this.dispose(); // Destrói o objeto TelaLogin da memória e fecha a janela atual.
        new TelaSelecaoMaquina().setVisible(true); // Instancia e exibe a próxima tela (Painel Principal).
    }

    // Entry Point (Ponto de entrada) da aplicação.
    public static void main(String[] args) {
        try {
            // Inicializa a engine gráfica FlatLaf ANTES de renderizar qualquer componente.
            FlatDarkLaf.setup();
        } catch (Exception e) {
            e.printStackTrace(); // Prevenção de falha visual (fallback).
        }

        // Enfileira a criação da interface na Event Dispatch Thread (EDT) do Swing.
        // Prática mandatória para thread-safety em GUIs Java (previne travamentos ao iniciar).
        SwingUtilities.invokeLater(() -> new TelaLogin().setVisible(true));
    }
}