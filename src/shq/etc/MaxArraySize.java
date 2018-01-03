package shq.etc;

public class MaxArraySize {
    public static void main(String[] args) {
        try {
            allocByteArray();
            allocIntArray();
            allocLongArray();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void allocIntArray() {
        int ii[] = new int[0];

        for (int sz = (int) ((long) Integer.MAX_VALUE + 1 / 2), step = sz; sz > 0; sz += step) {
            try {
                ii = new int[sz];
                step = Math.abs(step) / 2;
            }
            catch (OutOfMemoryError e) {
                step = -Math.abs(step) / 2;
            }

            if (step == 0) {
                break;
            }
        }

        System.out.println("Max. int[] size: 0x" + Long.toHexString(ii.length) + ", which is 0x" + Long.toHexString(Integer.MAX_VALUE - ii.length)
            + " less than Integer.MAX_VALUE or 0x" + Long.toHexString((long) ii.length * Integer.BYTES) + " bytes");
    }

    private static void allocLongArray() {
        long ll[] = new long[0];

        for (int sz = Integer.MAX_VALUE / 2, step = sz; sz > 0; sz += step) {
            try {
                ll = new long[sz];
                step = Math.abs(step) / 2;
            }
            catch (OutOfMemoryError e) {
                step = -Math.abs(step) / 2;
            }

            if (step == 0) {
                break;
            }
        }

        System.out.println("Max. long[] size: 0x" + Long.toHexString(ll.length) + ", which is 0x" + Long.toHexString(Integer.MAX_VALUE - ll.length)
            + " less than Integer.MAX_VALUE or 0x" + Long.toHexString((long) ll.length * Long.BYTES) + " bytes");
    }

    private static void allocByteArray() {
        byte bb[] = new byte[0];

        for (int sz = Integer.MAX_VALUE; sz > 0; --sz) {
            try {
                bb = new byte[sz];
                break;
            }
            catch (OutOfMemoryError e) {
                // proceed
            }
        }

        System.out.println("Max. byte[] size: " + bb.length + ", which is " + (Integer.MAX_VALUE - bb.length)
            + " less than Integer.MAX_VALUE or 0x" + Long.toHexString(bb.length) + " bytes");
    }
}
