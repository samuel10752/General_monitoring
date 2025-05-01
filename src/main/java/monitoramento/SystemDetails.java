package monitoramento;

import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import oshi.software.os.OperatingSystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

            // Obtendo informações básicas do sistema
            details.put("serialNumber", computerSystem.getSerialNumber()); // Número de Série
            details.put("computerName", operatingSystem.getNetworkParams().getHostName()); // Nome do Computador

            // Obtendo nome correto do sistema operacional via PowerShell
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

            details.put("systemName", osName);
            details.put("systemVersion", osVersion);

            // Obtendo o proprietário registrado via PowerShell
            String owner = "Desconhecido";
            try {
                Process processOwner = Runtime.getRuntime().exec("powershell.exe -Command \"(Get-ItemProperty 'HKLM:\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion').RegisteredOwner\"");
                BufferedReader readerOwner = new BufferedReader(new InputStreamReader(processOwner.getInputStream()));
                owner = readerOwner.readLine().trim();
                readerOwner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            details.put("registeredOwner", owner);
        } catch (Exception e) {
            e.printStackTrace();
            details.put("error", "Falha ao obter informações do sistema."); // Tratamento de erro
        }

        return details;
    }
}