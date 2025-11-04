// Arquivo: Candidato.java

import java.io.Serializable;

public class Candidato implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public String nome;
    private int votos;

    public Candidato(int id, String nome) {
        this.id = id;
        this.nome = nome;
        this.votos = 0;
    }

    public void addVoto() {
        this.votos++;
    }

    public int getVotos() {
        return votos;
    }
    
    @Override
    public String toString() {
        return "ID: " + id + ", Nome: " + nome + ", Votos: " + votos;
    }
}