import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class DoubleBuffer implements AutoCloseable {

    private static final int BUFFER_SIZE = 32;

    private final Reader reader;

    private final char[] primaryBuffer;
    private final char[] secondaryBuffer;

    private int primarySize;
    private int secondarySize;

    private boolean usingPrimary;
    private int position;

    private boolean eof;

    public DoubleBuffer(Path file) throws IOException {
        reader = Files.newBufferedReader(file);

        primaryBuffer = new char[BUFFER_SIZE];
        secondaryBuffer = new char[BUFFER_SIZE];

        usingPrimary = true;
        position = 0;
        eof = false;

        primarySize = reader.read(primaryBuffer);

        if (primarySize < 0) {
            primarySize = 0;
            eof = true;
        }

        secondarySize = 0;
    }

    public char Current() {
        if (eof) return '\0';

        if (usingPrimary) return primaryBuffer[position];

        return secondaryBuffer[position];
    }

    public char Peek() throws IOException {
        if (eof) return '\0';

        /*
         * Dentro do buffer atual.
         */
        if (position + 1 < CurrentSize()) return Get(position + 1);

        /*
         * Estamos no último caractere do buffer 1.
         *
         * O buffer 2 será carregado antecipadamente.
         */
        if (usingPrimary) {
            if (secondarySize == 0) LoadSecondary();

            if (secondarySize > 0) return secondaryBuffer[0];

            return '\0';
        }

        /*
         * Estamos no final do buffer 2.
         *
         * Aqui ainda não carregamos o buffer 1,
         * porque ele será reutilizado somente depois
         * que o buffer 2 for consumido.
         */
        return '\0';
    }

    private char Get(int index) {
        if (usingPrimary) return primaryBuffer[index];

        return secondaryBuffer[index];
    }

    public void Advance() throws IOException {
        if (eof) return;

        position++;

        if (position < CurrentSize()) return;

        if (usingPrimary) {

            /*
             * Terminou buffer 1.
             */
            if (secondarySize == 0) {
                LoadSecondary();
            }

            if (secondarySize == 0) {
                eof = true;
                return;
            }

            usingPrimary = false;
            position = 0;

        } 
        else {
            /*
             * Terminou buffer 2.
             * Agora podemos reutilizar buffer 1.
             */
            LoadPrimary();

            if (primarySize == 0) {
                eof = true;
                return;
            }

            secondarySize = 0;

            usingPrimary = true;
            position = 0;
        }
    }

    private int CurrentSize() {
        return usingPrimary ? primarySize : secondarySize;
    }

    private void LoadPrimary() throws IOException {
        primarySize = reader.read(primaryBuffer);

        if (primarySize < 0) {
            primarySize = 0;
        }
    }

    private void LoadSecondary() throws IOException {
        secondarySize = reader.read(secondaryBuffer);

        if (secondarySize < 0) {
            secondarySize = 0;
        }
    }

    public boolean IsEOF() { return eof; }

    @Override
    public void close() throws IOException { reader.close(); }

}
