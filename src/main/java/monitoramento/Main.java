package monitoramento;

public class Main {
    public static void main(String[] args) {
        // Inicia o servidor HTTP em uma thread separada
        Thread serverThread = new Thread(() -> {
            try {
                SystemInfoServer.startServer(); // Inicializa o servidor HTTP
            } catch (Exception e) {
                System.err.println("Erro ao iniciar o servidor HTTP:");
                e.printStackTrace();
            }
        });
        serverThread.start();
        System.out.println("Servidor HTTP em execução na porta 8080...");

        // Aguarda para garantir que o servidor esteja pronto
        try {
            Thread.sleep(2000); // Pausa de 2 segundos para o servidor iniciar
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Cria e exibe a interface gráfica para monitoramento
        SystemDisplay display = new SystemDisplay();

        // Inicia o monitoramento em tempo real
        SystemData systemData = new SystemData();
        systemData.addObserver(display); // Adiciona a interface como observadora

        AgentMonitor agent = AgentMonitor.getInstance();
        agent.startMonitoring(systemData); // Inicia o monitoramento

        // Aguarda indefinidamente enquanto o monitoramento ocorre
        try {
            System.out.println("Servidor, monitoramento e interface ativa. Pressione Ctrl+C para encerrar.");
            Thread.sleep(Long.MAX_VALUE); // Mantém o programa rodando
        } catch (InterruptedException e) {
            System.out.println("Encerrando monitoramento e servidor.");
        } finally {
            agent.stopMonitoring();
            System.out.println("Monitoramento encerrado.");
        }
    }
}