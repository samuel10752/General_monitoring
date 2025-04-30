package monitoramento;

import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;

public class SystemInfoServer {
    public static void startServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/systeminfo", exchange -> {
            try {
                // Obtém informações do sistema e hardware
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

                // Obtém localização via API ip-api.com
                System.out.println("Obtendo localização via IP...");
                double[] location = fetchLocationFromIP();
                double lat = location[0];
                double lon = location[1];
                String mapsLink = lat != 0.0 && lon != 0.0
                        ? "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon
                        : "Localização indisponível";

                // Monta resposta JSON
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

    // Método para obter localização via ip-api.com
    private static double[] fetchLocationFromIP() {
        double[] location = {0.0, 0.0}; // Valores padrão

        try {
            URL url = new URL("http://ip-api.com/json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            // Processa o JSON da API
            String response = responseBuilder.toString();
            JSONObject jsonObject = new JSONObject(response);
            location[0] = jsonObject.getDouble("lat");
            location[1] = jsonObject.getDouble("lon");

        } catch (Exception e) {
            System.err.println("Erro ao obter localização via IP: " + e.getMessage());
        }
        return location;
    }
}