package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import me.LogosAcUmbra.Matrix.SymMatrixBuffer;
import me.LogosAcUmbra.Matrix.SymMatrixBufferPool;
import org.jspecify.annotations.NonNull;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static me.LogosAcUmbra.Matrix.SymMatrixParallelRouter.*;

import static me.LogosAcUmbra.Matrix.SymMatrixExpr.ISymMatrixAdvancedExpr.ensureIsAdvanced;


public class ScaleExpr implements ISymMatrixExpr, IOneOperandExpr {

    private final int numRows;
    private final int numCols;
    private @NonNull ISymMatrixAdvancedExpr operand;
    private final @NonNull IExpr scalar;

    private ScaleExpr(@NonNull ISymMatrixAdvancedExpr operand, @NonNull IExpr scalar) {
        this.numRows = operand.getNumRows();
        this.numCols = operand.getNumCols();
        this.operand = operand;
        this.scalar = scalar;
    }

    /**
     * create a ScaleExpr instance of the given operand with scalar = {@link F#C1}
     * @param operand the operand, recommended not to be Self ({@link ScaleExpr})
     * @return the resultant instance
     */
    public static ScaleExpr of(@NonNull ISymMatrixExpr operand) {
        ISymMatrixAdvancedExpr advOp = ensureIsAdvanced(operand);
        assert !(operand instanceof ScaleExpr);
        return new ScaleExpr(advOp, F.C1);
    }

