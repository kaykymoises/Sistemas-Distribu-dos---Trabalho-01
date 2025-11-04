// Arquivo: ServidorVotacao.java (Versão FINAL: Estável, Sessões Curtas e Persistência JSON)

import java.net.*;
import java.io.*;
import java.util.*;

public class ServidorVotacao {

    // --- CONFIGURAÇÃO E DADOS ---
    private static final int PORTA_TCP = 12349;
    private static final long PRAZO_FINAL = System.currentTimeMillis() + (5 * 60 * 1000); 
    
    // Configurações do Multicast (UDP)
    public static final String MULTICAST_ADDRESS = "230.0.0.1";
    public static final int MULTICAST_PORT = 4446;
    
    // Armazenamento de Dados (Inicializado no main)
    private static Map<Integer, Candidato> CANDIDATOS;
    private static final Map<String, Usuario> USUARIOS = new HashMap<>();
    private static Set<String> ELEITORES_QUE_VOTARAM; 

    // Protocolo: Comandos Principais (TCP)
    public static final int CMD_LOGIN = 1;
    public static final int CMD_LISTA_CANDIDATOS = 2;
    public static final int CMD_VOTO = 3;
    public static final int CMD_ADMIN_OP = 4;
    public static final int CMD_RESULTADOS = 5; 
    
    // Protocolo: Sub-Comandos de Administração 
    public static final int OP_ADMIN_ADICIONAR = 1;
    public static final int OP_ADMIN_REMOVER = 2;
    public static final int OP_ADMIN_NOTA_MULTICAST = 3;
    
    // Inicialização do Servidor (Carrega JSON e define usuários)
    private static void initialize() {
        // Carrega dados persistentes (JSON)
        CANDIDATOS = PersistenceUtil.loadCandidatos();
        ELEITORES_QUE_VOTARAM = PersistenceUtil.loadVotantes();

        // Se o arquivo JSON estava vazio, define candidatos iniciais
        if (CANDIDATOS.isEmpty()) {
            CANDIDATOS.put(1, new Candidato(1, "Ana"));
            CANDIDATOS.put(2, new Candidato(2, "Beto"));
            System.out.println("[INIT] Candidatos padrões definidos.");
        }
        
        // Usuários (Geralmente não persistidos em JSON em sistemas simples)
        USUARIOS.put("admin@vote.com", new Administrador(900, "admin@vote.com", "Admin Geral", "123"));
        USUARIOS.put("eleitor1@vote.com", new Eleitor(101, "eleitor1@vote.com", "Eleitor Um", "456"));
    }


