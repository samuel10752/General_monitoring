package monitoramento;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Agent {
    private static String lastResponse = "Nenhuma informação disponível."; // Guarda a última resposta

    public static void executeAgent() {
        try {
            System.out.println("Tentando se conectar ao servidor em: http://localhost:8080/systeminfo");

            // URL do servidor
            URL url = new URL("http://localhost:8080/systeminfo");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Lê a resposta do servidor
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            reader.close();

            // Armazena a resposta para o pop-up
            lastResponse = response.toString();

            // Exibe as informações no console
            System.out.println("Informações do sistema obtidas do servidor:");
            System.out.println(lastResponse);
        } catch (Exception e) {
            System.err.println("Erro ao executar o módulo Agent:");
            e.printStackTrace();
        }
    }

    // Método para retornar os detalhes obtidos do servidor
    public static String getServerDetails() {
        return lastResponse;
    }
}