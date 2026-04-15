package com.suporte; // Define o namespace para agrupamento lógico das classes do sistema.

// Importações do Swing e AWT para construção da GUI e tratamento de eventos
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Importações de I/O e Rede para o Ping contínuo e resolução de DNS
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;

public class TelaSelecaoMaquina extends JFrame { // Herda de JFrame para atuar como a janela principal (Top-Level Container).

    // --- DECLARAÇÃO DE COMPONENTES DA GUI ---
    private JTextField campoAlvo;
    private JButton botaoConectar;
    private JLabel labelStatus;
    
    // Componente de texto multilinha para simular o terminal (STDOUT padrão).
    private JTextArea areaConsole;
    
    // --- CONTROLE DE CONCORRÊNCIA (MULTITHREADING) ---
    // A palavra-chave 'volatile' garante a visibilidade imediata dessa variável entre múltiplas Threads.
    // Impede que a CPU faça cache do valor, garantindo que quando o botão de parar for clicado, a Thread do ping pare instantaneamente.
    private volatile boolean pingando = false; 
    private Thread threadPing; // Referência para a thread de background do ping contínuo.
    
    // --- BOTÕES DE AÇÃO ---
    private JButton btnRenovarIp;
    private JButton btnFlushDNS;
    private JButton btnGpupdate;
    private JButton btnSyncHora;
    private JButton btnReiniciarSpooler;
    private JButton btnLimparTemp;
    private JButton btnDescobrirUsuario;
    private JButton btnUptime;
    private JButton btnReboot;
    private JButton btnPingContinuo;

