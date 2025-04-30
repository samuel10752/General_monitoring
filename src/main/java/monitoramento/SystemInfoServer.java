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

        // Endpoint /systeminfo
        server.createContext("/systeminfo", exchange -> {
            try {
                // Sistema e hardware
                SystemInfo systemInfo = new SystemInfo();
                OperatingSystem os = systemInfo.getOperatingSystem();
                CentralProcessor processor = systemInfo.getHardware().getProcessor();
                GlobalMemory memory = systemInfo.getHardware().getMemory();
                List<OSFileStore> fileStores = os.getFileSystem().getFileStores();

                // Montagem de informações de armazenamento
                StringBuilder storageBuilder = new StringBuilder();
                for (OSFileStore fileStore : fileStores) {
                    if (storageBuilder.length() > 0) {
                        storageBuilder.append(",");
                    }
                    storageBuilder.append(String.format(
                            "{ \"name\": \"%s\", \"totalSpace\": %.2f GB, \"freeSpace\": %.2f GB}",
                            fileStore.getMount(),
                            fileStore.getTotalSpace() / (1024.0 * 1024 * 1024),
                            fileStore.getUsableSpace() / (1024.0 * 1024 * 1024)
                    ));
                }

                // Latitude e longitude fixas (exemplo: São Paulo)
                double lat = -23.550520; // Latitude configurada
                double lon = -46.633308; // Longitude configurada
                String mapsLink = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon;

                // Montagem do JSON final
                String response = String.format(
                        "{\n" +
                                "    \"hostName\": \"%s\",\n" +
                                "    \"fabricante\": \"%s\",\n" +
                                "    \"modelo\": \"%s\",\n" +
                                "    \"processador\": {\n" +
                                "        \"nome\": \"%s\",\n" +
                                "        \"nucleosFisicos\": %d,\n" +
                                "        \"nucleosLogicos\": %d\n" +
                                "    },\n" +
                                "    \"memoria\": {\n" +
                                "        \"total\": %.2f GB\n" +
                                "    },\n" +
                                "    \"sistemaOperacional\": {\n" +
                                "        \"nome\": \"%s\",\n" +
                                "        \"versao\": \"%s\"\n" +
                                "    },\n" +
                                "    \"localizacao\": {\n" +
                                "        \"latitude\": %f,\n" +
                                "        \"longitude\": %f,\n" +
                                "        \"linkGoogleMaps\": \"%s\"\n" +
                                "    },\n" +
                                "    \"armazenamento\": [%s]\n" +
                                "}",
                        os.getNetworkParams().getHostName(),
                        systemInfo.getHardware().getComputerSystem().getManufacturer(),
                        systemInfo.getHardware().getComputerSystem().getModel(),
                        processor.getProcessorIdentifier().getName(),
                        processor.getPhysicalProcessorCount(),
                        processor.getLogicalProcessorCount(),
                        memory.getTotal() / (1024.0 * 1024 * 1024),
                        os.getFamily(),
                        os.getVersionInfo().toString(),
                        lat,
                        lon,
                        mapsLink,
                        storageBuilder.toString()
                );

                // Envio da resposta
                byte[] responseBytes = response.getBytes();
                exchange.sendResponseHeaders(200, responseBytes.length);
                OutputStream osStream = exchange.getResponseBody();
                osStream.write(responseBytes);
                osStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor HTTP iniciado na porta 8080...");
    }
}