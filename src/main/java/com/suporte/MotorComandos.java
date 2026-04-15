package com.suporte;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class MotorComandos {

    // Variável para guardar o caminho do arquivo temporário para não extrair duas vezes
    private static String caminhoPsExecTemp = null;

    // --- NOVA LÓGICA DE EXTRAÇÃO INVISÍVEL ---
    private static synchronized String obterCaminhoPsExec() throws Exception {
        if (caminhoPsExecTemp != null) {
            return caminhoPsExecTemp; // Se já extraiu, só devolve o caminho
        }

        // Puxa o arquivo de dentro do próprio código compilado
        InputStream is = MotorComandos.class.getResourceAsStream("/PsExec.exe");
        if (is == null) {
            throw new Exception("Arquivo PsExec.exe não foi encontrado embutido no sistema!");
        }

        // Cria um arquivo invisível na pasta %TEMP% do Windows
        File arquivoTemp = File.createTempFile("PsExec_Ferramenta", ".exe");
        
        // Magia: Diz pro Windows apagar esse arquivo sozinho quando fecharmos nosso programa
        arquivoTemp.deleteOnExit(); 

        // Copia o arquivo embutido para o arquivo temporário
        Files.copy(is, arquivoTemp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        caminhoPsExecTemp = arquivoTemp.getAbsolutePath();
        return caminhoPsExecTemp;
    }

    public static void executar(String alvo, String nomeAcao, String comandoRemoto, TelaSelecaoMaquina tela) {
        tela.logConsole("\n>>> Iniciando: " + nomeAcao + " no alvo " + alvo + "...");

        new Thread(() -> {
            try {
                String usuarioRaw = SessaoSuporte.getInstancia().getUsuario();
                char[] senhaChar = SessaoSuporte.getInstancia().getSenha();
                String senhaStr = new String(senhaChar); 
                String dominioWindows = System.getenv("USERDOMAIN");
                String usuarioFinal = usuarioRaw;

                if (!usuarioRaw.contains("\\") && !usuarioRaw.contains("@")) {
                    usuarioFinal = dominioWindows + "\\" + usuarioRaw;
                }

                // CHAMA O EXTRATOR ANTES DE MONTAR O COMANDO
                String executavel = obterCaminhoPsExec();

                List<String> comandoPsexec = new ArrayList<>();
                // Agora, em vez de passar só "psexec", passamos o caminho absoluto do arquivo no %TEMP%
                comandoPsexec.add(executavel); 
                comandoPsexec.add("\\\\" + alvo);

                boolean isLocal = alvo.equalsIgnoreCase("localhost") || alvo.equals("127.0.0.1");

                if (!isLocal) {
                    comandoPsexec.add("-u");
                    comandoPsexec.add(usuarioFinal);
                    comandoPsexec.add("-p");
                    comandoPsexec.add(senhaStr);
                }

                comandoPsexec.add("-accepteula");
                comandoPsexec.add("-nobanner");
                comandoPsexec.add("cmd.exe");
                comandoPsexec.add("/c");
                comandoPsexec.add(comandoRemoto);

                ProcessBuilder builder = new ProcessBuilder(comandoPsexec);
                builder.redirectErrorStream(true); 
                Process processo = builder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(processo.getInputStream(), "CP850"));
                String linha;
                while ((linha = reader.readLine()) != null) {
                    if (!linha.trim().isEmpty()) {
                        tela.logConsole("  " + linha);
                    }
                }

                int exitCode = processo.waitFor();
                if (exitCode == 0) {
                    tela.logConsole(">>> SUCESSO: " + nomeAcao + " concluído (Código 0).");
                } else {
                    tela.logConsole(">>> AVISO: " + nomeAcao + " retornou código de erro " + exitCode + ".");
                }

                senhaStr = null; 

            } catch (Exception e) {
                tela.logConsole(">>> ERRO CRÍTICO ao executar " + nomeAcao + ": " + e.getMessage());
            }
        }).start();
    }
}