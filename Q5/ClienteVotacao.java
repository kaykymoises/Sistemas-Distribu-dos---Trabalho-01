// Arquivo: ClienteVotacao.java (Versão Final, Estável com Sessões Curtas e Correção do Sair)

import java.net.*;
import java.io.*;
import java.util.*;

public class ClienteVotacao {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORTA_TCP = 12349;
    
    private static final long PRAZO_FINAL = System.currentTimeMillis() + (5 * 60 * 1000); 

    private static MulticastListener listener;

    public static void main(String[] args) {
        
        System.out.println("Cliente de Votação iniciado.");
        
        listener = new MulticastListener();
        new Thread(listener).start(); 
        
        try (Scanner scanner = new Scanner(System.in)) {
            
            Usuario user = null;
            
            while (user == null) {
                System.out.println("\n--- TENTATIVA DE LOGIN ---");
                System.out.print("Email: ");
                String email = scanner.nextLine();
                System.out.print("Senha: ");
                String senha = scanner.nextLine();
                user = performLogin(email, senha); 
                if (user == null) {
                    System.out.println("Credenciais inválidas. Tente novamente.");
                }
            }
            
            System.out.println("\n✅ Login SUCESSO. Bem-vindo(a), " + user.nome + ".");
            
            if (user instanceof Administrador) {
                runAdminMenu((Administrador) user, scanner);
            } else {
                runEleitorMenu((Eleitor) user, scanner);
            }

        } catch (Exception e) {
            System.err.println("Erro irrecuperável: " + e.getMessage());
        } finally {
            if (listener != null) {
                listener.stop();
                System.out.println("[LISTENER] Encerrado.");
            }
            System.exit(0); 
        }
    }
    
    // --- MÉTODOS DE SERVIÇO ---

