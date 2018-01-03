package shq.ocn;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class OrtodoxCyrillicNumber implements Comparable<OrtodoxCyrillicNumber> {

    public static final long BASE = 43;
    public static final BigInteger BIGINT_BASE = BigInteger.valueOf(BASE);

    public static final char DIGITS[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И',
        'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т',
        'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь',
        'Э', 'Ю', 'Я',
    };
    private final List<Integer> digits;

    public OrtodoxCyrillicNumber() {
        this(0);
    }

    public OrtodoxCyrillicNumber(long value) {
        this(BigInteger.valueOf(value));
    }

    public OrtodoxCyrillicNumber(BigInteger value) {
        if (value.compareTo(BigInteger.ZERO) < 0)
            throw new IllegalArgumentException("Православное число не может нести негатив!");

        if (value.equals(BigInteger.ZERO)) {
            digits = new ArrayList<>(0);
            return;
        }

        double magnitude = Math.ceil(Math.log(value.doubleValue()) / Math.log(BASE));

        if (magnitude > Integer.MAX_VALUE - 8)
            throw new IllegalArgumentException("Россия не сможет вместить число порядка " + magnitude);

        digits = new ArrayList<>((int) magnitude);

        while (!value.equals(BigInteger.ZERO)) {
            BigInteger[] modRem = value.divideAndRemainder(BIGINT_BASE);

            int digit = modRem[1].byteValueExact();
            assert digit >= 0 && digit < BASE;

            digits.add(digit);

            value = modRem[0];
        }
    }

    private OrtodoxCyrillicNumber(int[] digits) {
        this.digits = new ArrayList<>(digits.length);
        for (int digit : digits) {
            assert digit >= 0 && digit < BASE;
            this.digits.add(digit);
        }
    }

    public OrtodoxCyrillicNumber(String strVal) {
        this.digits = new ArrayList<>(strVal.length());
        for (char c : strVal.toCharArray()) {
            this.digits.add(0, ord(c));
        }
    }

    private OrtodoxCyrillicNumber(ArrayList<Integer> digits) {
        this.digits = digits;
    }

    public static int ord(char orthodoxCyrillicDigit) {
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

    @Override public String toString() {
        StringBuilder sb = new StringBuilder(digits.size());
        for (int digit : digits) {
            sb.insert(0, DIGITS[digit]);
        }
        return sb.toString();
    }

    public BigInteger toBigInteger() {
        BigInteger result = BigInteger.ZERO;
        for (ListIterator<Integer> iter = digits.listIterator(digits.size()); iter.hasPrevious(); ) {
            int digit = iter.previous();
            result = result.multiply(BIGINT_BASE).add(BigInteger.valueOf(digit));
        }
        return result;
    }

    @Override public int hashCode() {
        return digits.hashCode();
    }

    @Override public boolean equals(Object obj) {
        return obj != null
            && obj.getClass() == OrtodoxCyrillicNumber.class
            && digits.equals(((OrtodoxCyrillicNumber) obj).digits);
    }

    @Override public int compareTo(OrtodoxCyrillicNumber o) {
        int s = digits.size() - o.digits.size();
        if (s != 0)
            return s;

        for (int i = digits.size() - 1; i >= 0; --i) {
            s = digits.get(i) - o.digits.get(i);
            if (s != 0)
                return s;
        }

        return 0;
    }

    public OrtodoxCyrillicNumber add(OrtodoxCyrillicNumber o) {

        List<Integer> a1, a2;

        if (o.digits.size() > digits.size()) {
            a1 = o.digits;
            a2 = digits;
        } else {
            a2 = digits;
            a1 = o.digits;
        }

        int a1sz = a1.size();
        int a2sz = a2.size();

        int carry = 0;

        int[] sum = new int[a1sz + 1];

        int i = 0;
        for (; i < a2sz; i++) {
            int s = carry + a1.get(i) + a2.get(i);
            carry = 0;
            while (s >= BASE) {
                s -= BASE;
                ++carry;
            }
            sum[i] = s;
        }

        for (; i < a1sz; i++) {
            int s = carry + a1.get(i);
            carry = 0;
            while (s >= BASE) {
                s -= BASE;
                ++carry;
            }
            sum[i] = s;
        }

        if (carry != 0)
            sum[i] = carry;

        OrtodoxCyrillicNumber sumOcn = new OrtodoxCyrillicNumber(sum);
        assert sumOcn.toBigInteger().equals(this.toBigInteger().add(o.toBigInteger()));
        return sumOcn;
    }
}
