package me.LogosAcUmbra.Matrix;
import org.jspecify.annotations.NonNull;
import org.matheclipse.core.expression.F;

import java.util.Arrays;
import java.util.List;

public class ZeroExpr implements ISymMatrixExpr {
    final int numRows;
    final int numCols;

    private ZeroExpr(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public static @NonNull ZeroExpr ofSize(int numRows, int numCols) {
        return new ZeroExpr(numRows, numCols);
    }

    @Override
    public int getNumRows() {
        return numRows;
    }

    @Override
    public int getNumCols() {
        return numCols;
    }

    @Override
    public @NonNull List<ISymMatrixExpr> getOperands() {
        return List.of();
    }

    @Override
    public void computeInto(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool pool) {
        assert this.numRows == target.getNumRows() && this.numCols == target.getNumCols();
        if (target.isContAnyMaj()) {
            Arrays.fill(target.raw, target.offset, target.offset + numRows * numCols, F.C0);
            return;
        }
        if (target.isRowMaj()) {
            computeIntoRowMaj(target);
            return;
        }
        if (target.isColMaj()) {
            computeIntoColMaj(target);
            return;
        }
        for (int r = 0; r < numRows; ++r) {
            int rowRawIdx = target.offset + r * target.rowStride;
            for (int c = 0; c < numCols; ++c) {
                target.raw[rowRawIdx + c * target.colStride] = F.C0;
            }
        }
    }

    private void computeIntoRowMaj(@NonNull SymMatrixBuffer target) {
        computeIntoRowMajHelper(target, numRows, numCols, target.rowStride);
    }

    private void computeIntoColMaj(@NonNull SymMatrixBuffer target) {
        computeIntoRowMajHelper(target, numCols, numRows, target.colStride);
    }

    private void computeIntoRowMajHelper(@NonNull SymMatrixBuffer target, int numRows, int numCols, int rowStride) {
        for (int r = 0; r < numRows; ++r) {
            int rowRawIdx = target.offset + r * rowStride;
            for (int c = 0; c < numCols; ++c) {
                target.raw[rowRawIdx + c] = F.C0;
            }
        }
    }
}
