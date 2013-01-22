package helpers.util;

public class Flags {
	private static int flag_mask(int n) {
		StringBuffer out = new StringBuffer();
		for (int i=0;i<32;i++) {
			if (i == (32-n))
				out.append("1");
			else
				out.append("0");
		}
		return Integer.parseInt(out.toString(), 2);
	}
	
	public static int F_00 = flag_mask(0);
	public static int F_01 = flag_mask(1);
	public static int F_02 = flag_mask(2);
	public static int F_03 = flag_mask(3);
	public static int F_04 = flag_mask(4);
	public static int F_05 = flag_mask(5);
	public static int F_06 = flag_mask(6);
	public static int F_07 = flag_mask(7);
	public static int F_08 = flag_mask(8);
	public static int F_09 = flag_mask(9);
	public static int F_10 = flag_mask(10);
	public static int F_11 = flag_mask(11);
	public static int F_12 = flag_mask(12);
	public static int F_13 = flag_mask(13);
	public static int F_14 = flag_mask(14);
	public static int F_15 = flag_mask(15);
	public static int F_16 = flag_mask(16);
	public static int F_17 = flag_mask(17);
	public static int F_18 = flag_mask(18);
	public static int F_19 = flag_mask(19);
	public static int F_20 = flag_mask(20);
	public static int F_21 = flag_mask(21);
	public static int F_22 = flag_mask(22);
	public static int F_23 = flag_mask(23);
	public static int F_24 = flag_mask(24);
	public static int F_25 = flag_mask(25);
	public static int F_26 = flag_mask(26);
	public static int F_27 = flag_mask(27);
	public static int F_28 = flag_mask(28);
	public static int F_29 = flag_mask(29);
	public static int F_30 = flag_mask(30);
	public static int F_31 = flag_mask(31);
	
	public static boolean isset(int flags, int mask) {
		return (flags & mask) == mask;
	}
	public static int set(int flags, int mask) {
		return flags | mask;
	}
	public static int unset(int flags, int mask) {
		return flags & ~mask;
	}
	public static int toggle(int flags, int mask) {
		return flags ^ mask;
	}
}
