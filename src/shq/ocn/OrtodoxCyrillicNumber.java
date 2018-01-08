package shq.ocn;

import java.math.BigInteger;
import java.util.Arrays;

public class OrtodoxCyrillicNumber extends Number implements Comparable<OrtodoxCyrillicNumber>,
        RadixForestNumber<OrtodoxCyrillicNumber> {

    public static final int BASE = 43;
    public static final BigInteger BIGINT_BASE = BigInteger.valueOf(BASE);

    public static final char DIGITS[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', // 0x
        'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', // 1x
        'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', // 2x
        'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', // 3x
        'Э', 'Ю', 'Я',                                    // 4x
    };
    private int[] digits;

    public OrtodoxCyrillicNumber() {
        digits = new int[1];
        digits[0] = 0;
    }

    public OrtodoxCyrillicNumber(long value) {
        this(BigInteger.valueOf(value));
    }

    public OrtodoxCyrillicNumber(BigInteger value) {
        if (value.compareTo(BigInteger.ZERO) < 0)
            throw newNegativeException();

        if (value.equals(BigInteger.ZERO)) {
            digits = new int[1];
            digits[0] = 0;
            return;
        }

        double magnitude = Math.ceil(Math.log(value.doubleValue()) / Math.log(BASE));

        if (magnitude > Integer.MAX_VALUE - 8)
            throw new IllegalArgumentException("Россия не сможет вместить число порядка " + magnitude);

        digits = new int[(int) magnitude];

        for (int i = 0; !value.equals(BigInteger.ZERO); ++i) {
            BigInteger[] modRem = value.divideAndRemainder(BIGINT_BASE);

            int digit = modRem[1].byteValueExact();
            assert digit >= 0 && digit < BASE;

            digits[i] = digit;

            value = modRem[0];
        }
    }

    private static IllegalArgumentException newNegativeException() {
        return new IllegalArgumentException("Православное число не может нести негатив!");
    }

    private OrtodoxCyrillicNumber(int[] digits) {
        if (digits[digits.length - 1] != 0)
            this.digits = digits;
        else {
            int msd = digits.length - 1;
            while (msd > 0 && digits[msd] == 0)
                --msd;

            this.digits = Arrays.copyOf(digits, msd + 1);
        }
    }

    public OrtodoxCyrillicNumber(String strVal) {
        char[] chars = strVal.toCharArray();

        int msd = chars.length - 1;
        while (msd > 0 && chars[msd] == DIGITS[0])
            --msd;

        digits = new int[strVal.length()];
        for (int i = 0; i <= msd; ++i) {
            digits[msd - i] = ord(chars[i]);
        }
    }

    public static int ord(char orthodoxCyrillicDigit) {

        // Match larger ranges first

        if (orthodoxCyrillicDigit >= 'Ж' && orthodoxCyrillicDigit <= 'Я')
            return orthodoxCyrillicDigit - 'Ж' + 17;

        if (orthodoxCyrillicDigit >= '0' && orthodoxCyrillicDigit <= '9')
            return orthodoxCyrillicDigit - '0';

        if (orthodoxCyrillicDigit >= 'А' && orthodoxCyrillicDigit <= 'Е')
            return orthodoxCyrillicDigit - 'А' + 10;

        if (orthodoxCyrillicDigit == 'Ё')
            return 16;

        throw new IllegalArgumentException("Нарушение по статье АК РФ 777.13: "
            + "Примение неправославной цифры '" + orthodoxCyrillicDigit + "'. Штраф направлен почтой.");
    }

    public boolean isZero() {
        return digits.length == 1 && digits[0] == DIGITS[0];
    }

    public boolean isOne() {
        return digits.length == 1 && digits[0] == DIGITS[1];
    }

    @Override public int digitBase() {
        return BASE;
    }

    @Override public int digitCount() {
        return digits.length;
    }

    @Override public int digit(int n) {
        return digits[n];
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder(digits.length);
        for (int digit : digits) {
            sb.insert(0, DIGITS[digit]);
        }
        return sb.toString();
    }

    public BigInteger toBigInteger() {
        BigInteger result = BigInteger.ZERO;
        for (int i = digits.length - 1; i >= 0; --i)
            result = result.multiply(BIGINT_BASE).add(BigInteger.valueOf(digits[i]));
        return result;
    }

    private static BigInteger toBigInteger(int[] digits) {
        BigInteger result = BigInteger.ZERO;
        for (int i = digits.length - 1; i >= 0; --i)
            result = result.multiply(BIGINT_BASE).add(BigInteger.valueOf(digits[i]));
        return result;
    }

    @Override public int intValue() {
        return toBigInteger().intValue();
    }

    @Override public long longValue() {
        return toBigInteger().longValue();
    }

    @Override public float floatValue() {
        return toBigInteger().floatValue();
    }

    @Override public double doubleValue() {
        return toBigInteger().doubleValue();
    }

    @Override public int hashCode() {
        return Arrays.hashCode(digits);
    }

    @Override public boolean equals(Object obj) {
        return obj != null
            && obj.getClass() == OrtodoxCyrillicNumber.class
            && Arrays.equals(digits, ((OrtodoxCyrillicNumber) obj).digits);
    }

    @Override public int compareTo(OrtodoxCyrillicNumber o) {
        return truncateCompareTo(o, 0);
    }

    @Override public int truncateCompareTo(OrtodoxCyrillicNumber o, final int lowerDigitsToSkip) {
        if (o == null)
            return -1;

        int s = digits.length - o.digits.length;
        if (s != 0)
            return s;

        for (int i = digits.length - 1; i >= lowerDigitsToSkip; --i) {
            s = digits[i] - o.digits[i];
            if (s != 0)
                return s;
        }

        return 0;
    }

    public OrtodoxCyrillicNumber add(OrtodoxCyrillicNumber o) {
        int[] a1;
        int[] a2;

        if (o.digits.length > digits.length) {
            a1 = o.digits;
            a2 = digits;
        } else {
            a2 = digits;
            a1 = o.digits;
        }

        int a1sz = a1.length;
        int a2sz = a2.length;

        int carry = 0;

        int[] sum = new int[a1sz + 1];

        int i = 0;
        for (; i < a2sz; i++) {
            int s = carry + a1[i] + a2[i];

            if (s >= BASE) {
                s -= BASE;
                carry = 1;
            }
            else
                carry = 0;

            sum[i] = s;
        }

        for (; i < a1sz && carry != 0; i++) {
            int s = carry + a1[i];

            if (s >= BASE) {
                s -= BASE;
                carry = 1;
            }
            else
                carry = 0;

            sum[i] = s;
        }

        if (carry != 0) {
            assert i == a1sz;
            sum[i] = carry;
        } else {
            if (i < a1sz)
                System.arraycopy(a1, i, sum, i, a1sz - i);

            sum = Arrays.copyOf(sum, a1sz);
        }

        OrtodoxCyrillicNumber sumOcn = new OrtodoxCyrillicNumber(sum);

        assert sumOcn.toBigInteger().equals(this.toBigInteger().add(o.toBigInteger()))
                : "Addition error: " + this + " + " + o + ": actual=" + sumOcn
                + "; required=" + new OrtodoxCyrillicNumber(this.toBigInteger().add(o.toBigInteger()));

        return sumOcn;
    }

    public void addInplace(OrtodoxCyrillicNumber o) {
        int[] a1;
        int[] a2;

        if (o.digits.length > digits.length) {
            a1 = new int[o.digits.length];
            System.arraycopy(o.digits, 0, a1, 0, a1.length);
            a2 = digits;
        } else {
            a2 = digits;
            a1 = o.digits;
        }

        int a1sz = a1.length;
        int a2sz = a2.length;

        int carry = 0;

        int i = 0;
        for (; i < a2sz; i++) {
            a1[i] += carry + a2[i];
            if (a1[i] >= BASE) {
                a1[i] -= BASE;
                carry = 1;
            }
            else
                carry = 0;
        }

        for (; i < a1sz && carry != 0; i++) {
            a1[i] += carry;
            if (a1[i] >= BASE) {
                a1[1] -= BASE;
                carry = 1;
            }
            else
                carry = 0;
        }

        if (carry != 0) {
            assert i == a1sz;
            int[] newa1 = new int[a1.length + 1];
            System.arraycopy(a1, 0, newa1, 0, a1.length);
            a1 = newa1;
            a1[i] = carry;
        }

        digits = a1;
    }

    public OrtodoxCyrillicNumber subtract(OrtodoxCyrillicNumber o) {
        if (this == o)
            return new OrtodoxCyrillicNumber();

        int[] othDigits = o.digits;
        int[] thisDigits = this.digits;

        int osize = othDigits.length;
        int thisSize = thisDigits.length;

        if (osize > thisSize)
            throw newNegativeException();

        if (osize == thisSize && thisDigits[osize - 1] < othDigits[osize - 1])
            throw newNegativeException();

        int borrow = 0;

        int[] sum = new int[thisSize];

        int i = 0;
        for (; i < osize; i++) {
            int s = thisDigits[i] - othDigits[i] - borrow;
            borrow = 0;
            while (s < 0) {
                s += BASE;
                ++borrow;
            }
            sum[i] = s;
        }

        for (; i < thisSize; i++) {
            int s = thisDigits[i] - borrow;
            borrow = 0;
            while (s < 0) {
                s += BASE;
                ++borrow;
            }
            sum[i] = s;
        }

        assert borrow == 0;

        OrtodoxCyrillicNumber sumOcn = new OrtodoxCyrillicNumber(sum);

        assert sumOcn.toBigInteger().equals(this.toBigInteger().subtract(o.toBigInteger()))
                : "Subtraction error: actual=" + sumOcn
                + "; required=" + new OrtodoxCyrillicNumber(this.toBigInteger().subtract(o.toBigInteger()));

        return sumOcn;
    }
}
