package shq.ocn;

import javafx.util.Pair;
import shq.etc.Util;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindOcnWordOps {

    private static final OrtodoxCyrillicNumber SENTINEL_OCN = new OrtodoxCyrillicNumber("ПРОГРАММИСТ");

    public static final String DICT_FILE = "src/shq/ocn/lop1v2.txt";
    private static final int WORD_COUNT_ESTIMATION = 170_000;
    private static final long REPORT_TIME_MS = 10_000;
    public static final int REPORT_ITER_MASK = 0xfff;

    private OrtodoxCyrillicNumber[] ocns;

    public static void main(String[] args) {
        try {
            new FindOcnWordOps().run();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public FindOcnWordOps() throws FileNotFoundException {
        ocns = loadDict();
    }

    private void run() {
        //searchForSums2();
        searchForSums();
    }

    private OrtodoxCyrillicNumber[] loadDict() throws FileNotFoundException {
        System.out.println("Reading dictionary...");

        // Read into hash dictionary to eliminate duplicates
        // If our dictionary is sorted, we gain additional speedup on sorting it later
        // if we use LinkedHasSet here.
        Set<OrtodoxCyrillicNumber> ocnSet = LineReaderSpliterator.streamOfFileLines(DICT_FILE)
                .flatMap(line -> Stream.of(line.trim().toUpperCase().split("[-. :(),!\\?\"'`]")))
                .map(word -> word.trim())
                .filter(word -> word.length() >= 3)
                .map(word -> {
                    try {
                        return new OrtodoxCyrillicNumber(word);
                    } catch (Exception e) {
                        System.out.println("Can't parse OCN " + word + ": " + e.getMessage());
                        return SENTINEL_OCN;
                    }
                })
                .collect(Collectors.toCollection(() -> new LinkedHashSet<>(WORD_COUNT_ESTIMATION)));

        OrtodoxCyrillicNumber[] ocns = ocnSet.toArray(new OrtodoxCyrillicNumber[ocnSet.size()]);

        System.out.println("Sorting dictionary...");

        // Sort using TimSort, which has a great chance to be faster here
        Arrays.sort(ocns, (o1, o2) -> {
            if (o1 == o2)
                return 0;
            if (o1 == null)
                return -1;
            return o1.compareTo(o2);
        });

        return ocns;
    }

    private void searchForSums() {
        System.out.println("Building OCN tree...");

        RadixForestIndex<OrtodoxCyrillicNumber> radixForestIndex = new RadixForestIndex<>(OrtodoxCyrillicNumber.BASE,
                ocns[ocns.length - 1].digitCount());

        radixForestIndex.build(ocns);

        System.out.println("Looking for sums...");

        long nextReportTime = System.currentTimeMillis() + REPORT_TIME_MS;

        long pairsAnalysed = 0;

        long treeUses = 0;
        long treeSavings = 0;

        for (int i1 = 0; i1 < ocns.length; ++i1) {
            OrtodoxCyrillicNumber e1 = ocns[i1];
            if (e1.isZero())
                continue;

            int i2 = i1;
            OrtodoxCyrillicNumber e2 = ocns[i2];

            assert ! e2.isZero();

            if (i2 >= ocns.length)
                continue;

            OrtodoxCyrillicNumber e1e2sum = e1.add(e2);

            //int i3 = i2 + 1;

            Pair<Boolean, Integer> index = radixForestIndex.find(e1e2sum);

            if (index.getValue() >= ocns.length) // finished
                break;

            if (index.getKey()) {
                if (ocns[index.getValue()].compareTo(e1e2sum) != 0) {
                    radixForestIndex.find(e1e2sum);
                }
                assert ocns[index.getValue()].compareTo(e1e2sum) == 0;
            }
            else
                assert ocns[index.getValue()].compareTo(e1e2sum) > 0;

            if (index.getValue() < 0 || ocns[index.getValue() - 1].compareTo(e1e2sum) >= 0) {
                radixForestIndex.find(e1e2sum);
            }

            assert ocns[index.getValue() - 1].compareTo(e1e2sum) < 0
                : "should be: " + ocns[index.getValue() - 1] + " < " + e1e2sum
                    + " < " + ocns[index.getValue()];

            int i3 = index.getValue();

            assert i2 <= i3 : "" + i1 + " + " + i2 + " >= " + i3;

            ++treeUses;
            // last term is penalty for tree scan compared to simple forward array scan
            treeSavings += (i3 - (i2 + 1) - e1e2sum.digitCount());

            if (i3 >= ocns.length)
                continue;

            OrtodoxCyrillicNumber e3 = ocns[i3];

            for (;;) {
                ++pairsAnalysed;

                long currentTimeMillis = System.currentTimeMillis();

                if ((pairsAnalysed & REPORT_ITER_MASK) == 0 && currentTimeMillis > nextReportTime) {

                    System.out.println("@ " + e1 + " + " + e2 + " = " + e1e2sum + " ~> " + e3
                        + "; " + i1 + " + " + i2 + " ~ " + i3 + " < " + ocns.length
                        + "; tree savings=" + (float) treeSavings / treeUses
                        + "; pairs=" + treeUses + " triples=" + pairsAnalysed);

                    nextReportTime = currentTimeMillis + REPORT_TIME_MS;
                }

                int diff = e1e2sum.compareTo(e3);

                if (diff == 0) {
                    assert e1.toBigInteger().add(e2.toBigInteger()).equals(e3.toBigInteger());

                    System.out.println("==== Found: " + e1 + " + " + e2 + " = " + e3);

                    ++i2;
                    ++i3;

                    if (i3 >= ocns.length)
                        break;

                    e2 = ocns[i2];
                    e3 = ocns[i3];
                    e1e2sum = e1.add(e2);
                    continue;
                }

                if (diff < 0) {
                    ++i2;

                    if (i2 >= ocns.length)
                        break;

                    e2 = ocns[i2];
                    e1e2sum = e1.add(e2);

                    if (i2 < i3)
                        continue;
                }

                ++i3;

                if (i3 >= ocns.length)
                    break;

                e3 = ocns[i3];

                assert i2 < i3;
            }
        }

        System.out.println("Done");
    }

    private void searchForSums2() {
        System.out.println("Building OCN tree...");

        RadixForestIndex<OrtodoxCyrillicNumber> radixForestIndex = new RadixForestIndex<>(
                OrtodoxCyrillicNumber.BASE, ocns[ocns.length - 1].digitCount());

        radixForestIndex.build(ocns);

        System.out.println("Looking for sums...");

        int span = 1;
        while (span < ocns.length) {
            TreeMap<OrtodoxCyrillicNumber, Set<Integer>> distances = new TreeMap<>();

            System.out.println("Span " + span + ": Building distances...");

            for (int i = 0; i < ocns.length - span; ++i) {
                OrtodoxCyrillicNumber e1 = ocns[i];
                OrtodoxCyrillicNumber e2 = ocns[i + span];
                OrtodoxCyrillicNumber diff = e2.subtract(e1);

                distances.merge(diff, Util.modifiableSingletonSet(i, HashSet::new), (o, n) -> {
                    o.addAll(n);
                    return o;
                });
            }

            System.out.println("Entries: " + distances.size());

            if (distances.size() == 0)
                continue;

            OrtodoxCyrillicNumber firstDiff = distances.keySet().iterator().next();

            Pair<Boolean, Integer> index = radixForestIndex.find(firstDiff);
            int ocnIdx = index.getValue();

            for (Map.Entry<OrtodoxCyrillicNumber, Set<Integer>> distance : distances.entrySet()) {
                OrtodoxCyrillicNumber e1;
                int cmp;
                while ((cmp = (e1 = ocns[ocnIdx]).compareTo(distance.getKey())) < 0 && ocnIdx < ocns.length)
                    ++ocnIdx;

                if (cmp == 0) {
                    for (int idx : distance.getValue()) {
                        OrtodoxCyrillicNumber e2 = ocns[idx];
                        OrtodoxCyrillicNumber eSum = ocns[idx + span];

                        assert e1.toBigInteger().add(e2.toBigInteger()).equals(eSum.toBigInteger());

                        System.out.println("==== Found: " + e1 + " + " + e2 + " = " + eSum);
                    }
                }

                if (ocnIdx >= ocns.length)
                    break;
            }

            System.out.println("Done");

            ++span;
        }
    }
}
