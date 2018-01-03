package shq.ocn;

public class TestOCN {

    public static void main(String[] args) {
        OrtodoxCyrillicNumber n1 = new OrtodoxCyrillicNumber("ПОКАЙСЯ");
        OrtodoxCyrillicNumber n2 = new OrtodoxCyrillicNumber("МОЛИТВА");
        OrtodoxCyrillicNumber n3 = new OrtodoxCyrillicNumber("ПОСТ");

        System.out.println(n1.toString() + ": " + n1.toBigInteger().longValue());
        System.out.println(n2.toString() + ": " + n2.toBigInteger().longValue());
        System.out.println(n3.toString() + ": " + n3.toBigInteger().longValue());

        OrtodoxCyrillicNumber result = new OrtodoxCyrillicNumber(
            n1.toBigInteger().add(
                n2.toBigInteger().multiply(
                    n3.toBigInteger())));

        System.out.println(result);

        assert n1.toBigInteger().longValue() == 168103278466L;
        assert n2.toBigInteger().longValue() == 149143339604L;
        assert n3.toBigInteger().intValue() == 2114640;

        assert n1.toString().equals("ПОКАЙСЯ");
        assert n2.toString().equals("МОЛИТВА");
        assert n3.toString().equals("ПОСТ");

        assert result.toString().equals("ДОЛ74НЪХ0ЗФ");
    }
}
