/******************************************************************************
 * Author: William Schwartz
 * Written: 2012-08-25
 *
 * Compilation: $ javac Percolation.java
 * Testing: $ java Percolation
 *
 * Data type to model physical percolation (say of water through concrete).
 ******************************************************************************/

import java.lang.IndexOutOfBoundsException;


public class Percolation {

	private final int N; // Length of one side of the grid.
	private boolean[] open;
	private WeightedQuickUnionUF paths;
	private final int virtual_top;
	private final int virtual_bottom;

	// create N-by-N grid, with all sites blocked
	public Percolation(int N) throws IndexOutOfBoundsException {
		// The union-find data type we're using indexes its arrays with an int,
		// so our N^2 sized grid must have N^2 <= 2^32 - 1 <=> N > 2^16.
		if (N >= 0x10000)
			throw new IndexOutOfBoundsException("Dimension must be < 2^16");
		this.N = N;
		open = new boolean[N*N];
		// Add two for the virtual top and bottom
		paths = new WeightedQuickUnionUF(N*N + 2);
		virtual_top = indexOf(N, N) + 1;
		virtual_bottom = indexOf(N, N) + 2;
	}

	// is site (row i, column j) open?
	public boolean isOpen(int i, int j) {
		return open[indexOf(i, j)];
	}

	// is site (row i, column j) full?
	public boolean isFull(int i, int j) {
		return !open[indexOf(i, j)];
	}

	// does the system percolate?
	public boolean percolates() {
		return paths.connected(virtual_top, virtual_bottom);
	}

	// open site (row i, column j) if it is not already
	public void open(int i, int j) {
		if (isOpen(i, j))
			return;
		int index = indexOf(i, j);

		open[index] = true;
		if (i == 1) {
			paths.union(virtual_top, index);
		}
		else if (i == N) {
			paths.union(virtual_bottom, index);
		}

		if (i < N  && isOpen(i + 1, j)) {
			paths.union(indexOf(i + 1, j), index);
		}
		else if (i > 1 && isOpen(i - 1, j)) {
			paths.union(indexOf(i - 1, j), index);
		}

		if (j < N && isOpen(i, j + 1)) {
			paths.union(indexOf(i, j + 1), index);
		}
		else if (j > 1 && isOpen(i, j - 1)) {
			paths.union(indexOf(i, j - 1), index);
		}
	}

	/* Convert grid coordinates of the form (x, y) where x,y in {1,...,N}
	to an array index. E.g., indexOf(1,1) == 0; indexOf(N, N) = N^2 - 1.

	Assume the grid is in row-major form.
	*/
	private int indexOf(int row, int col) throws IndexOutOfBoundsException {
		if (row <= 0 || row > N || col <= 0 || col > N)
			throw new IndexOutOfBoundsException(
					"(" + row + ", " + col + ") out of bounds " +
					"for " + N + "^2 grid.");
		return (row - 1) * N + (col - 1);
	}

	private static boolean test(int N, int[][] open_sites, boolean expectation) {
		boolean result;
		Percolation tested = new Percolation(N);
		for (int[] open_site: open_sites)
			tested.open(open_site[0], open_site[1]);
		result = tested.percolates();
		if (!result && expectation) {
			System.err.println("Unexpected failure");
			return false;
		}
		else if (result && !expectation) {
			System.err.println("Unexpected success");
			return false;
		}
		return true;
	}

	public static void main(String[] argv) {
		int passes = 0;
		int total = 0;
		int[][] works = {{1, 1},		 {1, 3},
						 {2, 1}, {2, 2},		 {2, 4},
						 		 {3, 2}, {3, 3},
						 {4, 1}, 		 {4, 3},};
		total++;
		if (test(4, works, true))
			passes++;

		int[][] bad = {{1, 1},		 {1, 3},
						{2, 1}, {2, 2},			{2, 4},
								{3, 2},
						{4, 1}, 		 {4, 3},};
		total++;
		if (test(4, bad, false))
			passes++;
		System.err.println("Tests: " + passes + "/" + total);
	}
}
