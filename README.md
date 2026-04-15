Descrição Técnica
Solução desktop desenvolvida em Java para automação de rotinas de suporte técnico em ambientes de rede baseados em Microsoft Active Directory. A aplicação integra funcionalidades de diagnóstico de rede e gerência de estações através da execução remota de comandos, utilizando o binário PsExec de forma encapsulada.

Arquitetura e Implementação
O projeto foi estruturado seguindo padrões de design e segurança para garantir portabilidade e integridade dos dados:

Encapsulamento de Binários: O executável PsExec.exe é tratado como um recurso interno (embedded resource). Em tempo de execução, o sistema extrai o binário para o diretório temporário do sistema operacional (%TEMP%) e utiliza a flag deleteOnExit para garantir a limpeza do ambiente após o encerramento da sessão.

Gerenciamento de Memória (Segurança): Credenciais de autenticação são manipuladas através de char[] (arrays de caracteres) em vez de objetos String, permitindo a limpeza manual dos dados na memória RAM via Arrays.fill() para mitigar ataques de despejo de memória (Memory Dump).

Concorrência: Operações de rede e execução de comandos são delegadas a Threads secundárias e instâncias de SwingWorker, impedindo o bloqueio da Event Dispatch Thread (EDT) e mantendo a responsabilidade da interface gráfica.

Integração com SO: O sistema identifica dinamicamente o domínio do usuário através da variável de ambiente USERDOMAIN, facilitando a autenticação em larga escala no AD.

Funcionalidades Implementadas
Diagnóstico de Rede: Teste de conectividade (ICMP) e monitoramento de latência via Ping contínuo em Thread isolada.

Gerenciamento de IP: Comandos remotos para renovação de concessão DHCP e limpeza de cache DNS (FlushDNS).

Manutenção de Serviços: Reinicialização forçada do Spooler de Impressão e atualização de diretivas de grupo (GPUpdate).

Auditoria: Identificação de usuários logados via quser e verificação de tempo de atividade (Uptime).

Interface Gráfica: Interface customizada com a biblioteca FlatLaf, provendo modo escuro nativo e console de saída de sistema (STDOUT) integrado com codificação CP850.

Requisitos de Sistema
Ambiente de Execução: Java Runtime Environment (JRE) 17 ou superior.

Rede: Acesso administrativo às máquinas alvo (porta TCP 445 aberta e File Sharing habilitado).

Compilação: Apache Maven para gerenciamento de dependências e build do artefato.

Instruções de Build
Para gerar o artefato executável, utilize o comando:

Bash
mvn clean package
O arquivo .jar resultante conterá todas as dependências e recursos necessários para execução stand-alone.

Desenvolvido por: Pedro Henrique Gontijo da Cruz
Área: Tecnologia em Análise e Desenvolvimento de Sistemas (TADS)
