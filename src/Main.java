import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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

        // --- Handshake inicial ---
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
                continue;
            }

            if (client.enviarMensagem(mensagem)) {
                String resposta = client.receberResposta();
                if (resposta == null) {
                    break;
                }

                String opcao = entradaUsuario.split(";", 2)[0].trim();

                if (opcao.equals("4")) {
                    abrirImagemRecebida(resposta);
                } else if (resposta.startsWith("SERIALIZED;")) {
                    desserializarAluno(resposta);
                } else {
                    System.out.println("[Servidor] " + resposta);
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
        System.out.println("5 - Solicitar objeto Aluno (serializado)");
        System.out.println("0 - Encerrar Conexão");
        System.out.println("Formato: opcao;valor1;valor2  (ex: 1;2;3)");
        System.out.println("Para imagem ou aluno: digite apenas a opção");
    }

    private static String prepararMensagem(String entrada) {
        String[] partes = entrada.split(";");
        String opcao = partes[0].trim();

        // Comandos sem argumentos: envia apenas o número
        if (opcao.equals("4") || opcao.equals("5")) {
            return opcao;
        }

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

    /**
     * Recebe a linha "SERIALIZED;<base64>", decodifica o Base64, reconstrói
     * o objeto via ObjectInputStream e exibe no terminal.
     */
    private static void desserializarAluno(String resposta) {
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