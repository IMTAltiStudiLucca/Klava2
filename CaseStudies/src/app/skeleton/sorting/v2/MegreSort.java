package app.skeleton.sorting.v2;

import java.util.Vector;

public class MegreSort {

	public static int[] reconstructArray(Vector<int[]> parts, int length) {
		int[] res = new int[length];
		int[] nextPart;
		int n = 0;
		do {
			nextPart = nextPartition(parts);
			for (int i = 0; i < nextPart.length; i++) {
				res[n] = nextPart[i];
				n++;
			}
		} while ((nextPart != null) && (n < length));

		return res;
	}

	public static int[] nextPartition(Vector<int[]> parts) {
		if (parts.isEmpty())
			return null;

		if (parts.size() == 1)
			return parts.elementAt(0);

		int low = 0;
		for (int i = 1; i < parts.size(); i++) {
			if (parts.elementAt(i)[0] < parts.elementAt(low)[0])
				low = i;
		}

		return parts.remove(low);
	}
}
