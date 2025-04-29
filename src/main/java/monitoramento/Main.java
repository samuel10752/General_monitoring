package monitoramento;

public class Main {
    public static void main(String[] args) {
        SystemData systemData = new SystemData();
        SystemDisplay display = new SystemDisplay();

        systemData.addObserver(display);

        // Instância Singleton do AgentMonitor
        AgentMonitor agent = AgentMonitor.getInstance();
        agent.startMonitoring(systemData);

        // Simula a execução do monitoramento por 30 segundos
        try {
            Thread.sleep(30000); // Aguarda enquanto o monitoramento é executado
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Finaliza o monitoramento
        agent.stopMonitoring();
        System.out.println("Monitoramento encerrado.");
    }
}