
package helpers.util;

public class BoyerMoore {
	
	private static byte[][] preprocessForBadCharacterShift(byte[] pattern) {
		byte[][] map = new byte[pattern.length][];
		int mapIdx = 0;
		for (int i=pattern.length-1;i>=0;--i) {
			byte b = pattern[i];
			for (int j=0;j<map.length;++j) {
				if (map[j] == null) break;
				if (map[j][0] == b) {
					b = -1;
					break;
				}
			}
			if (b == -1) continue;
			map[mapIdx++] = new byte[] {b, (byte) i};
		}
		return map;
	}
	
	private static byte lookup(byte[][] map, byte key) {
		for (int i=0;i<map.length;++i) {
			if (map[i] == null) break;
			if (key == map[i][0]) return map[i][1];
		}
		return -1;
	}
	
	private static int MAX_MATCHES = 1;
	
	public static int[] match (byte[] pattern, byte[] blob, boolean skipMoreMatches) {
		int[] matches = new int[MAX_MATCHES];
		int matchesIdx = 0;
		int m = blob.length;
		int n = pattern.length;
		byte[][] rightMostIndexes = preprocessForBadCharacterShift(pattern);
		int alignedAt = 0;
		while (alignedAt + (n - 1) < m) {
			for (int indexInPattern = n - 1; indexInPattern >= 0; indexInPattern--) {
				int indexInText = alignedAt + indexInPattern;
				byte x = blob[indexInText];
				byte y = pattern[indexInPattern];
				if (indexInText >= m)
					break;
				if (x != y) {
					byte r = lookup(rightMostIndexes, x);
					if (r == -1) {
						alignedAt = indexInText + 1;
					} else {
						int shift = indexInText - (alignedAt + r);
						alignedAt += shift > 0 ? shift : 1;
					}
					break;
				} else if (indexInPattern == 0) {
					if (matchesIdx == MAX_MATCHES) {
						if (skipMoreMatches) {
							return matches;
						} else {
							throw new RuntimeException("Too much matches in the blob");	
						}
					}
					matches[matchesIdx++] = alignedAt++;
				}
			}
		}
		if (matchesIdx == 0) return new int[0];
		int[] result = new int[matchesIdx];
		System.arraycopy(matches, 0, result, 0, matchesIdx);
		return result;
	}
}
