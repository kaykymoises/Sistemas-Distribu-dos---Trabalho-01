import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class UsuarioOutputStream extends OutputStream {
    private final Usuario[] usuarios;
    private final int numObjetos;
    private final OutputStream destino;

    public UsuarioOutputStream(Usuario[] arrayUsuarios, int num, OutputStream destino) throws IOException {
        if (num > arrayUsuarios.length) {
            throw new IllegalArgumentException("O número de objetos a enviar é maior que o array.");
        }
        this.usuarios = arrayUsuarios;
        this.numObjetos = num;
        this.destino = destino;
        
        // Envia o número total de objetos (necessário para a leitura)
        new DataOutputStream(destino).writeInt(numObjetos); 
    }

    @Override
    public void write(int b) throws IOException {
        // Implementação base não utilizada
    }
    
    // Método principal para enviar os objetos
    public void writeUsuarios() throws IOException {
        DataOutputStream dos = new DataOutputStream(destino);
        
        for (int i = 0; i < numObjetos; i++) {
            Usuario user = usuarios[i];
            
            // 1. [NOVO] Grava o nome da classe (Ex: "Administrador" ou "Visitante")
            dos.writeUTF(user.getClass().getSimpleName());
            
            // 2. Grava 3 atributos
            dos.writeLong(user.id);
            dos.writeUTF(user.email);
            dos.writeUTF(user.nome); 
            
            // 3. Envia o número de bytes utilizados
            int bytesId = 8;
            int bytesEmail = user.email.getBytes("UTF-8").length + 2;
            int bytesNome = user.nome.getBytes("UTF-8").length + 2;
            int bytesTipo = user.getClass().getSimpleName().getBytes("UTF-8").length + 2;
            
            int bytesTotal = bytesId + bytesEmail + bytesNome + bytesTipo;
            
            dos.writeInt(bytesTotal); 
        }
        dos.flush();
    }
}