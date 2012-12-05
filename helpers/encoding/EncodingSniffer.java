package helpers.encoding;


public class EncodingSniffer {
	
	 /**************************************************************
	 * http://en.wikipedia.org/wiki/Cp1252#Codepage_layout         *
	 * http://en.wikipedia.org/wiki/ISO/IEC_8859-1#Codepage_layout *
	 * http://en.wikipedia.org/wiki/Utf-8#Codepage_layout          *
	 **************************************************************/
	
	 /***********************************************************************************
	 *  UTF-8 patterns                                                                  *
	 *  http://triptico.com/docs/unicode.html                                           *
	 *  U+00000000 - U+0000007F: 0xxxxxxx                                               *
	 *  U+00000080 - U+000007FF: 110xxxxx 10xxxxxx                                      *
	 *  U+00000800 - U+0000FFFF: 1110xxxx 10xxxxxx 10xxxxxx                             *
	 *  U+00010000 - U+001FFFFF: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx                    *
	 *  U+00200000 - U+03FFFFFF: 111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx           *
	 *  U+04000000 - U+7FFFFFFF: 1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx  *
	 ***********************************************************************************/
	
	/**
	 * Nota bene: byte e' signed, quindi per caratteri > 128 considerare che il valore sara' negativo
	 */
	
//	public static void main(String[] args) throws Exception {
//		String[] files = new File("input").list();
//		for (int i=0;i<files.length;i++) {
//			String fileName = "input/" + files[i];
//			
//			System.out.println("fileName: " + fileName);
//		String fileName = "input/PLNAA-2012-00157320120326104830_MODI.xml";
//			System.out.println("encoding: "+guessEncoding(IO.readFile(fileName)));
//		}
//	}
	// java coi byte signed nun se po vede
	public static String guessEncoding(byte[] blob) {
		if (blob.length > 2) {
			if (blob[0] == (byte)0xEF && blob[1] == (byte)0xBB && blob[2] == (byte)0xBF) {
				// bom utf-8 presente
				return "UTF-8";
			}
		}
		boolean excludeCp1252 = false;
		boolean excludeLatin1 = false;
		boolean validUtf8Sequence = false;
		boolean isUtf8Sequence = false;
		boolean containsUtf8Sequences = false;
		boolean latin1GrayArea = false;
		
		for (int i = 0; i < blob.length; i++) {
			byte b = blob[i];
			
			if (b == (byte)0x7F || b >= (byte)0x80 && b <= (byte)0xA0) {
				// non e' latin1
				excludeLatin1 = true;
			}
			if (b == (byte)0x81 || b == (byte)0x8D || b == (byte)0x8F || b == (byte)0x90 || b == (byte)0x9D) {
				// non e' cp1252
				excludeCp1252 = true;
			}
			
			if (b >= B10000000 && b <= B10111111) {
				latin1GrayArea = true;
			}
			isUtf8Sequence = false;
			validUtf8Sequence = false;
			if (b >= B11000000 && b <= B11011111) {
				// utf8: U+00000080 - U+000007FF
				isUtf8Sequence = true;
				validUtf8Sequence = i < blob.length - 1
									&& blob[i+1] >= B10000000 && blob[i+1] <= B10111111;
				if (validUtf8Sequence) i+=1;
			} else if (b >= B11100000 && b <= B11101111) {
				// utf8: U+00000800 - U+0000FFFF
				isUtf8Sequence = true;
				validUtf8Sequence = i < blob.length - 2
									&& blob[i+1] >= B10000000 && blob[i+1] <= B10111111
									&& blob[i+2] >= B10000000 && blob[i+2] <= B10111111;
				if (validUtf8Sequence) i+=2;
			} else if (b >= B11110000 && b <= B11110111) {
				// utf-8: U+00010000 - U+001FFFFF
				isUtf8Sequence = true;
				validUtf8Sequence = i < blob.length - 3
									&& blob[i+1] >= B10000000 && blob[i+1] <= B10111111
									&& blob[i+2] >= B10000000 && blob[i+2] <= B10111111
									&& blob[i+3] >= B10000000 && blob[i+3] <= B10111111;
				if (validUtf8Sequence) i+=3;
			} else if (b >= B11111000 && b <= B11111011) {
				// utf-8: U+00200000 - U+03FFFFFF
				isUtf8Sequence = true;
				validUtf8Sequence = i < blob.length - 4
									&& blob[i+1] >= B10000000 && blob[i+1] <= B10111111
									&& blob[i+2] >= B10000000 && blob[i+2] <= B10111111
									&& blob[i+3] >= B10000000 && blob[i+3] <= B10111111
									&& blob[i+4] >= B10000000 && blob[i+4] <= B10111111;
				if (validUtf8Sequence) i+=4;
			} else if (b == B11111100 || b == B11111101) {
				// utf-8: U+04000000 - U+7FFFFFFF
				isUtf8Sequence = true;
				validUtf8Sequence = i < blob.length - 5
									&& blob[i+1] >= B10000000 && blob[i+1] <= B10111111
									&& blob[i+2] >= B10000000 && blob[i+2] <= B10111111
									&& blob[i+3] >= B10000000 && blob[i+3] <= B10111111
									&& blob[i+4] >= B10000000 && blob[i+4] <= B10111111
									&& blob[i+5] >= B10000000 && blob[i+5] <= B10111111;
				if (validUtf8Sequence) i+=5;
			}
			if (excludeCp1252 && excludeLatin1) {
				if (isUtf8Sequence && !validUtf8Sequence) {
					return null;
				}
			}
			containsUtf8Sequences = containsUtf8Sequences || (isUtf8Sequence && validUtf8Sequence);
		}
		
		if (excludeCp1252 && !containsUtf8Sequences) {
			return "latin1";
		}
		if (excludeLatin1 && !containsUtf8Sequences) {
			return "cp1252";
		}
		if (!excludeLatin1 && !excludeCp1252 && latin1GrayArea && !containsUtf8Sequences) {
			return "latin1";
		}
		if (!excludeLatin1 && !excludeCp1252 && !latin1GrayArea && !containsUtf8Sequences) {
			return "ascii";
		}
		return "UTF-8";
	}
	
	private static byte B11000000 = fromBin("11000000");
	private static byte B11011111 = fromBin("11011111");
	
	private static byte B11100000 = fromBin("11100000");
	private static byte B11101111 = fromBin("11101111");
	
	private static byte B11110000 = fromBin("11110000");
	private static byte B11110111 = fromBin("11110111");
	
	private static byte B11111000 = fromBin("11111000");
	private static byte B11111011 = fromBin("11111011");
	
	private static byte B11111100 = fromBin("11111100");
	private static byte B11111101 = fromBin("11111101");
	
	private static byte B10000000 = fromBin("10000000");
	private static byte B10111111 = fromBin("10111111");

	public static byte fromBin(String bin) {
		return (byte) Integer.parseInt(bin, 2);
	}
}
