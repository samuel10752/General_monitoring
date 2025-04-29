package monitoramento;

public class SystemDisplay implements SystemObserver {
    @Override
    public void update(String cpuData, String gpuData, String ramData, String storageData) {
        System.out.println("Dados Atualizados:");
        System.out.println("CPU: " + cpuData);
        System.out.println("GPU: " + gpuData);
        System.out.println("RAM: " + ramData);
        System.out.println("Armazenamento: " + storageData);
        System.out.println("-----------------------------------");
    }
}