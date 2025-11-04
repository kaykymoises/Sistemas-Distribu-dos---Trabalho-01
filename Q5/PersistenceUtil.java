// Arquivo: PersistenceUtil.java

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;

public class PersistenceUtil {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CANDIDATOS_FILE = "candidatos.json";
    private static final String VOTANTES_FILE = "votantes.json";

    // --- Salvamento ---

    public static void saveCandidatos(Map<Integer, Candidato> candidatos) {
        try (Writer writer = new FileWriter(CANDIDATOS_FILE)) {
            GSON.toJson(candidatos, writer);
        } catch (IOException e) {
            System.err.println("Erro ao salvar candidatos em JSON: " + e.getMessage());
        }
    }

    public static void saveVotantes(Set<String> votantes) {
        // Converte o Set sincronizado para um HashSet normal para serialização
        Set<String> serializableSet = new HashSet<>(votantes); 
        try (Writer writer = new FileWriter(VOTANTES_FILE)) {
            GSON.toJson(serializableSet, writer);
        } catch (IOException e) {
            System.err.println("Erro ao salvar votantes em JSON: " + e.getMessage());
        }
    }

    // --- Carregamento ---

    public static Map<Integer, Candidato> loadCandidatos() {
        try (Reader reader = new FileReader(CANDIDATOS_FILE)) {
            Type type = new TypeToken<Map<Integer, Candidato>>(){}.getType();
            Map<Integer, Candidato> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                System.out.println("[JSON] Candidatos carregados do arquivo.");
                return loaded;
            }
        } catch (FileNotFoundException e) {
            System.out.println("[JSON] Arquivo de candidatos não encontrado. Iniciando vazio.");
        } catch (IOException e) {
            System.err.println("Erro ao carregar candidatos em JSON: " + e.getMessage());
        }
        return new HashMap<>();
    }

    public static Set<String> loadVotantes() {
        try (Reader reader = new FileReader(VOTANTES_FILE)) {
            Type type = new TypeToken<Set<String>>(){}.getType();
            Set<String> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                System.out.println("[JSON] Votantes carregados do arquivo.");
                // Retorna como Set sincronizado, pronto para o Servidor
                return Collections.synchronizedSet(loaded);
            }
        } catch (FileNotFoundException e) {
            System.out.println("[JSON] Arquivo de votantes não encontrado. Iniciando vazio.");
        } catch (IOException e) {
            System.err.println("Erro ao carregar votantes em JSON: " + e.getMessage());
        }
        return Collections.synchronizedSet(new HashSet<>());
    }
}