    public static void main(String[] args) {
        initialize(); // Chama a nova função de inicialização
        
        System.out.println("Servidor de Votação iniciado. Prazo final: " + new Date(PRAZO_FINAL));
        try (ServerSocket serverSocket = new ServerSocket(PORTA_TCP)) {
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new VotacaoHandler(clientSocket)).start(); 
            }
        } catch (IOException e) {
            System.err.println("Erro fatal no Servidor: " + e.getMessage());
        }
    }
    
    // --- CLASSE HANDLER (Trata UM comando e fecha) ---
    private static class VotacaoHandler implements Runnable {
        private final Socket socket;
        
        public VotacaoHandler(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            try (OutputStream os = socket.getOutputStream();
                 InputStream is = socket.getInputStream();
                 ObjectInputStream in = new ObjectInputStream(is); 
                 ObjectOutputStream out = new ObjectOutputStream(os)) { 
                
                out.flush(); 

                int command;
                try { command = in.readInt(); } catch (EOFException e) { return; } 
                
                switch (command) {
                    case CMD_LOGIN: handleLogin(in, out); break;
                    case CMD_LISTA_CANDIDATOS: handleListaCandidatos(out); break;
                    case CMD_VOTO: handleVoto(in, out); break; 
                    case CMD_ADMIN_OP: handleAdminOp(in, out); break;
                    case CMD_RESULTADOS: handleResultados(out); break; 
                    default: out.writeObject("Comando inválido."); out.writeInt(400); out.flush();
                }

            } catch (IOException | ClassNotFoundException e) {
            } finally {
                try { socket.close(); } catch (IOException e) { /* ignore */ }
            }
        }
        
        // --- MÉTODOS DE SERVIÇO (Modificados para Persistência) ---

        private void handleLogin(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
            String email = (String) in.readObject();
            String senha = (String) in.readObject();
            Usuario user = USUARIOS.get(email);

            if (user != null && user.getSenha() != null && user.getSenha().equals(senha)) {
                out.writeInt(200); out.writeObject(user); out.flush();
                System.out.println("[LOGIN SUCESSO]: " + user.nome);
            } else { out.writeInt(401); out.writeObject("Falha na autenticação."); out.flush(); }
        }
        
        private void handleListaCandidatos(ObjectOutputStream out) throws IOException {
             List<Candidato> lista = new ArrayList<>(CANDIDATOS.values());
             out.writeInt(200); 
             out.writeObject(lista); 
             out.writeLong(PRAZO_FINAL); 
             out.flush();
        }

        private void handleResultados(ObjectOutputStream out) throws IOException {
            List<Candidato> resultados = new ArrayList<>(CANDIDATOS.values());
            resultados.sort(Comparator.comparingInt(Candidato::getVotos).reversed());
            int totalVotos = resultados.stream().mapToInt(Candidato::getVotos).sum();
            
            out.writeInt(200);
            out.writeObject(resultados);
            out.writeInt(totalVotos);
            out.flush();
        }

        private void handleVoto(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
            String eleitorEmail = (String) in.readObject(); 
            int idCandidato = in.readInt();

            if (System.currentTimeMillis() > PRAZO_FINAL) { out.writeInt(403); out.writeObject("A votação foi encerrada."); out.flush(); return; }
            
            synchronized (ELEITORES_QUE_VOTARAM) {
                if (ELEITORES_QUE_VOTARAM.contains(eleitorEmail)) { out.writeInt(409); out.writeObject("Você já votou e não pode votar novamente."); out.flush(); return; }
            }
            
            Candidato candidato = CANDIDATOS.get(idCandidato);
            if (candidato == null) { out.writeInt(404); out.writeObject("Candidato com ID " + idCandidato + " não existe."); out.flush(); return; }

            synchronized (CANDIDATOS) { 
                candidato.addVoto(); 
                // --- NOVO: Salva Candidatos após o voto ---
                PersistenceUtil.saveCandidatos(CANDIDATOS);
            }
            
            ELEITORES_QUE_VOTARAM.add(eleitorEmail);
            // --- NOVO: Salva Votantes após o voto ---
            PersistenceUtil.saveVotantes(ELEITORES_QUE_VOTARAM); 
            
            System.out.println("[VOTO REGISTRADO] Eleitor " + eleitorEmail + " votou em " + candidato.nome);

            out.writeInt(200); out.writeObject("Voto em " + candidato.nome + " registrado com sucesso!"); out.flush();
        }

        private void handleAdminOp(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
            
            int opType = in.readInt(); 

            switch (opType) {
                case ServidorVotacao.OP_ADMIN_ADICIONAR: 
                    Candidato novoCandidato = (Candidato) in.readObject();
                    if (CANDIDATOS.containsKey(novoCandidato.id)) { out.writeObject("Candidato com ID " + novoCandidato.id + " já existe."); out.writeInt(409); } 
                    else {
                        CANDIDATOS.put(novoCandidato.id, novoCandidato);
                        // --- NOVO: Salva Candidatos após adicionar ---
                        PersistenceUtil.saveCandidatos(CANDIDATOS); 
                        
                        System.out.println("[ADMIN] Adicionou: " + novoCandidato);
                        out.writeObject("Candidato " + novoCandidato.nome + " adicionado com sucesso."); out.writeInt(200);
                    }
                    out.flush();
                    break;

                case ServidorVotacao.OP_ADMIN_REMOVER:
                    int idRemover = in.readInt();
                    if (CANDIDATOS.remove(idRemover) != null) {
                        // --- NOVO: Salva Candidatos após remover ---
                        PersistenceUtil.saveCandidatos(CANDIDATOS); 
                        
                        System.out.println("[ADMIN] Removeu o candidato ID: " + idRemover);
                        out.writeObject("Candidato ID " + idRemover + " removido com sucesso."); out.writeInt(200);
                    } else { out.writeObject("Candidato ID " + idRemover + " não encontrado."); out.writeInt(404); }
                    out.flush();
                    break;
                    
                case ServidorVotacao.OP_ADMIN_NOTA_MULTICAST:
                    String nota = (String) in.readObject();
                    enviarMulticast(nota); 
                    out.writeObject("Nota informativa enviada via Multicast."); out.writeInt(200);
                    out.flush();
                    break;
                    
                default:
                    out.writeObject("Operação Admin desconhecida."); out.writeInt(400); out.flush();
            }
        }
        
        // --- MÉTODO UDP MULTICAST ---
        private void enviarMulticast(String nota) {
            String mensagem = String.format("[NOTA ADMINISTRADOR] %s", nota);
            try (MulticastSocket socket = new MulticastSocket()) {
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                byte[] buffer = mensagem.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, MULTICAST_PORT);
                socket.send(packet);
                System.out.println("[MULTICAST ENVIADO] Admin enviou nota.");
            } catch (IOException e) {
                System.err.println("Erro ao enviar Multicast: " + e.getMessage());
            }
        }
    }
}