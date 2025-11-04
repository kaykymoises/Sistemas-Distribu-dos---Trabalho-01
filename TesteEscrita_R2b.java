import java.io.*;
import java.net.*;

public class TesteEscrita_R2b {

    // AGORA INCLUI UMA SUBCLASSE (Administrador) para o teste 2.b
    private static final Usuario[] DADOS = { 
        new Administrador(1, "admin@out.com", "Admin User"), // Subclasse para teste
        new Visitante(2, "user@out.com", "Guest User") 
    };
    private static final int NUM_ENVIAR = 2; // Alterado para 2

    public static void main(String[] args) throws IOException {
        
        // i. Teste: Saída Padrão (System.out)
        System.out.println("\n--- 2.b.i: Teste System.out (Console) ---");
        try (UsuarioOutputStream uos = new UsuarioOutputStream(DADOS, NUM_ENVIAR, System.out)) {
            uos.writeUsuarios();
            System.out.println("Bytes enviados para o console.");
        }
        
        // ii. Teste: Arquivo (FileOutputStream)
        String nomeArquivo = "test_output.dat";
        System.out.println("\n--- 2.b.ii: Teste FileOutputStream ---");
        try (FileOutputStream fos = new FileOutputStream(nomeArquivo);
             UsuarioOutputStream uos = new UsuarioOutputStream(DADOS, NUM_ENVIAR, fos)) {
            uos.writeUsuarios();
            System.out.println("Sucesso: Dados gravados em " + nomeArquivo);
        }
        
        // iii. Teste: Servidor Remoto (TCP)
        System.out.println("\n--- 2.b.iii: Teste TCP (Escrita) ---");
        try (Socket socket = new Socket("localhost", 12347);
             UsuarioOutputStream uos = new UsuarioOutputStream(DADOS, NUM_ENVIAR, socket.getOutputStream())) {
            
            uos.writeUsuarios();
            System.out.println("Sucesso: Dados enviados para a Porta 12347. Verifique o Servidor.");
            
        } catch (ConnectException e) {
            System.err.println("ERRO: Servidor na Porta 12347 Inativo.");
        }
    }
}