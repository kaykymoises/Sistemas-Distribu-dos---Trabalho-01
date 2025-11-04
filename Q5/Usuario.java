// Arquivo: Usuario.java (Classe base abstrata)

import java.io.Serializable;

public abstract class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String email;
    public String nome;
    private String senha;

    public Usuario(int id, String email, String nome, String senha) {
        this.id = id;
        this.email = email;
        this.nome = nome;
        this.senha = senha;
    }

    public String getSenha() {
        return senha;
    }
    
    public String getEmail() {
        return email;
    }
}