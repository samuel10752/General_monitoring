package monitoramento;

public interface SystemObserver {
    void update(String cpuData, String gpuData, String ramData, String storageData);
}