    // Construtor: Inicializa os componentes e monta a topologia da janela.
    public TelaSelecaoMaquina() {
        setTitle("Painel de Suporte Remoto");
        setSize(550, 650); // Dimensões calibradas para acomodar o JTextArea central.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Encerra a JVM no fechamento.
        setLocationRelativeTo(null); // Centraliza no monitor primário.
        setResizable(false); // Bloqueia redimensionamento para manter a integridade do layout.
        
        // Define o layout raiz como BorderLayout (Regiões: North, South, East, West, Center).
        setLayout(new BorderLayout(10, 10));

        // --- PAINEL SUPERIOR: INPUT DE REDE ---
        // FlowLayout organiza os componentes em linha, da esquerda para a direita.
        JPanel painelBusca = new JPanel(new FlowLayout());
        painelBusca.add(new JLabel("Alvo (IP ou Hostname):"));
        
        campoAlvo = new JTextField(15);
        painelBusca.add(campoAlvo);
        
        botaoConectar = new JButton("Conectar");
        painelBusca.add(botaoConectar);

        // Agrupa a busca e o status em um sub-painel Norte.
        JPanel painelTopo = new JPanel(new BorderLayout());
        painelTopo.add(painelBusca, BorderLayout.NORTH);
        
        labelStatus = new JLabel("Aguardando alvo...", SwingConstants.CENTER);
        labelStatus.setFont(new Font("Arial", Font.BOLD, 12));
        painelTopo.add(labelStatus, BorderLayout.SOUTH);

        // --- PAINEL CENTRAL: CONSOLE VIRTUAL ---
        areaConsole = new JTextArea();
        areaConsole.setEditable(false); // Modo Read-Only (Apenas leitura para o usuário).
        areaConsole.setBackground(Color.BLACK); // Fundo estilo terminal.
        areaConsole.setForeground(new Color(0, 255, 0)); // Fonte verde (estilo hacker/Matrix).
        areaConsole.setFont(new Font("Consolas", Font.PLAIN, 12)); // Fonte monoespaçada para alinhamento perfeito de caracteres.
        
        // Encapsula o JTextArea em um JScrollPane para adicionar barras de rolagem automáticas.
        JScrollPane scrollConsole = new JScrollPane(areaConsole);
        scrollConsole.setBorder(BorderFactory.createTitledBorder("Saída do Sistema (Console)"));

        // --- PAINEL INFERIOR: AÇÕES E CRÉDITOS ---
        JPanel painelInferior = new JPanel(new BorderLayout());

        // GridLayout cria uma matriz perfeita (5 linhas x 2 colunas) com espaçamento de 10px.
        JPanel painelAcoes = new JPanel(new GridLayout(5, 2, 10, 10));
        painelAcoes.setBorder(BorderFactory.createTitledBorder("Ações Remotas"));
        
        // Instanciação dos botões de comando.
        btnRenovarIp = new JButton("Renovar IP");
        btnFlushDNS = new JButton("Limpar Cache DNS");
        btnGpupdate = new JButton("Forçar GPO");
        btnSyncHora = new JButton("Sincronizar Hora");
        btnReiniciarSpooler = new JButton("Reiniciar Spooler");
        btnLimparTemp = new JButton("Limpar %TEMP%");
        btnDescobrirUsuario = new JButton("Usuário Logado");
        btnUptime = new JButton("Verificar Uptime");
        btnReboot = new JButton("Reboot Forçado (PERIGO)");
        btnPingContinuo = new JButton("Ping Contínuo (Iniciar)");
        
        btnReboot.setForeground(Color.RED); // Destaque de advertência (Warning).

        // Desabilita os botões inicialmente (State management preventivo).
        ativarBotoesAcao(false);

        // Adição dos botões preenchendo a matriz da esquerda para a direita, de cima para baixo.
        painelAcoes.add(btnRenovarIp);
        painelAcoes.add(btnFlushDNS);
        painelAcoes.add(btnGpupdate);
        painelAcoes.add(btnSyncHora);
        painelAcoes.add(btnReiniciarSpooler);
        painelAcoes.add(btnLimparTemp);
        painelAcoes.add(btnDescobrirUsuario);
        painelAcoes.add(btnUptime);
        painelAcoes.add(btnReboot);
        painelAcoes.add(btnPingContinuo);

        painelInferior.add(painelAcoes, BorderLayout.CENTER);

        // --- RODAPÉ COM HYPERLINK ---
        JLabel labelCreditos = new JLabel("<html><a href=''>Desenvolvido por Pedro Henrique Gontijo da Cruz</a></html>");
        labelCreditos.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        labelCreditos.setForeground(Color.GRAY);
        labelCreditos.setHorizontalAlignment(SwingConstants.CENTER);
        labelCreditos.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        labelCreditos.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Listener para invocar o navegador padrão do sistema operacional.
        labelCreditos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("http://www.teste.com")); 
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        painelInferior.add(labelCreditos, BorderLayout.SOUTH);

        // --- MONTAGEM FINAL DA JANELA ---
        add(painelTopo, BorderLayout.NORTH);
        add(scrollConsole, BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);

        // Aplica padding global na raiz da janela.
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- BINDING DE EVENTOS (LISTENERS) ---
        botaoConectar.addActionListener(e -> testarConexao());
        btnPingContinuo.addActionListener(e -> togglePingContinuo());
        
        // Expressões Lambda injetando os comandos shell diretamente no método genérico da classe MotorComandos.
        // O parâmetro 'this' passa a referência desta janela para que o Motor possa invocar o logConsole() nela.
        btnRenovarIp.addActionListener(e -> MotorComandos.executar(campoAlvo.getText().trim(), "Renovar IP", "ipconfig /renew", this));
        btnFlushDNS.addActionListener(e -> MotorComandos.executar(campoAlvo.getText().trim(), "Limpar Cache DNS", "ipconfig /flushdns", this));
        btnGpupdate.addActionListener(e -> MotorComandos.executar(campoAlvo.getText().trim(), "Forçar GPO", "gpupdate /force", this));
        btnSyncHora.addActionListener(e -> MotorComandos.executar(campoAlvo.getText().trim(), "Sincronizar Hora", "w32tm /resync", this));
        btnReiniciarSpooler.addActionListener(e -> MotorComandos.executar(campoAlvo.getText().trim(), "Reiniciar Spooler", "net stop spooler & net start spooler", this));
        btnLimparTemp.addActionListener(e -> MotorComandos.executar(campoAlvo.getText().trim(), "Limpar %TEMP%", "del /q/f/s %TEMP%\\*", this));
        btnDescobrirUsuario.addActionListener(e -> MotorComandos.executar(campoAlvo.getText().trim(), "Usuário Logado", "quser", this));
        btnUptime.addActionListener(e -> MotorComandos.executar(campoAlvo.getText().trim(), "Verificar Uptime", "net statistics workstation", this));
        
