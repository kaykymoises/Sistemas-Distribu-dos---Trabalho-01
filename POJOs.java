class Usuario {
    public long id;
    public String email;
    public String nome;
    private final String senha;

    public Usuario(long id, String email, String nome, String senha) {
        this.id = id;
        this.email = email;
        this.nome = nome;
        this.senha = senha;

    }
    public Usuario(long id, String email, String nome) {
        this.id = id;
        this.email = email;
        this.nome = nome;
        this.senha = null;

    }
    public String getSenha() { 
        return senha;
    }
}

class Visitante extends Usuario {
    public Visitante(long id, String email, String nome, String senha) {
        super(id, email, nome, senha); 
    }
    public Visitante(long id, String email, String nome) {
        super(id, email, nome);
    }
}

class Administrador extends Usuario {
    public Administrador(long id, String email, String nome, String senha) {
        super(id, email, nome, senha); 
    }
    public Administrador(long id, String email, String nome) {
        super(id, email, nome);
    }
    // Pode ter atributos/métodos específicos de Admin
}