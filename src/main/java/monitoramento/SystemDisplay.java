package monitoramento;

import javax.swing.*;
import java.awt.*;
import java.util.Map; // Importa a classe Map

public class SystemDisplay implements SystemObserver {
    private JFrame frame;
    private JLabel systemInfoLabel;
    private JLabel metricsLabel; // Rótulo único para as métricas (CPU, GPU, RAM)
    private JTextArea storageInfoArea;

    public SystemDisplay() {
        // Configura a interface gráfica principal
        frame = new JFrame("Monitoramento de Sistema em Tempo Real");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 250); // Define o tamanho da janela
        frame.setLayout(new BorderLayout()); // Usa um layout estruturado

        // Painel superior para informações do sistema (expandido)
        JPanel systemPanel = new JPanel();
        systemPanel.setLayout(new BorderLayout());
        systemPanel.setBorder(BorderFactory.createTitledBorder("Informações do Sistema"));
        systemInfoLabel = new JLabel("<html>Carregando informações...<br><br></html>", JLabel.CENTER);
        systemInfoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        systemPanel.add(systemInfoLabel, BorderLayout.CENTER);

// Painel ultra-compacto para métricas principais
        JPanel metricsPanel = new JPanel(null); // Define layout nulo para controle manual do tamanho
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Métricas de Uso"));
        metricsPanel.setPreferredSize(new Dimension(100, 50)); // Define o tamanho fixo do painel

// Configura o JLabel com tamanho compacto
        metricsLabel = new JLabel("CPU: --% | GPU: Indisponível | RAM: --%", JLabel.CENTER);
        metricsLabel.setFont(new Font("Arial", Font.PLAIN, 14)); // Fonte menor
        metricsLabel.setBounds(10, 10, 300, 30); // Define tamanho e posição do rótulo

// Adiciona o JLabel ao painel
        metricsPanel.add(metricsLabel);

        // Painel inferior para informações de armazenamento (detalhado)
        JPanel storagePanel = new JPanel();
        storagePanel.setLayout(new BorderLayout());
        storagePanel.setBorder(BorderFactory.createTitledBorder("Armazenamento"));
        storageInfoArea = new JTextArea("Calculando...");
        storageInfoArea.setFont(new Font("Arial", Font.PLAIN, 12));
        storageInfoArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(storageInfoArea);
        storagePanel.add(scrollPane, BorderLayout.CENTER);

        // Adiciona os painéis à janela principal
        frame.add(systemPanel, BorderLayout.NORTH); // Expandido para informações do sistema
        frame.add(metricsPanel, BorderLayout.CENTER); // Ultra-compacto para métricas
        frame.add(storagePanel, BorderLayout.SOUTH); // Espaço detalhado para armazenamento

        // Exibe a interface gráfica
        frame.setVisible(true);

        // Configura as informações do sistema para exibição inicial
        displaySystemDetails();
    }

    private void displaySystemDetails() {
        Map<String, String> systemDetails = SystemDetails.getSystemDetails();

        // Extrai os valores
        String serialNumber = systemDetails.get("serialNumber"); // Número de Série do Hardware
        String computerName = systemDetails.get("computerName"); // Nome do Computador
        String systemName = systemDetails.get("systemName"); // Nome do Sistema Operacional
        String systemVersion = systemDetails.get("systemVersion"); // Versão do Sistema Operacional
        String registeredOwner = systemDetails.getOrDefault("registeredOwner", "Não disponível"); // Proprietário Registrado (ou padrão)

        // Atualiza o JLabel com os dados formatados
        systemInfoLabel.setText(String.format(
                "<html>Nome: %s<br>Sistema Operacional: %s<br>Versão: %s<br>Número de Série: %s<br>Proprietário Registrado: %s</html>",
                computerName, systemName, systemVersion, serialNumber, registeredOwner
        ));
    }

    @Override
    public void update(String cpuData, String gpuData, String ramData, String storageData) {
        // Atualiza os dados exibidos
        SwingUtilities.invokeLater(() -> {
            metricsLabel.setText(String.format("CPU: %s | GPU: %s | RAM: %s", cpuData, gpuData, ramData));
            storageInfoArea.setText(storageData);
        });
    }
}