        // Listener com interceptação modal (JOptionPane) para confirmar ação destrutiva antes de delegar ao Motor.
        btnReboot.addActionListener(e -> {
            int confirmacao = JOptionPane.showConfirmDialog(this, 
                "Tem certeza que deseja REINICIAR FORÇADAMENTE o alvo " + campoAlvo.getText().trim() + "?\nO usuário perderá dados não salvos!", 
                "Atenção - Reboot Remoto", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmacao == JOptionPane.YES_OPTION) {
                MotorComandos.executar(campoAlvo.getText().trim(), "Reboot Forçado", "shutdown /r /f /t 0", this);
            }
        });
    }

    // Método Thread-Safe para anexar texto ao JTextArea.
    public void logConsole(String mensagem) {
        // invokeLater joga a atualização visual para a fila da Event Dispatch Thread (EDT).
        // Isso é OBRIGATÓRIO no Swing se o texto estiver vindo de uma Thread separada (ex: MotorComandos ou Ping).
        SwingUtilities.invokeLater(() -> {
            areaConsole.append(mensagem + "\n");
            areaConsole.setCaretPosition(areaConsole.getDocument().getLength()); // Auto-scroll para a última linha.
        });
    }

    // Método utilitário para gerenciamento de estado da interface.
    private void ativarBotoesAcao(boolean estado) {
        btnRenovarIp.setEnabled(estado);
        btnFlushDNS.setEnabled(estado);
        btnGpupdate.setEnabled(estado);
        btnSyncHora.setEnabled(estado);
        btnReiniciarSpooler.setEnabled(estado);
        btnLimparTemp.setEnabled(estado);
        btnDescobrirUsuario.setEnabled(estado);
        btnUptime.setEnabled(estado);
        btnReboot.setEnabled(estado);
        btnPingContinuo.setEnabled(estado);
    }

    // Método de validação de rede usando processamento assíncrono.
    private void testarConexao() {
        String alvo = campoAlvo.getText().trim();
        if (alvo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite um IP ou Hostname!");
            return;
        }

        // Feedback visual imediato na thread principal.
        labelStatus.setText("Testando conexão com " + alvo + "...");
        labelStatus.setForeground(Color.BLUE);
        botaoConectar.setEnabled(false);
        ativarBotoesAcao(false);
        logConsole("\n>>> Buscando máquina: " + alvo + "...");

        // SwingWorker é a classe oficial do Java para rodar tarefas pesadas no background sem travar a UI.
        // <String (Retorno), Void (Progresso intermediário)>
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            
            @Override
            protected String doInBackground() throws Exception {
                // Esta camada roda em uma Worker Thread isolada.
                try {
                    InetAddress maquina = InetAddress.getByName(alvo); // Resolve o DNS ou IP localmente.
                    
                    if (maquina.isReachable(2000)) { // Dispara um ICMP Echo Request com timeout de 2000ms.
                        String ipResolvido = maquina.getHostAddress();
                        String nomeResolvido = maquina.getHostName(); // Reverse DNS Lookup.
                        
                        // Lógica para formatar a string de resposta demonstrando a resolução cruzada.
                        if (alvo.equals(ipResolvido)) {
                            return "ONLINE (" + ipResolvido + ") -> Nome: " + nomeResolvido;
                        } else {
                            return "ONLINE (" + nomeResolvido + ") -> IP: " + ipResolvido;
                        }
                    } else {
                        return "OFFLINE";
                    }
                } catch (Exception ex) {
                    return "ERRO";
                }
            }

            @Override
            protected void done() {
                // Esta camada roda de volta na EDT (Event Dispatch Thread) de forma segura para atualizar a UI.
                try {
                    String resultado = get(); // Captura o return do doInBackground().
                    botaoConectar.setEnabled(true);
                    
                    // Avalia o payload da resposta e ajusta os estados visuais.
                    if (resultado.startsWith("ONLINE")) {
                        labelStatus.setText(resultado);
                        labelStatus.setForeground(new Color(0, 153, 0)); 
                        logConsole(">>> SUCESSO: Máquina localizada e respondendo.");
                        ativarBotoesAcao(true); // Libera os comandos administrativos.
                    } else if (resultado.equals("OFFLINE")) {
                        labelStatus.setText("Máquina OFFLINE ou bloqueando Ping.");
                        labelStatus.setForeground(Color.RED);
                        logConsole(">>> FALHA: Destino inacessível (Timeout).");
                    } else {
                        labelStatus.setText("Erro ao resolver IP/Hostname.");
                        labelStatus.setForeground(Color.RED);
                        logConsole(">>> ERRO: Não foi possível resolver o DNS.");
                    }
                } catch (Exception ex) {
                    labelStatus.setText("Erro interno ao testar conexão.");
                    botaoConectar.setEnabled(true);
                }
            }
        };
        worker.execute(); // Coloca o Worker na fila de execução do pool de threads do Java.
    }

    // --- LÓGICA DO PING INFINITO (Toggled Threading) ---
    private void togglePingContinuo() {
        if (!pingando) { // ESTADO: INICIAR
            pingando = true; // Altera a flag volátil para manter o loop vivo.
            btnPingContinuo.setText("Parar Ping Contínuo");
            btnPingContinuo.setForeground(Color.RED); // Feedback de ação destrutiva/interrompível.
            logConsole("\n>>> Iniciando Ping Contínuo para " + campoAlvo.getText().trim());

            // Instancia uma Thread crua (Runnable) para o monitoramento contínuo.
            threadPing = new Thread(() -> {
                String alvo = campoAlvo.getText().trim();
                
                // Loop de execução atrelado à flag volátil 'pingando'.
                while (pingando) {
                    try {
                        // Utiliza o binário nativo de ping do SO para capturar métricas reais (TTL, ms).
                        Process process = new ProcessBuilder("ping", "-n", "1", "-w", "1000", alvo).start();
                        
                        // Bufferiza o STDOUT do processo (usando CP850 para suportar acentuação pt-BR do Windows).
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "CP850"));
                        String linha;
                        
                        // Filtra o output cru do ping, exibindo apenas as linhas com dados úteis (ms ou timeouts).
                        while ((linha = reader.readLine()) != null) {
                            if (linha.contains("bytes=") || linha.contains("Esgotado") || linha.contains("Inacessível")) {
                                logConsole(linha.trim());
                            }
                        }
                        process.waitFor(); // Aguarda a finalização deste pulso de ping.
                        Thread.sleep(1000); // Bloqueia a thread por 1000ms (1 segundo) para cadenciar os disparos e não floodar a rede.
                    } catch (Exception ex) {
                        logConsole(">>> Erro no processo de ping.");
                        pingando = false; // Quebra o loop em caso de anomalia severa (ex: InterruptedException).
                    }
                }
                logConsole(">>> Ping Contínuo Encerrado.");
            });
            threadPing.start(); // Inicia a Thread do ping.
            
        } else { // ESTADO: PARAR
            pingando = false; // O loop 'while(pingando)' na thread isolada lerá isso e quebrará o ciclo pacificamente.
            btnPingContinuo.setText("Ping Contínuo (Iniciar)");
            btnPingContinuo.setForeground(null); // Remove a cor vermelha, voltando ao look-and-feel nativo.
            
            // Segurança extra: se a Thread estiver travada no sleep(), o interrupt() acorda ela à força.
            if (threadPing != null) {
                threadPing.interrupt();
            }
        }
    }
}