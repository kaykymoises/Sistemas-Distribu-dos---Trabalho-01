// Arquivo: TesteLeitura_R3bcd.java
import java.io.*;
import java.net.*;

public class TesteLeitura_R3bcd {

    private static void ler(InputStream is, String fonte) throws IOException {
        try (UsuarioInputStream uis = new UsuarioInputStream(is)) {
            Usuario[] lidos = uis.readUsuarios();
            System.out.println("Sucesso na leitura de " + lidos.length + " objetos da Fonte: " + fonte);
        } catch (EOFException e) {
             System.err.println("Erro: Falha na leitura, dados incompletos ou vazios.");
        }
    }
    
    public static void main(String[] args) throws IOException {
        
        // c. Teste: Arquivo (FileInputStream)
        String nomeArquivo = "test_output.dat";
        System.out.println("\n--- 3.c: Teste FileInputStream ---");
        try (FileInputStream fis = new FileInputStream(nomeArquivo)) {
            ler(fis, "Arquivo");
        } catch (FileNotFoundException e) {
             System.err.println("ERRO: Arquivo 'test_output.dat' não encontrado. Execute o TesteEscrita_R2b primeiro.");
        }
        
        // b. Teste: Entrada Padrão (System.in)
        System.out.println("\n--- 3.b: Teste System.in ---");
        System.out.println("REQUER: Redirecionamento de arquivo (ex: java TesteLeitura_R3bcd < test_output.dat)");
        if (System.in.available() > 0) {
            ler(System.in, "Entrada Padrão");
        } else {
             System.out.println("Nenhum dado na entrada padrão. Pule o teste.");
        }
        
        // d. Teste: Servidor Remoto (TCP) - Leitura
        System.out.println("\n--- 3.d: Teste TCP (Leitura) ---");
        try (Socket socket = new Socket("localhost", 12348)) {
            System.out.println("Conectado ao Servidor EMISSOR (12348)...");
            ler(socket.getInputStream(), "Servidor Remoto TCP");
            System.out.println("Sucesso: Dados lidos do Servidor EMISSOR (12348).");
            
        } catch (ConnectException e) {
            System.err.println("ERRO: Servidor Emissor (12348) Inativo.");
        }
    }
}