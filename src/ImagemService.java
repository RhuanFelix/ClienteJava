import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ImagemService {
    public void abrirImagemRecebida(String resposta) {
        String navegador = "google-chrome"; // coloque o nome do executável instalado em sua máquina
        if (!resposta.startsWith("OK;")) {
            System.out.println("[Servidor] " + resposta);
            return;
        }

        String dadosBase64 = resposta.substring("OK;".length());
        try {
            byte[] imagem = Base64.getDecoder().decode(dadosBase64);
            Path arquivoTemporario = Files.createTempFile("imagem-servidor-", ".jpg");
            Files.write(arquivoTemporario, imagem);

            new ProcessBuilder(navegador, arquivoTemporario.toAbsolutePath().toString()).start();
            System.out.println("Imagem aberta no " + navegador);
        } catch (IllegalArgumentException e) {
            System.out.println("O servidor enviou dados Base64 inválidos.");
        } catch (IOException e) {
            System.out.println("Não foi possível salvar a imagem ou iniciar o " + navegador + ": "
                    + e.getMessage());
        }
    }
}
