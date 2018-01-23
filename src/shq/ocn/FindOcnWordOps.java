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
    private static final int MAX_SPAN = 8;
    private static final int MAX_SPAN_P = 1 << MAX_SPAN;

    private OrtodoxCyrillicNumber[] ocns;
    private Set<OrtodoxCyrillicNumber> ocnSet;

    public static void main(String[] args) {
        try {
            new FindOcnWordOps().run();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public FindOcnWordOps() throws FileNotFoundException {
        loadDict();
    }

    private void run() {
        sortDict();
        searchForSums_straightfwd_hashmap();
//        searchForSums_3idx();
//        searchForSums_span();
    }

    private void loadDict() throws FileNotFoundException {
        System.out.println("Reading dictionary...");

        // Read into hash dictionary to eliminate duplicates
        // If our dictionary is sorted, we gain additional speedup on sorting it later
        // if we use LinkedHasSet here.
        ocnSet = LineReaderSpliterator.streamOfFileLines(DICT_FILE)
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
    }

    private void sortDict() {
        ocns = ocnSet.toArray(new OrtodoxCyrillicNumber[ocnSet.size()]);

        System.out.println("Sorting dictionary...");

        // Sort using TimSort, which has a great chance to be faster here
        Arrays.sort(ocns, (o1, o2) -> {
            if (o1 == o2)
                return 0;
            if (o1 == null)
                return -1;
            return o1.compareTo(o2);
        });
    }

    private void searchForSums_straightfwd_hashmap() {
        System.out.println("Looking for sums...");

        long startTimeNs = System.nanoTime();

        for (OrtodoxCyrillicNumber e1 : ocnSet) {
            for (OrtodoxCyrillicNumber e2 : ocnSet) {
                if (e1.compareTo(e2) < 0) {
                    OrtodoxCyrillicNumber sum = e1.add(e2);
                    if (ocnSet.contains(sum))
                        System.out.println("==== Found: " + e1 + " + " + e2 + " = " + sum);
                }
            }
        }

        long stopTimeNs = System.nanoTime();
        System.out.printf("Elapsed: %6.2s" + (stopTimeNs - startTimeNs) / 10e-6);
    }

    private void searchForSums_3idx() {
        System.out.println("Building OCN tree...");

        RadixForestIndex<OrtodoxCyrillicNumber> radixForestIndex = new RadixForestIndex<>(OrtodoxCyrillicNumber.BASE,
                ocns[ocns.length - 1].digitCount());

        radixForestIndex.build(ocns);

        System.out.println("Building differences...");

        long[][] ocnDiffs = new long[ocns.length][MAX_SPAN];

        for (int span = 0; span < MAX_SPAN; span++) {
            int s = 1 << span;
            int i = 1;
            for (; i < ocns.length - s; ++i) {
                ocnDiffs[i][span] = ocns[i + s].subtract(ocns[i]).longValue();
            }
            for (; i < ocns.length; ++i) {
                ocnDiffs[i][span] = Long.MAX_VALUE;
            }
        }

        System.out.println("Looking for sums...");

        long startTimeNs = System.nanoTime();

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

            if (index.getKey())
                assert ocns[index.getValue()].compareTo(e1e2sum) == 0;
            else
                assert ocns[index.getValue()].compareTo(e1e2sum) > 0;

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

            int e2warp = 0;
            int e3warp = 0;

            for (;;) {
                ++pairsAnalysed;

                if ((pairsAnalysed & REPORT_ITER_MASK) == 0) {
                    long currentTimeMillis = System.currentTimeMillis();

                    if ( currentTimeMillis > nextReportTime) {
                        System.out.println("@ " + e1 + " + " + e2 + " = " + e1e2sum + " ~> " + e3
                                + "; " + i1 + " + " + i2 + " ~ " + i3 + " < " + ocns.length
                                + "; tree savings=" + (float) treeSavings / treeUses
                                + "; pairs=" + treeUses + " triples=" + pairsAnalysed);

                        nextReportTime = currentTimeMillis + REPORT_TIME_MS;
                    }
                }

                long s = e1e2sum.compareTo(e3);
                long diff = (s > 0) ? e1e2sum.subtract(e3).longValue() : (s < 0) ? e3.subtract(e1e2sum).longValue() : 0;

                if (s == 0) {
                    assert e1.toBigInteger().add(e2.toBigInteger()).equals(e3.toBigInteger());

                    System.out.println("==== Found: " + e1 + " + " + e2 + " = " + e3);

                    ++i2;
                    ++i3;

                    if (i3 >= ocns.length)
                        break;

                    e2 = ocns[i2];
                    e3 = ocns[i3];
                    e1e2sum = e1.add(e2);

//                    System.out.println("e2warp=" + e2warp + "; e3warp=" + e3warp);
//                    e2warp = 1;
//                    e3warp = 1;
                }
                else if (s < 0) {
                    // TODO: check for MIN_VALUE

                    diff = -diff;

                    long[] diffs = ocnDiffs[i2];
                    for (int span = 0; span <= MAX_SPAN; ++span) {
                        if (span == MAX_SPAN || diffs[span] > diff) {
                            if (span > 0) {
                                i2 += 1 << (span - 1);
                                treeSavings += (1 << span) - span;
                            } else {
                                ++i2;
                                treeSavings -= span;
                            }
                            break;
                        }
                    }

                    if (i2 >= ocns.length)
                        break;

                    if (i2 < i3) {
                        e2 = ocns[i2];
                        e1e2sum = e1.add(e2);

//                        ++e2warp;
//                        if (e3warp > 1)
//                            System.out.println("           e3warp=" + e3warp);
//                        e3warp = 1;
                    }
                    else {
                        i3 = i2 + 1;

                        if (i3 >= ocns.length)
                            break;

                        e2 = ocns[i2];
                        e1e2sum = e1.add(e2);

                        e3 = ocns[i3];

//                        ++e3warp;
                    }
                } else {
                    long[] diffs = ocnDiffs[i3];
                    for (int span = 0; span <= MAX_SPAN; ++span) {
                        if (span == MAX_SPAN || diffs[span] > diff) {
                            if (span > 0) {
                                i3 += 1 << (span - 1);
                                treeSavings += (1 << span) - span;
                            } else {
                                ++i3;
                                treeSavings -= span;
                            }
                            break;
                        }
                    }

                    if (i3 >= ocns.length)
                        break;

                    e3 = ocns[i3];

//                    ++e3warp;
//                    if (e2warp > 1)
//                        System.out.println("e2warp=" + e2warp);
//                    e2warp = 1;

                    assert i2 < i3;
                }
            }
        }

        long stopTimeNs = System.nanoTime();

        System.out.println("Done");
        System.out.printf("Elapsed: %6.2s" + (stopTimeNs - startTimeNs) / 10e-6);
    }

    private void searchForSums_span() {
        System.out.println("Building OCN tree...");

        RadixForestIndex<OrtodoxCyrillicNumber> radixForestIndex = new RadixForestIndex<>(
                OrtodoxCyrillicNumber.BASE, ocns[ocns.length - 1].digitCount());

        radixForestIndex.build(ocns);

        System.out.println("Looking for sums...");

        long startTimeNs = System.nanoTime();

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

        long stopTimeNs = System.nanoTime();
        System.out.printf("Elapsed: %6.2s" + (stopTimeNs - startTimeNs) / 10e-6);
    }
}
