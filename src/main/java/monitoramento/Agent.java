package monitoramento;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Agent {
    private static String lastResponse = "{}"; // JSON vazio por padrão

    public static void executeAgent() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://localhost:8080/systeminfo");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // Timeout de conexão (5s)
            connection.setReadTimeout(5000); // Timeout de leitura (5s)

            // Try-with-resources para leitura eficiente
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                lastResponse = response.toString();
                System.out.println("Dados recebidos do servidor: " + lastResponse);
            }

        } catch (Exception e) {
            System.err.println("Erro ao executar Agent: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String getServerDetails() {
        return lastResponse;
    }
}