    private static Usuario performLogin(String email, String senha) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORTA_TCP);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) { 
            
            out.flush();
            out.writeInt(ServidorVotacao.CMD_LOGIN); 
            out.writeObject(email);
            out.writeObject(senha);
            out.flush();

            int status = in.readInt();
            if (status == 200) {
                return (Usuario) in.readObject();
            } else {
                String errorMessage = (String) in.readObject();
                return null;
            }
        } catch (ConnectException e) {
            System.err.println("ERRO: O servidor não está ativo.");
            return null;
        } catch (IOException | ClassNotFoundException e) {
            String msg = e.getMessage();
            if (msg != null && !msg.contains("Connection reset") && !msg.equals("null")) {
                System.err.println("Erro na comunicação durante login: " + msg);
            }
            return null;
        }
    }
    
    // --- MENUS ---

    private static void runEleitorMenu(Eleitor eleitor, Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- MENU ELEITOR (" + eleitor.nome + ") ---");
            System.out.println("1. Ver Candidatos e Prazo");
            System.out.println("2. Votar"); 
            System.out.println("3. Ver Resultados"); 
            System.out.println("0. Sair");
            System.out.print("Escolha: ");
            
            try {
                if (scanner.hasNextLine()) {
                    String choice = scanner.nextLine();
                    switch (choice) {
                        case "1": requestCandidatos(); break;
                        case "2": performVoto(eleitor, scanner); break;
                        case "3": requestResultados(); break; 
                        case "0": running = false; break;
                        default: System.out.println("Opção inválida.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro na operação de menu: " + e.getMessage());
            }
        }
    }
    
    private static void runAdminMenu(Administrador admin, Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- MENU ADMINISTRADOR (" + admin.nome + ") ---");
            System.out.println("1. Ver Candidatos e Prazo");
            System.out.println("2. Adicionar Candidato");
            System.out.println("3. Remover Candidato");
            System.out.println("4. Enviar Nota Informativa (Multicast)");
            System.out.println("5. Ver Resultados"); 
            System.out.println("0. Sair");
            System.out.print("Escolha: ");
            
            try {
                if (scanner.hasNextLine()) {
                    String choice = scanner.nextLine();
                    switch (choice) {
                        case "1": requestCandidatos(); break;
                        case "2": performAdminAdd(scanner); break;
                        case "3": performAdminRemove(scanner); break;
                        case "4": performAdminMulticast(admin, scanner); break;
                        case "5": requestResultados(); break; 
                        case "0": running = false; break;
                        default: System.out.println("Opção inválida.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro na operação de menu: " + e.getMessage());
            }
        }
    }
    
    // --- REQUISIÇÕES TCP ---

    private static void requestCandidatos() {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORTA_TCP);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            out.flush();
            out.writeInt(ServidorVotacao.CMD_LISTA_CANDIDATOS);
            out.flush();
            
            int status = in.readInt();
            
            if (status == 200) {
                List<Candidato> candidatos = (List<Candidato>) in.readObject();
                long prazoFinal = in.readLong();
                
                System.out.println("\n-------------------------------------");
                System.out.println("CANDIDATOS ATUAIS:");
                for (Candidato c : candidatos) { System.out.println("  " + c); }
                System.out.println("-------------------------------------");
                
                Date dataPrazo = new Date(prazoFinal);
                System.out.println("PRAZO FINAL DE VOTAÇÃO: " + dataPrazo);
                
                if (System.currentTimeMillis() > prazoFinal) { System.out.println("ATENÇÃO: A votação já foi encerrada."); }
                System.out.println("-------------------------------------");
            } else {
                String errorMessage = (String) in.readObject();
                System.out.println("Erro ao obter lista: " + errorMessage);
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && !msg.contains("Connection reset") && !msg.equals("null")) {
                System.err.println("Erro inesperado ao comunicar com o servidor: " + msg);
            }
        }
    }

    private static void requestResultados() {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORTA_TCP);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            out.flush();
            out.writeInt(ServidorVotacao.CMD_RESULTADOS);
            out.flush();
            
            int status = in.readInt();
            
            if (status == 200) {
                List<Candidato> resultados = (List<Candidato>) in.readObject();
                int totalVotos = in.readInt();
                
                System.out.println("\n--- RESULTADOS DA VOTAÇÃO ---");
                System.out.println("Total de Votos Registrados: " + totalVotos);
                System.out.println("-----------------------------");
                
                if (resultados.isEmpty()) {
                    System.out.println("Nenhum candidato registrado.");
                    return;
                }

                for (int i = 0; i < resultados.size(); i++) {
                    Candidato c = resultados.get(i);
                    String percentual = (totalVotos > 0) ? String.format("%.2f%%", (double) c.getVotos() * 100 / totalVotos) : "0.00%";
                    System.out.printf(" %d. %s (ID: %d) - %d Votos (%s)\n", (i + 1), c.nome, c.id, c.getVotos(), percentual);
                }
                System.out.println("-----------------------------\n");
            } else {
                String errorMessage = (String) in.readObject();
                System.out.println("Erro ao obter resultados: " + errorMessage);
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && !msg.contains("Connection reset") && !msg.equals("null")) {
                System.err.println("Erro inesperado ao comunicar com o servidor: " + msg);
            }
        }
    }

    private static void performVoto(Eleitor eleitor, Scanner scanner) {
        requestCandidatos();
        
        System.out.print("Digite o ID do candidato em que deseja votar: ");
        
        if (scanner.hasNextLine()) {
            try {
                int idCandidato = Integer.parseInt(scanner.nextLine());
                
                try (Socket socket = new Socket(SERVER_ADDRESS, PORTA_TCP);
                     ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                     ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) { 
                    
                    out.flush();
                    out.writeInt(ServidorVotacao.CMD_VOTO);
                    out.writeObject(eleitor.email); 
                    out.writeInt(idCandidato);
                    out.flush();
                    
                    int status = in.readInt();
                    String mensagem = (String) in.readObject();
                    
                    if (status == 200) { System.out.println("\n✅ Voto concluído: " + mensagem); } 
                    else { System.out.println("\n❌ ERRO (" + status + "): " + mensagem); }
                    
                }
            } catch (NumberFormatException e) {
                System.out.println("ID de candidato inválido. Digite apenas números.");
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null && !msg.contains("Connection reset") && !msg.equals("null")) {
                    System.err.println("Erro inesperado na comunicação para votar: " + msg);
                }
            }
        }
    }
    
    private static void performAdminAdd(Scanner scanner) {
        int id = -1;
        try {
            System.out.print("ID do novo candidato (número inteiro): ");
            id = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("❌ Erro: ID inválido. Tente novamente.");
            return;
        }
        
        System.out.print("Nome do novo candidato: ");
        String nome = scanner.nextLine();
        Candidato novoCandidato = new Candidato(id, nome);
        
        try (Socket socket = new Socket(SERVER_ADDRESS, PORTA_TCP);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) { 
            
            out.flush();
            out.writeInt(ServidorVotacao.CMD_ADMIN_OP);
            out.writeInt(ServidorVotacao.OP_ADMIN_ADICIONAR); 
            out.writeObject(novoCandidato);
            out.flush();
            
            int status = in.readInt();
            String mensagem = (String) in.readObject();
            System.out.println(status == 200 ? "✅ " + mensagem : "❌ ERRO: " + mensagem);
            
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && !msg.contains("Connection reset") && !msg.equals("null")) {
                System.err.println("Erro inesperado na comunicação para adicionar: " + msg);
            }
        }
    }
    
    private static void performAdminRemove(Scanner scanner) {
        requestCandidatos();
        
        int id = -1;
        try {
            System.out.print("ID do candidato a remover: ");
            id = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("❌ Erro: ID inválido. Tente novamente.");
            return;
        }
        
        try (Socket socket = new Socket(SERVER_ADDRESS, PORTA_TCP);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) { 
            
            out.flush();
            out.writeInt(ServidorVotacao.CMD_ADMIN_OP);
            out.writeInt(ServidorVotacao.OP_ADMIN_REMOVER); 
            out.writeInt(id);
            out.flush();

            int status = in.readInt();
            String mensagem = (String) in.readObject();
            System.out.println(status == 200 ? "✅ " + mensagem : "❌ ERRO: " + mensagem);
            
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && !msg.contains("Connection reset") && !msg.equals("null")) {
                System.err.println("Erro inesperado na comunicação para remover: " + msg);
            }
        }
    }
    
    private static void performAdminMulticast(Administrador admin, Scanner scanner) {
        System.out.print("Digite a NOTA INFORMATIVA a ser enviada: ");
        String nota = scanner.nextLine();
        
        try (Socket socket = new Socket(SERVER_ADDRESS, PORTA_TCP);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) { 
            
            out.flush();
            out.writeInt(ServidorVotacao.CMD_ADMIN_OP);
            out.writeInt(ServidorVotacao.OP_ADMIN_NOTA_MULTICAST); 
            out.writeObject(nota);
            out.flush();

            int status = in.readInt();
            String mensagem = (String) in.readObject();
            System.out.println(status == 200 ? "✅ " + mensagem : "❌ ERRO: " + mensagem);
            
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && !msg.contains("Connection reset") && !msg.equals("null")) {
                System.err.println("Erro inesperado na comunicação para multicast: " + msg);
            }
        }
    }

    // --- CLASSE ANINHADA: MULTICAST LISTENER (Final, Estruturalmente Correta) ---
    private static class MulticastListener implements Runnable { 
        
        private volatile boolean shouldStop = false; 

        public void stop() {
            this.shouldStop = true;
        }
        
        @Override
        public void run() {
            System.out.println("[LISTENER] Escutando notas informativas UDP...");
            try (MulticastSocket socket = new MulticastSocket(ServidorVotacao.MULTICAST_PORT)) {
                InetAddress group = InetAddress.getByName(ServidorVotacao.MULTICAST_ADDRESS);
                socket.joinGroup(group);
                socket.setSoTimeout(1000); 

                while (!shouldStop) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    
                    try {
                        socket.receive(packet);
                        String received = new String(packet.getData(), 0, packet.getLength());
                        System.out.println("\n--- NOTA INFORMATIVA RECEBIDA (UDP) ---");
                        System.out.println(received);
                        System.out.println("----------------------------------------");
                        System.out.print("Escolha: "); 
                    } catch (SocketTimeoutException e) {
                        // Continua o loop para verificar shouldStop
                    }
                }
            } catch (IOException e) {
                if (!shouldStop) {
                    System.err.println("Erro no Multicast Listener: " + e.getMessage());
                }
            }
        }
    }
}