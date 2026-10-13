public class MenuService {
    public  void exibirMenu() {
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

    public  String prepararMensagem(String entrada) {
        String[] partes = entrada.split(";");
        String opcao = partes[0].trim();

        // Comandos sem argumentos: envia apenas o número
        if (opcao.equals("4") || opcao.equals("5")) {
            return opcao;
        }

        return entrada;
    }
}
