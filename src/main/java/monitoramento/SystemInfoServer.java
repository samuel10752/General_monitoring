package monitoramento;

import com.sun.net.httpserver.HttpServer;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

public class SystemInfoServer {
    public static void startServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/systeminfo", exchange -> {
            try {
                // Coleta informações do sistema usando OSHI
                SystemInfo systemInfo = new SystemInfo();
                OperatingSystem os = systemInfo.getOperatingSystem();
                CentralProcessor processor = systemInfo.getHardware().getProcessor();
                GlobalMemory memory = systemInfo.getHardware().getMemory();
                List<OSFileStore> fileStores = os.getFileSystem().getFileStores();

                // Monta a resposta em JSON para armazenamento
                StringBuilder storageBuilder = new StringBuilder();
                for (OSFileStore fileStore : fileStores) {
                    if (storageBuilder.length() > 0) {
                        storageBuilder.append(",");
                    }
                    storageBuilder.append(String.format(
                            "{ \"name\": \"%s GB\", \"totalSpace\": %d GB, \"freeSpace\": %d GB}",
                            fileStore.getMount(),
                            fileStore.getTotalSpace() / (1024 * 1024), // Total em MB
                            fileStore.getUsableSpace() / (1024 * 1024) // Livre em MB
                    ));
                }

                // Monta o JSON completo com todas as informações traduzidas
                String response = String.format(
                        "{\n" +
                                "    \"hostName\": \"%s\" \n" +
                                "    \"fabricante\": \"%s\" \n" +
                                "    \"modelo\": \"%s\" \n" +
                                "    \"processador\": {\n" +
                                "        \"nome\": \"%s\" \n" +
                                "        \"nucleosFisicos\": %d \n" +
                                "        \"nucleosLogicos\": %d \n" +
                                "    },\n" +
                                "    \"memoria\": {\n" +
                                "        \"total\": %d GB\n" +
                                "    },\n" +
                                "    \"sistemaOperacional\": {\n" +
                                "        \"nome\": \"%s\" \n" +
                                "        \"versão\": \"%s\" \n" +
                                "    },\n" +
                                "    \"armazenamento\": [%s]\n" +
                                "}",
                        os.getNetworkParams().getHostName(), // Nome do Computador
                        systemInfo.getHardware().getComputerSystem().getManufacturer(), // Fabricante
                        systemInfo.getHardware().getComputerSystem().getModel(), // Modelo
                        processor.getProcessorIdentifier().getName(), // Nome do Processador
                        processor.getPhysicalProcessorCount(), // Núcleos Físicos
                        processor.getLogicalProcessorCount(), // Núcleos Lógicos
                        memory.getTotal() / (1024 * 1024), // Memória Total em MB
                        os.getFamily(), // Nome do Sistema Operacional
                        os.getVersionInfo().toString(), // Versão do Sistema Operacional
                        storageBuilder.toString() // Detalhes do Armazenamento
                );

                // Envia a resposta ao cliente
                byte[] responseBytes = response.getBytes();
                exchange.sendResponseHeaders(200, responseBytes.length); // Envia o tamanho exato da resposta
                OutputStream osStream = exchange.getResponseBody();
                osStream.write(responseBytes);
                osStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1); // Responde com erro interno
            }
        });

        server.setExecutor(null); // Usa o executor padrão
        server.start();
        System.out.println("Servidor HTTP iniciado na porta 8080...");
    }
}