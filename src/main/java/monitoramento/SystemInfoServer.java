package monitoramento;

import com.sun.net.httpserver.HttpServer;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;

import org.json.JSONObject;

public class SystemInfoServer {
    public static void startServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Endpoint /systeminfo
        server.createContext("/systeminfo", exchange -> {
            try {
                SystemInfo systemInfo = new SystemInfo();
                OperatingSystem os = systemInfo.getOperatingSystem();
                CentralProcessor processor = systemInfo.getHardware().getProcessor();
                GlobalMemory memory = systemInfo.getHardware().getMemory();
                List<OSFileStore> fileStores = os.getFileSystem().getFileStores();

                StringBuilder storageBuilder = new StringBuilder();
                for (OSFileStore fileStore : fileStores) {
                    if (storageBuilder.length() > 0) {
                        storageBuilder.append(",");
                    }
                    // Corrigido para exibir o total e o espaço livre em GB
                    storageBuilder.append(String.format(
                            "{ \"name\": \"%s\", \"totalSpace\": %.2f GB, \"freeSpace\": %.2f GB}",
                            fileStore.getMount(),
                            fileStore.getTotalSpace() / (1024.0 * 1024 * 1024), // Em GB
                            fileStore.getUsableSpace() / (1024.0 * 1024 * 1024)  // Em GB
                    ));
                }

                // Coleta as informações de localização do ipinfo.io
                String locationJson = getLocationInfo();
                JSONObject location = new JSONObject(locationJson);

                // Verificando as chaves 'city' e 'country' e tratando a latitude e longitude
                String cidade = location.optString("city", "Desconhecida");
                String pais = location.optString("country", "Desconhecido");
                double lat = 0; // Variáveis para latitude e longitude
                double lon = 0;

                // Checando se latitude e longitude estão disponíveis
                if (location.has("loc")) {
                    String[] loc = location.getString("loc").split(",");
                    lat = Double.parseDouble(loc[0]);
                    lon = Double.parseDouble(loc[1]);
                }

                String mapsLink = "https://www.openstreetmap.org/?mlat=" + lat + "&mlon=" + lon + "#map=12/" + lat + "/" + lon;

                // Modificado para mostrar a memória RAM em GB
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
                                "         \"total\": %.2f GB\n" +  // Aqui a memória em GB
                                "    },\n" +
                                "    \"sistemaOperacional\": {\n" +
                                "        \"nome\": \"%s\",\n" +
                                "        \"versao\": \"%s\"\n" +
                                "    },\n" +
                                "    \"localizacao\": {\n" +
                                "        \"cidade\": \"%s\",\n" +
                                "        \"pais\": \"%s\",\n" +
                                "        \"latitude\": %f,\n" +
                                "        \"longitude\": %f,\n" +
                                "        \"linkOpenStreetMap\": \"%s\"\n" +
                                "    },\n" +
                                "    \"armazenamento\": [%s]\n" +
                                "}",
                        os.getNetworkParams().getHostName(),
                        systemInfo.getHardware().getComputerSystem().getManufacturer(),
                        systemInfo.getHardware().getComputerSystem().getModel(),
                        processor.getProcessorIdentifier().getName(),
                        processor.getPhysicalProcessorCount(),
                        processor.getLogicalProcessorCount(),
                        memory.getTotal() / (1024.0 * 1024 * 1024), // Memória total em GB
                        os.getFamily(),
                        os.getVersionInfo().toString(),
                        cidade,
                        pais,
                        lat,
                        lon,
                        mapsLink,
                        storageBuilder.toString()
                );

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

        // Endpoint /sendLocation
        server.createContext("/sendLocation", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                    StringBuilder body = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }

                    JSONObject json = new JSONObject(body.toString());
                    double lat = json.getDouble("latitude");
                    double lon = json.getDouble("longitude");

                    System.out.printf("Localização exata recebida: LAT %.6f, LON %.6f%n", lat, lon);

                    String response = "https://www.openstreetmap.org/?mlat=" + lat + "&mlon=" + lon + "#map=12/" + lat + "/" + lon;
                    byte[] responseBytes = response.getBytes();
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(responseBytes);
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor HTTP iniciado na porta 8080...");
    }

    private static String getLocationInfo() throws Exception {
        // Substitua 'YOUR_API_KEY' pela chave de API real, ou mantenha o nome para ignorar a chave
        String apiKey = "YOUR_API_KEY"; // Substitua isso pela sua chave de API real

        String urlString;
        if ("YOUR_API_KEY".equals(apiKey)) {
            // Se a chave for o valor default "YOUR_API_KEY", usamos a URL sem chave de API
            urlString = "https://ipinfo.io/json";
        } else {
            // Caso contrário, usamos a chave de API real
            urlString = "https://ipinfo.io/json?token=" + apiKey;
        }

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder responseBuilder = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            responseBuilder.append(inputLine);
        }
        in.close();

        // Retorna a resposta
        return responseBuilder.toString();
    }

}
