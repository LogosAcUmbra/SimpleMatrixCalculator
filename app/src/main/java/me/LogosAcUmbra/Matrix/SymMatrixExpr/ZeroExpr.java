package me.LogosAcUmbra.Matrix.SymMatrixExpr;
import me.LogosAcUmbra.Matrix.SymMatrixBuffer;
import me.LogosAcUmbra.Matrix.SymMatrixBufferPool;
import org.jspecify.annotations.NonNull;
import org.matheclipse.core.expression.F;

import java.util.Arrays;
import java.util.List;

public class ZeroExpr implements ConstOperandExpr {
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
    public boolean isZero() {
        return true;
    }

    @Override
    public boolean isIdentity() {
        return false;
    }

    @Override
    public @NonNull List<SymMatrixExpr> getOperands() {
        return List.of();
    }

    @Override
    public void computeIntoBuffer(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool pool) {
        assert this.numRows == target.getNumRows() && this.numCols == target.getNumCols();
        if (target.isContAnyMaj()) {
            Arrays.fill(target.unsafeGetRaw(), target.getOffset(), target.getOffset() + numRows * numCols, F.C0);
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
            int rowRawIdx = target.getOffset() + r * target.getRowStride();
            for (int c = 0; c < numCols; ++c) {
                target.unsafeGetRaw()[rowRawIdx + c * target.getColStride()] = F.C0;
            }
        }
    }

    private void computeIntoRowMaj(@NonNull SymMatrixBuffer target) {
        computeIntoRowMajHelper(target, numRows, numCols, target.getRowStride());
    }

    private void computeIntoColMaj(@NonNull SymMatrixBuffer target) {
        computeIntoRowMajHelper(target, numCols, numRows, target.getColStride());
    }

    private void computeIntoRowMajHelper(@NonNull SymMatrixBuffer target, int numRows, int numCols, int rowStride) {
        for (int r = 0; r < numRows; ++r) {
            int rowRawIdx = target.getOffset() + r * rowStride;
            for (int c = 0; c < numCols; ++c) {
                target.unsafeGetRaw()[rowRawIdx + c] = F.C0;
            }
        }
    }
}
