package monitoramento;

import java.util.ArrayList;
import java.util.List;

public class SystemData {
    private List<SystemObserver> observers = new ArrayList<>();
    private String cpuData;
    private String gpuData;
    private String ramData;
    private String storageData; // Dados de armazenamento

    // Adiciona um observador à lista
    public void addObserver(SystemObserver observer) {
        observers.add(observer);
    }

    // Notifica todos os observadores sobre atualizações de dados
    public void notifyObservers() {
        for (SystemObserver observer : observers) {
            observer.update(cpuData, gpuData, ramData, storageData);
        }
    }

    // Atualiza os dados do sistema e notifica os observadores
    public void updateData(String cpuData, String gpuData, String ramData, String storageInfo) {
        this.cpuData = cpuData;
        this.gpuData = gpuData;
        this.ramData = ramData;
        this.storageData = storageInfo; // Corrigido para usar storageInfo
        notifyObservers();
    }
}