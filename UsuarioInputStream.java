import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

class UsuarioInputStream extends InputStream {
    private final DataInputStream dis;
    private final int numObjetos;

    public UsuarioInputStream(InputStream origem) throws IOException {
        this.dis = new DataInputStream(origem);
        
        // Usa uma variável local temporária para evitar o erro do campo 'final'
        int tempNumObjetos; 
        
        try {
            tempNumObjetos = dis.readInt(); // Tenta ler o número de objetos
        } catch (EOFException e) {
            tempNumObjetos = 0; // Se o stream estiver vazio, atribui zero
        }
        
        // Atribui o valor final à variável de instância UMA ÚNICA VEZ
        this.numObjetos = tempNumObjetos; 
    }

    @Override
    public int read() throws IOException {
        return dis.read();
    }
    
    public Usuario[] readUsuarios() throws IOException {
        Usuario[] listaUsuarios = new Usuario[this.numObjetos];
        
        for (int i = 0; i < this.numObjetos; i++) {
            
            // 1. Lê o nome da classe (para decidir qual objeto criar)
            String tipoClasse = dis.readUTF(); 
            
            // 2. Leitura dos atributos
            long id = dis.readLong();
            String email = dis.readUTF();
            String nome = dis.readUTF();
            
            int bytesTotalUsados = dis.readInt(); // Lê o número de bytes usados
            
            Usuario user;
            
            // 3. Lógica de Decisão para Instanciar a Subclasse Correta
            if (tipoClasse.equals("Administrador")) {
                user = new Administrador(id, email, nome);
            } else if (tipoClasse.equals("Visitante")) {
                user = new Visitante(id, email, nome);
            } else {
                // Caso padrão/fallback
                user = new Usuario(id, email, nome); 
            }
            
            listaUsuarios[i] = user;
            
            System.out.println("Lido - TIPO: " + tipoClasse + ", ID: " + id + ", Nome: " + nome + ", Bytes Reportados: " + bytesTotalUsados);
        }
        return listaUsuarios;
    }
}