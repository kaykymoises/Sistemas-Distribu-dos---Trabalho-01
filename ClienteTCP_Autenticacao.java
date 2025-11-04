import java.net.*;
import java.io.*;
import java.util.Scanner;

public class ClienteTCP_Autenticacao {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 12345;
        
        // Mantemos o scanner fora do loop
        try (Scanner scanner = new Scanner(System.in);
             // 1. ABRIMOS O SOCKET E OS STREAMS APENAS UMA VEZ!
             Socket socket = new Socket(serverAddress, serverPort);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            int codigoStatus = 0; // Inicializa o status
            
            // LOOP PRINCIPAL: Continua enquanto o códigoStatus não for 200 (Sucesso)
            do {
                System.out.println("\n--- TENTATIVA DE LOGIN ---");
                System.out.print("Email (admin@teste.com ou user@teste.com): ");
                String emailTentativa = scanner.nextLine();
                
                System.out.print("Senha (123 ou 456): ");
                String senhaTentativa = scanner.nextLine();

                // 1. Cliente EMPACOTA e envia a requisição de Login
                out.writeInt(1); // Código de Operação: Login
                out.writeUTF(emailTentativa);
                out.writeUTF(senhaTentativa);
                out.flush();

                // 2. Cliente DESEMPACOTA a resposta do servidor
                codigoStatus = in.readInt();

                if (codigoStatus == 200) {
                    // Lógica de SUCESSO (continua a mesma)
                    String tipoUsuario = in.readUTF(); 
                    long id = in.readLong();
                    String nome = in.readUTF();
                    
                    System.out.println("\n Login SUCESSO!");
                    System.out.println("Bem-vindo(a): " + nome);
                    System.out.println("Seu perfil: " + tipoUsuario);
                    
                    if (tipoUsuario.equals("Administrador")) {
                        new Administrador(id, emailTentativa, nome);
                    } else if (tipoUsuario.equals("Visitante")) {
                        new Visitante(id, emailTentativa, nome);
                    }
                    
                } else if (codigoStatus == 401) {
                    // Resposta de FALHA: O loop continua
                    System.out.println("\n Login FALHOU. Credenciais inválidas. Tente novamente.");
                }
                
            } while (codigoStatus != 200);

        } catch (ConnectException e) {
            System.err.println("ERRO FATAL: O servidor de autenticação não está ativo na porta " + serverPort);
        } catch (IOException e) {
            System.err.println("ERRO FATAL de comunicação com o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}