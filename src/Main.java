import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ClientJava client = new ClientJava("127.0.0.1", 12345);
        client.conectar();

        if (!client.estaConectado()) {
            return;
        }

        // --- Handshake inicial (cada mensagem é uma linha única) ---
        String idLinha = client.receberLinha();
        if (idLinha != null && idLinha.startsWith("__ID__")) {
            String meuId = idLinha.replace("__ID__", "").trim();
            System.out.println("Você é o Cliente #" + meuId);
        }

        String boasVindas = client.receberLinha();
        if (boasVindas != null) {
            System.out.println("[Servidor] " + boasVindas);
        }

        exibirMenu();

        // --- Loop principal ---
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\n> ");
            String entradaUsuario = scanner.nextLine().trim();

            if (entradaUsuario.equals("0")) {
                client.enviarMensagem("0");
                break;
            }

            if (entradaUsuario.isEmpty()) {
                continue;
            }

            String mensagem = prepararMensagem(entradaUsuario);
            if (mensagem == null) {
                // Erro de validação (arquivo não encontrado, formato
                // inválido etc.) — volta a pedir uma entrada, sem
                // gastar uma ida e volta com o servidor.
                continue;
            }

            if (client.enviarMensagem(mensagem)) {
                String resposta = client.receberResposta();
                if (resposta != null) {
                    String opcao = entradaUsuario.split(";", 2)[0].trim();
                    if (opcao.equals("4")) {
                        abrirImagemRecebida(resposta);
                    } else {
                        System.out.println("[Servidor] " + resposta);
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        client.fechar();
        scanner.close();
    }

    private static void exibirMenu() {
        System.out.println("\n===== MENU =====");
        System.out.println("1 - Somar");
        System.out.println("2 - Subtrair");
        System.out.println("3 - Multiplicar");
        System.out.println("4 - Solicitar imagem ao servidor");
        System.out.println("0 - Encerrar Conexão");
        System.out.println("Formato: opcao;valor1;valor2  (ex: 1;2;3)");
        System.out.println("Para imagem: digite 4");
    }

    /**
     * Recebe a linha digitada pelo usuário (ex: "1;2;3" ou "4")
     * e devolve a mensagem pronta para envio ao servidor.
     *
     * Para as opções 1, 2 e 3, a linha já está no formato que o
     * servidor espera, então é enviada como veio (a lógica de somar/
     * subtrair/multiplicar fica por conta do servidor).
     *
     * Para a opção 4, envia somente o comando. A imagem fica armazenada
     * no servidor e será recebida na resposta.
     */
    private static String prepararMensagem(String entrada) {
        String[] partes = entrada.split(";");
        String opcao = partes[0].trim();

        if (opcao.equals("4")) {
            return "4";
        }

        // Opções 1, 2, 3 (e qualquer outra que o servidor saiba tratar)
        return entrada;
    }

    /** Decodifica a resposta do servidor e abre o arquivo temporário no Firefox. */
    private static void abrirImagemRecebida(String resposta) {
        if (!resposta.startsWith("OK;")) {
            System.out.println("[Servidor] " + resposta);
            return;
        }

        String dadosBase64 = resposta.substring("OK;".length());
        try {
            byte[] imagem = Base64.getDecoder().decode(dadosBase64);
            Path arquivoTemporario = Files.createTempFile("imagem-servidor-", ".jpg");
            Files.write(arquivoTemporario, imagem);

            new ProcessBuilder("firefox", arquivoTemporario.toAbsolutePath().toString()).start();
            System.out.println("Imagem aberta no Firefox.");
        } catch (IllegalArgumentException e) {
            System.out.println("O servidor enviou dados Base64 inválidos.");
        } catch (IOException e) {
            System.out.println("Não foi possível salvar a imagem ou iniciar o Firefox: "
                    + e.getMessage());
        }
    }
}
