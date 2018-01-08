package shq.ocn;

import javafx.util.Pair;

import java.util.Arrays;

/**
 * Radix tree forest (a tree per each possible OCN length)
 * for fast searching for a OCN in a sorted array.
 */
public class RadixForestIndex<T extends RadixForestNumber> {

    private static final int NOT_INITIALIZED = -1;

    private static final Node NUMBER_EXISTS_LEVEL0_SENTINEL = new Node(0);

    private static class Node {

        private final int[] digitOffset;
        private final Node[] subNodes;

        Node(int base) {
            digitOffset = new int[base];
            Arrays.fill(digitOffset, NOT_INITIALIZED);

            subNodes = new Node[base];
        }

        int digitOffset(int n) {
            assert digitOffset[n] != NOT_INITIALIZED;
            return digitOffset[n];
        }

        void digitOffset(int n, int offset) {
            assert offset != NOT_INITIALIZED;
            digitOffset[n] = offset;
        }

        Node subNode(int i) {
            return subNodes[i];
        }

        void subNode(int i, Node node) {
            subNodes[i] = node;
        }
    }

    private final int radix;
    private final int[] rootOffsets;
    private final Node[] rootNodes;
    private int indexedArraySize;

    public RadixForestIndex(int radix, int maxWordSize) {
        this.radix = radix;

        rootOffsets = new int[maxWordSize];
        Arrays.fill(rootOffsets, NOT_INITIALIZED);

        rootNodes = new Node[maxWordSize];
        indexedArraySize = 0;
    }

    public Pair<Boolean, Integer> find(T ocn) {
        int d = ocn.digitCount();

        if (rootNodes[d] != null)
            return find(ocn, rootNodes[d], d - 1);
        else {
            assert rootOffsets[d] != NOT_INITIALIZED;
            return new Pair<>(false, rootOffsets[d]);
        }
    }

    private Pair<Boolean, Integer> find(T ocn, Node node, int d) {
        int digit = ocn.digit(d);

        int offset = node.digitOffset(digit);
        assert offset != NOT_INITIALIZED;
        if (d == 0)
            return new Pair<>(node.subNode(digit) == NUMBER_EXISTS_LEVEL0_SENTINEL, offset);

        Node subNode = node.subNode(digit);
        if (subNode != null)
            return find(ocn, subNode, d - 1);
        else
            return new Pair<>(false, offset);
    }

    private static class Scanner<T extends RadixForestNumber> {
        private final T[] ocns;
        private int offset;

        Scanner(T[] ocns) {
            this.ocns = ocns;
            offset = 0;
        }

        int offset() {
            return offset;
        }

        T cur() {
            return ocns[offset];
        }

        boolean hasNext() {
            return offset < ocns.length - 1;
        }

        void advance() {
            assert ocns[offset].compareTo(ocns[offset + 1]) < 0;
            ++offset;
        }
    }

    public void build(T[] ocns) {
        Scanner scanner = new Scanner<T>(ocns);

        int sizeToFill = 0;
        while (scanner.hasNext()) {
            int size = scanner.cur().digitCount();
            int offset = scanner.offset();

            Node rootNode = buildNode(scanner, size - 1);

            rootNodes[size] = rootNode;

            for (; sizeToFill <= size; ++sizeToFill)
                rootOffsets[sizeToFill] = offset;
        }

        indexedArraySize = scanner.offset();

        for (; sizeToFill < rootOffsets.length; ++sizeToFill)
            rootOffsets[sizeToFill] = indexedArraySize;
    }

    private Node buildNode(Scanner<T> scanner, int curLevel) {
        Node node = new Node(radix);

        T curOcn = scanner.cur();

        int lastDigitToFill = 0;

        for (;;) {
            int curDigit = curOcn.digit(curLevel);

            for (; lastDigitToFill <= curDigit; ++lastDigitToFill)
                node.digitOffset(lastDigitToFill, scanner.offset());

            if (curLevel <= 0) {
                node.subNode(curDigit, NUMBER_EXISTS_LEVEL0_SENTINEL);

                if (!scanner.hasNext())
                    break;

                scanner.advance();
            }
            else {
                Node subNode = buildNode(scanner, curLevel - 1);

                node.subNode(curDigit, subNode);

                if (!scanner.hasNext())
                    break;
            }

            T nextOcn = scanner.cur();

            int cmp = nextOcn.truncateCompareTo(curOcn, curLevel + 1);
            if (cmp > 0)
                break;
            assert cmp == 0;

            curOcn = nextOcn;
        }

        for (; lastDigitToFill < radix; ++lastDigitToFill)
            node.digitOffset(lastDigitToFill, scanner.offset());

        return node;
    }
}
