package shq.ocn;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class LineReaderSpliterator implements Spliterator<String> {

    private BufferedReader reader;
    private IOException exception = null;

    public LineReaderSpliterator(BufferedReader reader) {
        this.reader = reader;
    }

    public static Stream<String> streamOfFileLines(String fileName) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        return StreamSupport.stream(new LineReaderSpliterator(reader), false);
    }

    public IOException exception() {
        return exception;
    }

    @Override public boolean tryAdvance(Consumer<? super String> action) {
        try {
            if (reader == null)
                return false;

            String line = reader.readLine();
            if (line != null) {
                action.accept(line);
                return true;
            }
            else {
                closeReader();
                return false;
            }
        }
        catch (IOException e) {
            exception = e;
            try {
                closeReader();
            } catch (IOException e1) {
                // skip
            }
            return false;
        }
    }

    private void closeReader() throws IOException {
        try {
            reader.close();
        } finally {
            reader = null;
        }
    }

    @Override public Spliterator<String> trySplit() {
        return null;
    }

    @Override public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override public int characteristics() {
        return DISTINCT | NONNULL | IMMUTABLE;
    }
}
