package monitoramento;

import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import oshi.software.os.OperatingSystem;

import java.util.HashMap;
import java.util.Map;

public class SystemDetails {
    public static Map<String, String> getSystemDetails() {
        Map<String, String> details = new HashMap<>();

        try {
            // Instância do OSHI para obter detalhes do sistema
            SystemInfo systemInfo = new SystemInfo();
            ComputerSystem computerSystem = systemInfo.getHardware().getComputerSystem();
            OperatingSystem operatingSystem = systemInfo.getOperatingSystem();

            // Capturar informações do hardware e sistema operacional
            details.put("serialNumber", computerSystem.getSerialNumber()); // Número de Série
            details.put("computerName", operatingSystem.getNetworkParams().getHostName()); // Nome do Computador

            // Obter nome completo do sistema operacional
            String systemName = operatingSystem.toString(); // Retorna nome detalhado
            details.put("systemName", systemName);

            // Verificar se é Windows 11 Pro
            if (systemName.contains("Windows 11 Pro")) {
                details.put("isWindows11Pro", "Sim");
            } else {
                details.put("isWindows11Pro", "Não");
            }

            // Obter versão completa do sistema operacional
            String systemVersion = operatingSystem.getVersionInfo().toString();
            details.put("systemVersion", systemVersion);

        } catch (Exception e) {
            e.printStackTrace();
            details.put("error", "Falha ao obter informações do sistema."); // Tratamento de erro
        }

        return details;
    }
}