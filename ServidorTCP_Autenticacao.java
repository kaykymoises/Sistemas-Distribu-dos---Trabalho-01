import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ServidorTCP_Autenticacao {

    // ... (Lista de USUARIOS_CADASTRADOS e função autenticar continuam as mesmas) ...
    private static final List<Usuario> USUARIOS_CADASTRADOS = new ArrayList<>();
    static {
        USUARIOS_CADASTRADOS.add(new Administrador(101, "admin@teste.com", "Gad Admin", "123")); 
        USUARIOS_CADASTRADOS.add(new Visitante(202, "user@teste.com", "Bob Visitante", "456"));
    }
    private static Usuario autenticar(String email, String senha) {
        // ... (Implementação da função autenticar) ...
        for (Usuario u : USUARIOS_CADASTRADOS) {
            if (u.email.equals(email)) {
                if (u.getSenha() != null) {
                    if (u.getSenha().equals(senha)) { 
                        return u;
                    }
                }
                return null;
            }
        }
        return null;
    }
    
    //---------------------------------------------------------

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Servidor de Autenticação escutando...");

            // LOOP EXTERNO: Mantém o servidor rodando (aceita vários clientes ao longo do tempo)
            while (true) {
                
                try (Socket clientSocket = serverSocket.accept(); // Bloqueia e aceita um novo cliente
                     DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                     DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

                    System.out.println("\n[Nova Conexão]: Aguardando tentativas de login...");

                    // LOOP INTERNO: Processa múltiplas requisições do MESMO cliente
                    int codigoOp;
                    while (true) {
                        try {
                            // Tenta ler o código de operação (se falhar, o cliente desconectou)
                            codigoOp = in.readInt(); 
                        } catch (EOFException | SocketException e) {
                            System.out.println("[Conexão Encerrada]: Cliente desconectou.");
                            break; // Sai do loop interno e fecha os streams
                        }
                        
                        if (codigoOp == 1) { // Código de Login
                            String emailTentativa = in.readUTF();
                            String senhaTentativa = in.readUTF();

                            Usuario userLogado = autenticar(emailTentativa, senhaTentativa);

                            if (userLogado != null) {
                                enviarReplySucesso(out, userLogado);
                                System.out.println("Login SUCESSO. Conexão encerrada.");
                                break; // Login bem-sucedido: Sai do loop interno
                            } else {
                                enviarReplyFalha(out);
                                System.out.println("Login FALHOU. Aguardando nova tentativa...");
                                // O loop continua...
                            }
                        } else {
                            System.out.println("Código de operação desconhecido: " + codigoOp);
                        }
                    } // Fim do Loop Interno (Múltiplas Requisições)

                } catch (IOException e) {
                    System.err.println("Erro na comunicação com o cliente: " + e.getMessage());
                }
                
            } // Fim do Loop Externo (Servidor Ativo)

        } catch (IOException e) {
            System.err.println("Erro fatal no Servidor: " + e.getMessage());
        }
    }
    
    // ... (Métodos enviarReplySucesso e enviarReplyFalha) ...
    private static void enviarReplySucesso(DataOutputStream out, Usuario user) throws IOException {
        out.writeInt(200); 
        out.writeUTF(user.getClass().getSimpleName()); 
        out.writeLong(user.id);
        out.writeUTF(user.nome);
        out.flush();
    }
    
    private static void enviarReplyFalha(DataOutputStream out) throws IOException {
        out.writeInt(401); 
        out.flush();
    }
}