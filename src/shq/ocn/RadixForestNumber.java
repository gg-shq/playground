package shq.ocn;

public interface RadixForestNumber<T> extends Comparable<T> {

    int truncateCompareTo(T o, final int lowerDigitsToSkip);

    default int compareTo(T o) {
        return truncateCompareTo(o, 0);
    }

    int digitBase();

    int digitCount();

    int digit(int n);
}
