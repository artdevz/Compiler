package examples;

public class ValidTest {

    private int idade = 21;
    private float altura = 1.70f;
    private char letra = 'A';
    private char novaLinha = '\n';
    private static String nome = "Arthur\nDantas";
    private static boolean ativo = true;
    public static void main(String[] args) {
        int numero = 10;
        double valor = 3.14;

        // Comentário que deve ser ignorado

        if (numero >= 10 && ativo) {
            valor += 2.0f;
            numero -= 1;
        }
        else {
            valor *= 2.0f;
        }

        /*
        * Comentário de bloco.
        * Também deve ser ignorado
        */

        while (numero != 0 || valor > 1.0) {
            numero--;
        }

        System.out.println(nome);
    }
}