    /**
     * create a ScaleExpr instance of the given operand with the given scalar
     * @param operand the operand, recommended not to be Self ({@link ScaleExpr})
     * @return the resultant instance
     */
    public static ScaleExpr of(@NonNull ISymMatrixExpr operand, IExpr scalar) {
        ISymMatrixAdvancedExpr advOp = ensureIsAdvanced(operand);
        assert !(operand instanceof ScaleExpr);
        return new ScaleExpr(advOp, scalar);
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
    public @NonNull List<ISymMatrixExpr> getOperands() {
        return List.of(operand);
    }

    @Override
    public @NonNull ISymMatrixAdvancedExpr getOperandRef() {
        return operand;
    }

    public @NonNull IExpr getScalar() {
        return scalar;
    }

    @Override
    public @NonNull ScaleExpr unsafeSetOperand(@NonNull ISymMatrixAdvancedExpr operand) {
        this.operand = operand;
        return this;
    }

    @Override
    public @NonNull ISymMatrixExpr scale(@NonNull IExpr scalar) {
        return new ScaleExpr(  operand, F.Times(this.scalar, scalar)  );
    }

    @Override
    public void computeIntoBuffer(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool bufferPool) {
        ((ISymMatrixAdvancedExpr) operand).computeIntoBuffer(target, bufferPool);
        assert this.numRows == target.getNumRows() && this.numCols == target.getNumCols();
        assert target.hasAllElemsNonNull();
        computeIntoHelper(target);
    }

    @Override
    public @NonNull SymMatrixBuffer computeToBuffer(@NonNull SymMatrixBufferPool pool) {
        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }

    private static void ensureNotSelf(@NonNull ISymMatrixExpr operand) {
        if (operand instanceof ScaleExpr) {
            throw new IllegalArgumentException(
                    "Strange Wrapping: A ScaleExpr instance should not have operand(" + operand + ") in type of Self (ScaleExpr)."
            );
        }
    }





    private void computeIntoHelper(@NonNull SymMatrixBuffer target) {
        assert this.numRows == target.getNumRows() && this.numCols == target.getNumCols();
        assert target.hasAllElemsNonNull();
        if (target.isEmpty() || target.isZero()) {
            return;
        }
        if (target.isContAnyMaj()) {
            computeIntoHelperContiguous(target);
            return;
        }
        if (target.getColStride() == 1) {
            computeIntoHelperRowMaj(target);
            return;
        }
        if (target.getRowStride() == 1) {
            computeIntoHelperColMaj(target);
            return;
        }
        computeIntoHelperFragmented(target);
    }

    private void computeIntoHelperContiguous(@NonNull SymMatrixBuffer target) {
        assert this.numRows == target.getNumRows() && this.numCols == target.getNumCols();
        assert !target.isEmpty() && !target.isZero();
        assert target.hasAllElemsNonNull();
        assert target.isContAnyMaj();

        final int numElems = numRows * numCols;

        int numCores = calSuitableNumCoresCont(
                Runtime.getRuntime().availableProcessors(),
                numElems
        );
        if (numCores == 1) {
            computeIntoHelperContiguousSequential(target);
            return;
        }

        int numElemsPerCore = (numElems + numCores - 1) / numCores; // numElems ceilDiv numCores
        int numElemsLastCore = numElems - (numCores - 1) * numElemsPerCore;
        try (  ExecutorService executor = Executors.newFixedThreadPool(numCores)  ) {
            int begin = target.getOffset();
            int end;
            for (int i = 0; i < numCores - 1; ++i) {
                end = begin + numElemsPerCore;

                final int finalBegin = begin;
                final int finalEnd = end;
                executor.submit( () -> {
                    mapScaleToArr(target.unsafeGetRaw(), finalBegin, finalEnd);
                });

                begin = end;
            }
            final int finalBegin = begin;
            executor.submit( () -> mapScaleToArr(target.unsafeGetRaw(), finalBegin, finalBegin + numElemsLastCore));
        }
    }

    private void computeIntoHelperRowMaj(@NonNull SymMatrixBuffer target) {
        assert this.numRows == target.getNumRows() && this.numCols == target.getNumCols();
        assert !target.isEmpty() && !target.isZero();
        assert target.hasAllElemsNonNull();
        assert target.getColStride() == 1;

        int numCores = calSuitableNumCoresRowMaj(
                Runtime.getRuntime().availableProcessors(),
                numRows, numCols
        );
        if (numCores == 1) {
            computeIntoHelperRowMajSequential(target);
            return;
        }

        rowMajHelper(target, numCores, numRows, numCols, target.getRowStride());
    }

    private void computeIntoHelperColMaj(@NonNull SymMatrixBuffer target) {
        assert this.numRows == target.getNumRows() && this.numCols == target.getNumCols();
        assert !target.isEmpty() && !target.isZero();
        assert target.hasAllElemsNonNull();
        assert target.getRowStride() == 1;

        int numCores = calSuitableNumCoresColMaj(
                Runtime.getRuntime().availableProcessors(),
                numRows, numCols
        );
        if (numCores == 1) {
            computeIntoHelperColMajSequential(target);
            return;
        }

        // reuse rowMaj logic with numRows and numCols, rowStride and colStride swapped
        rowMajHelper(target, numCores, numCols, numRows, target.getColStride());
    }

    private void computeIntoHelperFragmented(@NonNull SymMatrixBuffer target) {
        assert this.numRows == target.getNumRows() && this.numCols == target.getNumCols();
        assert !target.isEmpty() && !target.isZero();
        assert target.hasAllElemsNonNull();

        int numElems = numRows * numCols;
        int numCores = calSuitableNumCoresFragmented(
                Runtime.getRuntime().availableProcessors(),
                numElems
        );
        if (numCores == 1) {
            computeIntoHelperFragmentedSequential(target);
            return;
        }
        int numElemsPerCore = (numElems + numCores - 1) / numCores;
        int numElemsLastCore = numElems - numElemsPerCore * (numCores - 1);

        try (  ExecutorService executor = Executors.newFixedThreadPool(numCores)  ) {
            int begin = 0;
            int nextBegin;
            for (int i = 0; i < numCores - 1; ++i) {
                nextBegin = begin + numElemsPerCore;

                final int beginR = begin / numCols;
                final int beginC = begin % numCols;
                executor.submit( () -> fragmentedHelperEachCore(target, beginR, beginC, numElemsPerCore) );
                begin = nextBegin;
            }
            final int beginR = begin / numCols;
            final int beginC = begin % numCols;
            executor.submit( () -> fragmentedHelperEachCore(target, beginR, beginC, numElemsLastCore) );
        }
    }

    private void computeIntoHelperContiguousSequential(@NonNull SymMatrixBuffer target) {
        assert this.numRows == target.getNumRows() && this.numCols == target.getNumCols();
        assert !target.isEmpty() && !target.isZero();
        assert target.hasAllElemsNonNull();
        assert target.isContAnyMaj();

        mapScaleToArr(target.unsafeGetRaw(), target.getOffset(), target.getOffset() + target.getNumRows() * target.getNumCols());
    }
    private void computeIntoHelperRowMajSequential(@NonNull SymMatrixBuffer target) {
        assert this.numRows == target.getNumRows() && this.numCols == target.getNumCols();
        assert !target.isEmpty() && !target.isZero();
        assert target.hasAllElemsNonNull();
        assert target.getColStride() == 1;

        for (int r = 0; r < numRows; ++r) {
            int rowRawIdx = target.getOffset() + r * target.getRowStride();
            mapScaleToArr(target.unsafeGetRaw(), rowRawIdx, rowRawIdx + target.getNumCols());
        }
    }
    private void computeIntoHelperColMajSequential(@NonNull SymMatrixBuffer target) {
        assert this.numRows == target.getNumRows() && this.numCols == target.getNumCols();
        assert !target.isEmpty() && !target.isZero();
        assert target.hasAllElemsNonNull();
        assert target.getRowStride() == 1;

        for (int c = 0; c < numCols; ++c) {
            int colRawIdx = target.getOffset() + c * target.getColStride();
            mapScaleToArr(target.unsafeGetRaw(), colRawIdx, colRawIdx + numRows);
        }
    }
    private void computeIntoHelperFragmentedSequential(@NonNull SymMatrixBuffer target) {
        assert this.numRows == target.getNumRows() && this.numCols == target.getNumCols();
        assert !target.isEmpty() && !target.isZero();
        assert target.hasAllElemsNonNull();

        fragmentedHelperEachCore(target, 0, 0, numRows * numCols);
    }

    private void rowMajHelper(@NonNull SymMatrixBuffer target, int numCores, int numRows, int numCols, int rowStride) {

        int numRowsPerCore = (numRows + numCores - 1) / numCores;
        int numRowsLastCore = numRows - numRowsPerCore * (numCores - 1);
        try (  ExecutorService executor = Executors.newFixedThreadPool(numCores)  ) {
            int beginRow = 0;
            int nextBeginRow;
            for (int i = 0; i < numCores - 1; ++i) {
                nextBeginRow = beginRow + numRowsPerCore;

                final int finalBeginRow = beginRow;
                final int finalEndRow = nextBeginRow;
                executor.submit( () -> mapScaleToRows(target.unsafeGetRaw(), target.getOffset(), finalBeginRow, finalEndRow, numCols, rowStride) );

                beginRow = nextBeginRow;
            }
            final int finalBeginRow = beginRow;
            executor.submit( () -> mapScaleToRows(target.unsafeGetRaw(), target.getOffset(), finalBeginRow, finalBeginRow + numRowsLastCore, numCols, rowStride) );
        }
    }

    private void fragmentedHelperEachCore(@NonNull SymMatrixBuffer target, int beginR, int beginC, int numElemsOfCore) {
        // cache
        int rowStride = target.getRowStride(), colStride = target.getColStride();
        IExpr [] raw = target.unsafeGetRaw();

        int rowRawIdx = target.getOffset() + beginR * rowStride;
        int colRawOffset = beginC * colStride;
        int colRawOffsetEnd = numCols * colStride;
        for (int j = 0; j < numElemsOfCore; ++j) {
            int idx = rowRawIdx + colRawOffset;
            raw[idx] = F.Times(  raw[idx], scalar  );
            colRawOffset += colStride;
            if (colRawOffset == colRawOffsetEnd) { // prevent divide operations
                colRawOffset = 0;
                rowRawIdx += rowStride;
            }
        }
    }

    private void mapScaleToRows(
            IExpr @NonNull [] arr,
            int offset, int beginRow, int endRow,
            int numElemsPerRow, int rowStride
    ) {
        for (int i = offset + beginRow * rowStride; i != offset + endRow * rowStride; i += rowStride) {
            mapScaleToArrReversible(arr, i, i + numElemsPerRow);
        }
    }
    /**
     * scale each element in range [beginIdx, endIdx) of the given array
     * @param arr the array (requires non-null for all elements in the given range)
     * @param beginIdx the start-from-index, included
     * @param endIdx the to-index, not included
     */
    private void mapScaleToArr(
            IExpr @NonNull [] arr,
            int beginIdx, int endIdx
    ) {
        for (int i = beginIdx; i != endIdx; ++i) {
            arr[i] = F.Times(  arr[i], this.scalar  );
        }
    }
    /**
     * scale each element in range [beginIdx, endIdx) of the given array <br>
     * endIdx can be > beginIdx
     * @param arr the array (requires non-null for all elements in the given range)
     * @param beginIdx the start-from-index, included
     * @param endIdx the to-index, not included
     */
    private void mapScaleToArrReversible(
            IExpr @NonNull [] arr,
            int beginIdx, int endIdx
    ) {
        int increment = (beginIdx < endIdx) ? 1 : -1;
        for (int i = beginIdx; i != endIdx; i += increment) {
            arr[i] = F.Times(  arr[i], this.scalar  );
        }
    }


}
