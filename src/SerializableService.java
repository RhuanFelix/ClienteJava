import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;

public class SerializableService {
    public void desserializarAluno(String resposta) {
        String base64 = resposta.substring("SERIALIZED;".length());

        try {
            byte[] bytes = Base64.getDecoder().decode(base64);

            try (ObjectInputStream ois =
                         new ObjectInputStream(new ByteArrayInputStream(bytes))) {

                Object obj = ois.readObject();

                if (obj instanceof Aluno aluno) {
                    System.out.println("[Servidor] Objeto Aluno recebido:");
                    System.out.println(aluno);
                } else {
                    System.out.println("[Servidor] Objeto recebido não é um Aluno: "
                            + obj.getClass().getName());
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("O servidor enviou dados Base64 inválidos.");
        } catch (IOException e) {
            System.out.println("Erro de I/O ao desserializar: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            // Acontece se a classe Aluno não existir no classpath do cliente
            System.out.println("Classe Aluno não encontrada no cliente: " + e.getMessage());
        }
    }
}
