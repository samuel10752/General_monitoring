package monitoramento;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AgentMonitor {
    private static AgentMonitor instance;
    private static SystemData systemData;
    private ScheduledExecutorService scheduler;
    private HardwareAbstractionLayer hardware;
    private static final String LOG_FILE_PATH = "logs" + File.separator + "monitoramento_log.txt"; // Único log

    private AgentMonitor() {
        SystemInfo systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.scheduler = Executors.newScheduledThreadPool(1);

        // Cria a pasta de logs, se não existir
        createLogFolder();
    }

    public void saveLog(String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) { // 'true' para append
            writer.write(data);
            writer.newLine(); // Adiciona uma nova linha ao final
        } catch (IOException e) {
            System.out.println("Erro ao salvar o log: " + e.getMessage());
        }
    }

    private void createLogFolder() {
        File logFolder = new File("logs");
        if (!logFolder.exists()) {
            boolean created = logFolder.mkdirs(); // Cria a pasta, se não existir
            if (created) {
                System.out.println("Pasta de logs criada: logs");
            } else {
                System.out.println("Falha ao criar a pasta de logs.");
            }
        }
    }

    public static AgentMonitor getInstance() {
        if (instance == null) {
            instance = new AgentMonitor();
        }
        return instance;
    }

    public void startMonitoring(SystemData systemData) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Coleta de dados
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String cpuUsage = String.format("%.1f%%", getCpuUsage());
                String ramUsage = String.format("%.1f%%", getRamUsage());
                String storageInfo = getStorageInfo();

                // Formata os dados para o log
                String logData = String.format(
                        "[%s] Dados Atualizados:\nCPU: %s\nRAM: %s\nArmazenamento:\n%s\n-----------------------------------",
                        timestamp, cpuUsage, ramUsage, storageInfo);

                // Salva os dados no arquivo de log único
                saveLog(logData);

                System.out.println(logData); // Exibe os dados no console
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS); // A cada 5 segundos
    }

    public void stopMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("Monitoramento encerrado.");
        }
    }

    private double getCpuUsage() {
        CentralProcessor processor = hardware.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(1000); // Aguarda 1 segundo para calcular o uso
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long[] currTicks = processor.getSystemCpuLoadTicks();

        long totalCpuTime = 0;
        long idleCpuTime = 0;
        for (int i = 0; i < currTicks.length; i++) {
            totalCpuTime += (currTicks[i] - prevTicks[i]);
            if (i == CentralProcessor.TickType.IDLE.getIndex()) {
                idleCpuTime += (currTicks[i] - prevTicks[i]);
            }
        }

        return 100.0 * (totalCpuTime - idleCpuTime) / totalCpuTime;
    }

    private double getRamUsage() {
        GlobalMemory memory = hardware.getMemory();
        long totalMemory = memory.getTotal();
        long usedMemory = totalMemory - memory.getAvailable();
        return ((double) usedMemory / totalMemory) * 100;
    }

    private String getStorageInfo() {
        StringBuilder storageDetails = new StringBuilder();
        SystemInfo systemInfo = new SystemInfo();
        FileSystem fileSystem = systemInfo.getOperatingSystem().getFileSystem();

        for (OSFileStore fileStore : fileSystem.getFileStores()) {
            long totalSpace = fileStore.getTotalSpace() / (1024 * 1024 * 1024); // GB
            long usableSpace = fileStore.getUsableSpace() / (1024 * 1024 * 1024); // GB
            long usedSpace = totalSpace - usableSpace;

            storageDetails.append(String.format(
                    "Disco: %s, Usado: %dGB, Disponível: %dGB, Total: %dGB\n",
                    fileStore.getName(), usedSpace, usableSpace, totalSpace));
        }
        return storageDetails.toString();
    }

    public static void main(String[] args) {
        AgentMonitor monitor = AgentMonitor.getInstance();
        monitor.startMonitoring(systemData);

        // Simula monitoramento por 30 segundos
        try {
            Thread.sleep(30000); // Aguarda enquanto o monitoramento é executado
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        monitor.stopMonitoring();
        System.out.println("Monitoramento encerrado.");
    }
}