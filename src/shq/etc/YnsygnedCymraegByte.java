package shq.etc;

public class YnsygnedCymraegByte {

    private static int ubyteToInt(byte b) {
        return b & 0xFF;
    }

    private static void chwaraeGyda(byte b) {
        int r = ubyteToInt(b);

        System.out.println(b + " => " + r);
    }

    public static void main(String[] args) {
        chwaraeGyda((byte) -128);
        chwaraeGyda((byte) -127);
        chwaraeGyda((byte) -1);
        chwaraeGyda((byte) 0);
        chwaraeGyda((byte) 1);
        chwaraeGyda((byte) 126);
        chwaraeGyda((byte) 127);
        chwaraeGyda((byte) 128);
        chwaraeGyda((byte) 200);
        chwaraeGyda((byte) 255);
        chwaraeGyda((byte) 256);
        chwaraeGyda((byte) Integer.MAX_VALUE);
    }
}
