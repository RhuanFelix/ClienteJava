import java.util.Scanner;

public class ClienteService {
    private final MenuService menu;
    private final ImagemService imagem;
    private final SerializableService serializableService;

    public ClienteService() {
        menu = new MenuService();
        imagem = new ImagemService();
        serializableService = new SerializableService();
    }

    public void iniciar() {
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

        menu.exibirMenu();

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

            String mensagem = menu.prepararMensagem(entradaUsuario);
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
                    imagem.abrirImagemRecebida(resposta);
                } else if (resposta.startsWith("SERIALIZED;")) {
                    serializableService.desserializarAluno(resposta);
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
}
