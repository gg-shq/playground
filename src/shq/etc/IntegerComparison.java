package shq.etc;

public class IntegerComparison {
    public static void main(String[] args) {

        int v[] = {Integer.MIN_VALUE, -10, -1, 0, 1, 10, Integer.MAX_VALUE};

        for (int i : v) {
            for (int j: v) {
                int m = Integer.signum(i - j);
                int c = Integer.compare(i, j);
                if (m != c) {
                    System.out.println("" + i + " <=> " + j + ": minus=" + m + "; correct=" + c);
                }
            }
        }
    }
}
