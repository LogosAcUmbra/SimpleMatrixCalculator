package me.LogosAcUmbra.Matrix;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.matheclipse.core.interfaces.IExpr;

public class SymMatrixBuffer implements ISymMatrix {
    final @Nullable IExpr @NonNull [] raw;
    int numRows, numCols;
    int offset, rowStride, colStride;

    SymMatrixBuffer(
            @Nullable IExpr @NonNull [] raw, int numRows, int numCols, int offset, int rowStride, int colStride
    ) {
        this.raw = raw;
        this.numRows = numRows;
        this.numCols = numCols;
        this.offset = offset;
        this.rowStride = rowStride;
        this.colStride = colStride;
    }

    protected static SymMatrixBuffer unsafeOfRowMaj
            (@Nullable IExpr @NonNull [] mat, int numRows, int numCols) {
        return new SymMatrixBuffer(mat, numRows, numCols, 0, numCols, 1);
    }
    protected static SymMatrixBuffer unsafeOfColMaj(
            @Nullable IExpr @NonNull [] mat, int numRows, int numCols) {
        return new SymMatrixBuffer(mat, numRows, numCols, 0, 1, numRows);
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
    public int getOffset() {
        return 0;
    }

    @Override
    public int getRowStride() {
        return rowStride;
    }

    @Override
    public int getColStride() {
        return colStride;
    }

    public SymMatrixBuffer setFields(
            int numRows, int numCols,
            int offset, int rowStride, int colStride) {
        assert numRows * numCols <= this.raw.length;
        this.numRows = numRows;
        this.numCols = numCols;
        this.offset = offset;
        this.rowStride = rowStride;
        this.colStride = colStride;
        return this;
    }

}
