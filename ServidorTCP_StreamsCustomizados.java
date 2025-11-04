import java.net.*;
import java.io.*;

public class ServidorTCP_StreamsCustomizados {

    // AGORA INCLUI UMA SUBCLASSE (Visitante) para o teste 3.d
    private static final Usuario[] DADOS_ENVIO = { 
        new Administrador(300, "server@envio.com", "Server Admin"), // Subclasse para teste
        new Visitante(400, "server@envio.com", "Server Guest") 
    };

    public static void main(String[] args) {
        // Requisito 2.b.iii (Servidor recebe array)
        new Thread(() -> iniciarRecebimento(12347, "Recebimento (2.b.iii)")).start(); 
        // Requisito 3.d (Servidor envia array)
        new Thread(() -> iniciarEnvio(12348, "Emissor (3.d)")).start();        
    }
    
    // ... (Métodos iniciarRecebimento e iniciarEnvio permanecem INALTERADOS) ...
    private static void iniciarRecebimento(int porta, String nome) {
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("\n[" + nome + " - Porta " + porta + "] Aguardando Cliente...");
            try (Socket clientSocket = serverSocket.accept();
                 UsuarioInputStream uis = new UsuarioInputStream(clientSocket.getInputStream())) {
                
                System.out.println("--- Teste " + nome + ": Lendo Array do Cliente ---");
                Usuario[] lidos = uis.readUsuarios();
                System.out.println("Sucesso: Array de " + lidos.length + " usuários lido.");
                
            } catch (IOException e) { /* ... */ }
        } catch (IOException e) { /* ... */ }
    }

    private static void iniciarEnvio(int porta, String nome) {
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("\n[" + nome + " - Porta " + porta + "] Aguardando Cliente...");
            try (Socket clientSocket = serverSocket.accept();
                 UsuarioOutputStream uos = new UsuarioOutputStream(DADOS_ENVIO, DADOS_ENVIO.length, clientSocket.getOutputStream())) {
                
                System.out.println("--- Teste " + nome + ": Enviando Array para o Cliente ---");
                uos.writeUsuarios();
                System.out.println("Sucesso: Array de usuários enviado.");
                
            } catch (IOException e) { /* ... */ }
        } catch (IOException e) { /* ... */ }
    }
}