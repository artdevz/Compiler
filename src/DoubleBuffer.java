import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class DoubleBuffer {

    private static final int BUFFER_SIZE = 32;

    private final Reader reader;

    private final char[] primaryBuffer;
    private final char[] secondaryBuffer;

    private int primarySize;
    private int secondarySize;

    private boolean primaryActive;
    private int position;

    private boolean endOfFile;

    public DoubleBuffer(Path file) throws IOException {

        reader = Files.newBufferedReader(file);

        primaryBuffer = new char[BUFFER_SIZE];
        secondaryBuffer = new char[BUFFER_SIZE];

        primarySize = 0;
        secondarySize = 0;

        primaryActive = true;
        position = 0;

        endOfFile = false;

        LoadPrimary();
    }

    private void LoadPrimary() throws IOException {

        primarySize = reader.read(primaryBuffer);

        if (primarySize == -1) {
            primarySize = 0;
        }
    }

    private void LoadSecondary() throws IOException {

        secondarySize = reader.read(secondaryBuffer);

        if (secondarySize == -1) {
            secondarySize = 0;
        }
    }

    public char Current() {

        if (position >= CurrentSize()) {
            return '\0';
        }

        if (primaryActive) {
            return primaryBuffer[position];
        }

        return secondaryBuffer[position];
    }

    public char Peek() throws IOException {

        // Ainda existe posição dentro do buffer atual
        if (position + 1 < CurrentSize()) {

            if (primaryActive) {
                return primaryBuffer[position + 1];
            }

            return secondaryBuffer[position + 1];
        }

        // Estamos no último caractere do buffer primário.
        // Precisamos olhar o primeiro caractere do secundário.
        if (primaryActive) {

            if (secondarySize == 0) {
                LoadSecondary();
            }

            if (secondarySize > 0) {
                return secondaryBuffer[0];
            }

            return '\0';
        }

        // Estamos no último caractere do secundário.
        // O próximo caractere ainda não está carregado.
        if (position + 1 >= secondarySize) {

            LoadPrimary();

            if (primarySize > 0) {
                return primaryBuffer[0];
            }
        }

        return '\0';
    }

    public void Advance() throws IOException {

        if (IsEOF()) {
            return;
        }

        position++;

        if (position < CurrentSize()) {
            return;
        }

        SwitchBuffer();
    }

    private void SwitchBuffer() throws IOException {

        if (primaryActive) {

            // Garante que o secundário esteja carregado.
            if (secondarySize == 0) {
                LoadSecondary();
            }

            if (secondarySize == 0) {
                endOfFile = true;
                position = 0;
                return;
            }

            primaryActive = false;
            position = 0;

            return;
        }

        // Secundário terminou.
        // Recarrega o primário com a próxima parte do arquivo.
        LoadPrimary();

        if (primarySize == 0) {
            endOfFile = true;
            position = 0;
            return;
        }

        primaryActive = true;
        position = 0;
    }

    private int CurrentSize() {

        if (primaryActive) {
            return primarySize;
        }

        return secondarySize;
    }

    public boolean IsEOF() {

        return endOfFile;
    }

    public void Close() throws IOException {

        reader.close();
    }
}