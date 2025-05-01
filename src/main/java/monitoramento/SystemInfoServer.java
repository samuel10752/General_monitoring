package monitoramento;

import com.sun.net.httpserver.HttpServer;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
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
                ComputerSystem computerSystem = systemInfo.getHardware().getComputerSystem();
                List<OSFileStore> fileStores = os.getFileSystem().getFileStores();

                // Obtendo nome correto do sistema operacional e versão via PowerShell
                String osName = "Desconhecido";
                String osVersion = "Desconhecido";

                try {
                    // Obtendo o nome do sistema operacional
                    Process process = Runtime.getRuntime().exec("powershell.exe -Command \"(Get-ComputerInfo | select-object OsName).OsName\"");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    osName = reader.readLine().trim();
                    reader.close();


                    // Obtendo a versão do sistema operacional
                    Process processVersion = Runtime.getRuntime().exec("powershell.exe -Command \"(Get-ItemProperty -Path 'HKLM:\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion').DisplayVersion\"");
                    BufferedReader readerVersion = new BufferedReader(new InputStreamReader(processVersion.getInputStream()));
                    osVersion = readerVersion.readLine().trim();
                    readerVersion.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Obtendo o proprietário registrado do sistema via PowerShell
                String owner = "Desconhecido";
                try {
                    Process process = Runtime.getRuntime().exec("powershell.exe -Command \"(Get-ItemProperty 'HKLM:\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion').RegisteredOwner\"");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    owner = reader.readLine().trim();
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Obtendo número de série do computador
                String serialNumber = computerSystem.getSerialNumber();
                String manufacturer = computerSystem.getManufacturer();
                String model = computerSystem.getModel();

                // Obtendo localização automaticamente via API
                double lat = 0.0, lon = 0.0;
                try {
                    URL url = new URL("http://ip-api.com/json/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String json = reader.readLine();
                    lat = Double.parseDouble(json.split("\"lat\":")[1].split(",")[0]);
                    lon = Double.parseDouble(json.split("\"lon\":")[1].split(",")[0]);
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String mapsLink = String.format("https://www.google.com/maps?q=%f,%f", lat, lon);

                // Formatação das informações de armazenamento
                StringBuilder storageBuilder = new StringBuilder("<ul>");
                for (OSFileStore fileStore : fileStores) {
                    storageBuilder.append(String.format(
                            "<li>Nome: %s<br>Total: %.2f GB<br>Disponivel: %.2f GB</li>",
                            fileStore.getMount(),
                            fileStore.getTotalSpace() / (1024.0 * 1024 * 1024),
                            fileStore.getUsableSpace() / (1024.0 * 1024 * 1024)
                    ));
                }
                storageBuilder.append("</ul>");

                // Monta resposta HTML com informações do sistema e mapa interativo
                String responseHTML = String.format(
                        "<html><head><title>Informacoes do Sistema</title></head><body>" +
                                "<h1>Informacoes do Sistema</h1>" +
                                "<p><strong>Nome do Sistema Operacional:</strong> %s</p>" +
                                "<p><strong>Versao do SO:</strong> %s</p>" +
                                "<p><strong>Proprietario Registrado:</strong> %s</p>" +
                                "<p><strong>Nome do Host:</strong> %s</p>" +
                                "<p><strong>Fabricante:</strong> %s</p>" +
                                "<p><strong>Modelo:</strong> %s</p>" +
                                "<p><strong>Numero de Serie:</strong> %s</p>" +
                                "<p><strong>Processador:</strong> %s</p>" +
                                "<p><strong>Nucleos Fisicos:</strong> %d</p>" +
                                "<p><strong>Nucleos Logicos:</strong> %d</p>" +
                                "<p><strong>Memoria RAM Total:</strong> %.2f GB</p>" +
                                "<h2>Localizacao</h2>" +
                                "<p><strong>Latitude:</strong> %f</p>" +
                                "<p><strong>Longitude:</strong> %f</p>" +
                                "<p><strong>Google Maps:</strong> <a href='%s'>Abrir localizacao</a></p>" +
                                "<div class='map-container'>" +
                                "<iframe width='600' height='400' src='https://www.google.com/maps?q=%f,%f&output=embed' frameborder='0' allowfullscreen></iframe>" +
                                "</div>" +
                                "<h2>Armazenamento</h2>" +
                                "%s" +
                                "</body></html>",
                        osName, // Nome correto do sistema operacional
                        osVersion, // Versão correta do sistema
                        owner, // Proprietário registrado
                        os.getNetworkParams().getHostName(),
                        manufacturer,
                        model,
                        serialNumber, // Número de série do sistema
                        processor.getProcessorIdentifier().getName(),
                        processor.getPhysicalProcessorCount(),
                        processor.getLogicalProcessorCount(),
                        memory.getTotal() / (1024.0 * 1024 * 1024),
                        lat,
                        lon,
                        mapsLink,
                        lat,
                        lon,
                        storageBuilder.toString()
                );

                // Envia a resposta como HTML
                byte[] responseBytes = responseHTML.getBytes();
                exchange.getResponseHeaders().set("Content-Type", "text/html");
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