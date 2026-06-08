package me.LogosAcUmbra.Matrix;

import org.jspecify.annotations.NonNull;

public class SymMatrixParallelRouter {

    public static final int MIN_ELEMS_PER_THREAD = 64;
    public static final int MIN_ROWS_PER_THREAD = 1;
    /**
     * use when need to stride when navigate through elem
     */
    public static final int MIN_STRIDE_ELEMS_PER_THREAD = 16;
    public static final int MIN_FRAGMENTED_ELEMS_PER_THREAD = 8;

    public static final int MIN_ACTIVE_CORES = 3;

    public static int calSuitableNumCoresCont(int numHardwareCores, int numElems) {
        assert numElems > 0;

        if (numHardwareCores < MIN_ACTIVE_CORES) {  return 1;  }

        int numThreadsOfMinWork = (numElems + MIN_ELEMS_PER_THREAD - 1) / MIN_ELEMS_PER_THREAD;
        int numActiveCores = Math.min(numThreadsOfMinWork, numHardwareCores);
        if (numActiveCores < MIN_ACTIVE_CORES) {  return 1;  }
        return numActiveCores;
    }
    public static int calSuitableNumCoresRowMaj(int numHardwareCores, int numRows, int numCols) {
        assert numRows > 0 && numCols > 0;

        if (numHardwareCores < MIN_ACTIVE_CORES) {  return 1;  }

        // find minimum rows per thread that fulfill both constants MIN_ROWS_PER_THREAD and MIN_STRIDED_ELEMS_PER_THREAD
        int minRowsPerThread = Math.max(MIN_ROWS_PER_THREAD, (MIN_STRIDE_ELEMS_PER_THREAD + numCols - 1) / numCols);

        int numThreadsOfMinRow = (numRows + minRowsPerThread - 1) / minRowsPerThread;
        if (numThreadsOfMinRow < numHardwareCores) {
            if (numThreadsOfMinRow < MIN_ACTIVE_CORES) {
                return 1;
            }
            return numThreadsOfMinRow;
        }
        return numHardwareCores;
    }

    public static int calSuitableNumCoresColMaj(int numHardwareCores, int numRows, int numCols) {
        assert numRows > 0 && numCols > 0;
        return calSuitableNumCoresRowMaj(numHardwareCores, numCols, numRows);
    }

    public static int calSuitableNumCoresFragmented(int numHardwareCores, int numElems) {

        if (numHardwareCores < MIN_ACTIVE_CORES) {  return 1;  }

        // since fragmented, all elements can be treated the same way
        int numThreadsOfMinWork = ( numElems + MIN_FRAGMENTED_ELEMS_PER_THREAD - 1 ) / MIN_FRAGMENTED_ELEMS_PER_THREAD;
        if (numThreadsOfMinWork < numHardwareCores) {
            if (numThreadsOfMinWork < MIN_ACTIVE_CORES) {  return 1;  }

            return numThreadsOfMinWork;
        }
        return numHardwareCores;

    }
}
