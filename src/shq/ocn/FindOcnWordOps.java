package shq.ocn;

import shq.parsers.Util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FindOcnWordOps {

    public static void main(String[] args) {
        try {
            new FindOcnWordOps().run();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    NavigableMap<OrtodoxCyrillicNumber, Set<String>> wordOcns = new TreeMap<>();

    private static class LineReaderSpliterator implements Spliterator<String> {

        private final BufferedReader reader;
        private boolean eof = false;
        private IOException exception = null;

        public LineReaderSpliterator(BufferedReader reader) {
            this.reader = reader;
        }

        public IOException exception() {
            return exception;
        }

        @Override public boolean tryAdvance(Consumer<? super String> action) {
            try {
                if (eof)
                    return false;

                String line = reader.readLine();
                if (line != null) {
                    action.accept(line);
                    return true;
                }
                else {
                    eof = true;
                    return false;
                }
            }
            catch (IOException e) {
                exception = e;
                return false;
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

    private static Stream<String> createFileLineStream(String fileName) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        return StreamSupport.stream(new LineReaderSpliterator(reader), false);
    }

    private void run() throws FileNotFoundException {
        System.out.println("Reading dictionary...");

        createFileLineStream("src/shq/ocn/lop1v2.txt")
            .flatMap(line -> Stream.of(line.trim().toUpperCase().split("[-. :(),!\\?\"'`]")))
            .forEach(line -> {
                line = line.trim();

                if (line.length() < 3)
                    return;

                try {
                    OrtodoxCyrillicNumber ocn = new OrtodoxCyrillicNumber(line);

                    wordOcns.merge(ocn, Util.modifiableSingletonSet(line, HashSet::new), (o, n) -> {
                        o.addAll(n);
                        return o;
                    });
                }
                catch (Exception e) {
                    System.out.println("Can't parse OCN " + line + ": " + e.getMessage());
                }
            });

        System.out.println("Read words: " + wordOcns.size());

        for (Map.Entry<OrtodoxCyrillicNumber, Set<String>> e : wordOcns.entrySet()) {
            if (e.getValue().size() > 1)
                System.out.println("Same OCN: " + e.getValue());
        }

        System.out.println("Looking for sums...");

        int n = 0;

        Iterator<Map.Entry<OrtodoxCyrillicNumber, Set<String>>> i1 = wordOcns.entrySet().iterator();
        while (i1.hasNext()) {
            Map.Entry<OrtodoxCyrillicNumber, Set<String>> e1 = i1.next();

            System.out.println(e1.getValue());

            Iterator<Map.Entry<OrtodoxCyrillicNumber, Set<String>>> i2 = wordOcns.tailMap(e1.getKey(), true).entrySet().iterator();

            if (!i2.hasNext())
                continue;

            Map.Entry<OrtodoxCyrillicNumber, Set<String>> e2 = i2.next();

            OrtodoxCyrillicNumber sum = e1.getKey().add(e2.getKey());

            Iterator<Map.Entry<OrtodoxCyrillicNumber, Set<String>>> iSum = wordOcns.tailMap(sum, true).entrySet().iterator();

            if (!iSum.hasNext())
                continue;

            Map.Entry<OrtodoxCyrillicNumber, Set<String>> eSum = iSum.next();

            for (;;) {
//                if ((n & 0xffff) == 0)
//                    System.out.println("? " + e1.getValue() + " + " + e2.getValue() + " = " + sum);
//                n++;

                int diff = sum.compareTo(eSum.getKey());

                if (diff == 0) {
                    System.out.println("==== Found sum: " + e1.getValue() + " + " + e2.getValue() + " = " + eSum.getValue());

                    e2 = i2.next();
                    eSum = iSum.next();
                    continue;
                }

                if (diff < 0) {
                    if (!i2.hasNext())
                        break;

                    e2 = i2.next();
                } else {
                    if (!iSum.hasNext())
                        break;

                    eSum = iSum.next();
                }

                sum = e1.getKey().add(e2.getKey());
            }
        }
    }
}
