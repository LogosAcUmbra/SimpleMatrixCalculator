package me.LogosAcUmbra.Matrix;

import org.jspecify.annotations.NonNull;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScaleExpr implements ISymMatrixExpr {

    final int numRows;
    final int numCols;
    final @NonNull ISymMatrixExpr operand;
    @NonNull IExpr scalar;

    ScaleExpr(@NonNull ISymMatrixExpr operand, @NonNull IExpr scalar) {
        this.numRows = operand.getNumRows();
        this.numCols = operand.getNumCols();
        this.operand = operand;
        this.scalar = scalar;
    }

    static ScaleExpr of(ISymMatrixExpr expr) {
        assert !(expr instanceof ScaleExpr);
        return new ScaleExpr(expr, F.C1);
    }

    static ScaleExpr of(ISymMatrixExpr expr, IExpr scalar) {
        assert !(expr instanceof ScaleExpr);
        return new ScaleExpr(expr, scalar);
    }

    @Override
    public int getNumRows() {
        return 0;
    }

    @Override
    public int getNumCols() {
        return 0;
    }

    @Override
    public @NonNull ISymMatrixExpr plus(@NonNull ISymMatrixExpr expr) {
        return SumExpr.of(this, expr);
    }

    @Override
    public @NonNull ISymMatrixExpr minus(@NonNull ISymMatrixExpr expr) {
        return SumExpr.ofMinus(this, expr);
    }

    @Override
    public @NonNull ISymMatrixExpr scale(@NonNull IExpr scalar) {
        this.scalar = F.Times(this.scalar, scalar);
        return this;
    }

    @Override
    public @NonNull ISymMatrixExpr times(@NonNull ISymMatrixExpr expr) {
        return MulExpr.of(this, expr);
    }

    @Override
    public void evalInto(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool bufferPool) {
        operand.evalInto(target, bufferPool);
        evalIntoHelperSequential(target);
    }

    @Override
    public @NonNull List<ISymMatrixExpr> getOperands() {
        return List.of(operand);
    }

    private void evalIntoHelperSequential(@NonNull SymMatrixBuffer target) {
        if (target.rowStride == 1) { // colMaj
            for (int c = 0; c < numCols; ++c) {
                int colIdx = c * target.colStride;
                for (int r = 0; r < numRows; ++r) {
                    int finalIdx = colIdx + r;
                    target.raw[finalIdx] = F.Times(  target.raw[finalIdx], this.scalar  );
                }
            }
            return;
        }
        // rowMaj or others
        for (int r = 0; r < numRows; ++r) {
            int rowIdx = r * target.rowStride;
            for (int c = 0; c < numCols; ++c) {
                int finalIdx = rowIdx + c * target.colStride;
                target.raw[finalIdx] = F.Times(  target.raw[finalIdx], this.scalar  );
            }
        }
    }

    private void evalIntoHelperParallel(@NonNull SymMatrixBuffer target) {
        int numCores = Runtime.getRuntime().availableProcessors();
        if (target.colStride == 1) {
            int numThreads = Math.min(numCores, numRows);
            if (numThreads <= 1) {
                evalIntoHelperSequential(target);
                return;
            }
            int rowsPerChunk = (target.numRows + numThreads - 1) / numThreads; // target.numRows ceilDiv numThreads
            try (  ExecutorService executor = Executors.newFixedThreadPool(numThreads)  ) {
                int offset = target.offset;
                for (int i = 0; i < numThreads-1; ++i) {
                    int finalOffset = offset;
                    executor.submit( () -> {
                        // get
                        IExpr[] localRows = new IExpr[rowsPerChunk * numCols];
                        for (int r = 0; r < rowsPerChunk; ++r) {
                            int rowLocalRawIdx = r * target.rowStride;
                            System.arraycopy(target.raw, finalOffset * rowLocalRawIdx, localRows, rowLocalRawIdx, numCols);
                        }
                        // calc
                        for (int r = 0; r < rowsPerChunk; ++r) {
                            int rowRawIdx = r * target.rowStride;
                            for (int c = 0; c < numCols; ++c) {

                            }
                        }
                    } );

                    offset += rowsPerChunk;
                }
            }
        }

        if (target.colStride == 1) { // rowMaj

            for (int r = 0; r < numRows; ++r) {
                int rowRawIdx = r * target.rowStride;
                executor.submit(() -> {
                    // get
                    IExpr[] localRow = new IExpr[numCols];
                    System.arraycopy(target.raw, rowRawIdx, localRow, 0, numCols);
                    // calc
                    for (int c = 0; c < numCols; ++c) {
                        localRow[c] = F.Times(  localRow[c], this.scalar  );
                    }
                    // commit
                    System.arraycopy(localRow, 0, target.raw, rowRawIdx, numCols);
                });
            }
        }
        if (target.rowStride == 1) { // colMaj
            for (int c = 0; c < numCols; ++c) {
                int colIdx = c * target.colStride;
                executor.submit(() -> {
                    // get
                    IExpr[] localCol = new IExpr[numRows];
                    System.arraycopy(target.raw, colIdx, localCol, 0, numRows);
                    // calc
                    for (int r = 0; r < numRows; ++r) {
                        localCol[r] = F.Times(  localCol[r], this.scalar  );
                    }
                    // commit
                    System.arraycopy(localCol, 0, target.raw, colIdx, numRows);
                });
            }
            // executor.close(); return;

        } else { // not rowMaj or colMaj, still r-c loop order
            for (int r = 0; r < numRows; ++r) {
                int rowIdx = r * target.rowStride;
                executor.submit(() -> {
                    // how to do?
                });
            }
            // executor.close(); return;
        }

    }
    private void evalIntoHelperParallelContiguous(@NonNull SymMatrixBuffer target) {
        assert (target.colStride == 1 && target.rowStride == target.numCols
                || target.rowStride == 1 && target.colStride == target.numRows
        );
        // assert, all elements in target.raw are NonNull

        int numCores = Runtime.getRuntime().availableProcessors();
        int size = target.numRows * target.numCols;
        int numElemsPerCore = (size + numCores - 1) / numCores; // size ceilDiv numCores
        int numElemsLastCore = size - (numCores - 1) * numElemsPerCore;
        try (  ExecutorService executor = Executors.newFixedThreadPool(numCores)  ) {
            int offset = target.offset;
            for (int i = 0; i < numCores - 1; ++i) {
                final int finalOffset = offset;
                executor.submit( () -> {
                    mapScaleTo(target.raw, finalOffset, finalOffset + numElemsPerCore);
                });

                offset += numElemsPerCore;
            }
            final int finalOffset = offset;
            executor.submit( () -> mapScaleTo(target.raw, finalOffset, finalOffset + numElemsLastCore));
        }
    }

    /**
     * scale each element in range [beginIdx, endIdx) of the given array
     * @param arr the array
     * @param beginIdx the start-from-index, included
     * @param endIdx the to-index, not included
     */
    private void mapScaleTo(
            @NonNull IExpr @NonNull [] arr,
            int beginIdx, int endIdx
    ) {
        for (int i = beginIdx; i < endIdx; ++i) {
            arr[i] = F.Times(  arr[i], this.scalar  );
        }
    }
}
