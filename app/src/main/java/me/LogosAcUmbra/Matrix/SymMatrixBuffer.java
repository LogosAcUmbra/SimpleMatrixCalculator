package me.LogosAcUmbra.Matrix;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;

import java.util.Arrays;

/**
 * don't deal with negative strides for now
 */
public class SymMatrixBuffer implements ISymMatrix {
    final @Nullable IExpr @NonNull [] raw;
    int numRows, numCols;
    int offset, rowStride, colStride;
    boolean isZero, isEmpty;

    protected final static IExpr[] EMPTY_ARR = new IExpr[0];

    SymMatrixBuffer(
            @Nullable IExpr @NonNull [] raw, int numRows, int numCols,
            int offset, int rowStride, int colStride,
            boolean isZero,
            boolean isEmpty
    ) {
        this.raw = raw;
        this.numRows = numRows;
        this.numCols = numCols;
        this.offset = offset;
        this.rowStride = rowStride;
        this.colStride = colStride;
        this.isZero = isZero;
        this.isEmpty = isEmpty;
    }

    public static SymMatrixBuffer empty() {
        return emptyOfSize(0, 0);
    }

    public static SymMatrixBuffer emptyOfSize(int numRows, int numCols) {
        return new SymMatrixBuffer(
                EMPTY_ARR, numRows, numCols,
                0, 0, 0, true, true
        );
    }

    public static SymMatrixBuffer of(int numRows, int numCols) {
        int size = numRows * numCols;
        if (size == 0) {  return emptyOfSize(numRows, numCols);  }
        return new SymMatrixBuffer(
                new IExpr[ size ], numRows, numCols,
                0, numCols, 1, false, false);
    }

    public static SymMatrixBuffer of(int numRows, int numCols, @NonNull IExpr val) {
        int size = numRows * numCols;
        if (size == 0) {  return emptyOfSize(numRows, numCols);  }
        IExpr[] arr = new IExpr[ size ];
        Arrays.fill(arr, val);
        return new SymMatrixBuffer(
                arr, numRows, numCols,
                0, numCols, 1, val.equals(F.C0), false);
    }

    protected static SymMatrixBuffer unsafeOfContRowMaj
            (@Nullable IExpr @NonNull [] mat, int numRows, int numCols) {
        return new SymMatrixBuffer(mat, numRows, numCols, 0, numCols, 1, false, false);
    }
    protected static SymMatrixBuffer unsafeOfContColMaj(
            @Nullable IExpr @NonNull [] mat, int numRows, int numCols) {
        return new SymMatrixBuffer(mat, numRows, numCols, 0, 1, numRows, false, false);
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

    @Override
    public boolean isZero() {
        return isZero;
    }

    @Override
    public boolean isEmpty() {
        return isEmpty;
    }

    public SymMatrixBuffer unsafeSetFields(
            int numRows, int numCols,
            int offset, int rowStride, int colStride,
            boolean isZero, boolean isEmpty) {
        assert numRows * numCols <= this.raw.length;
        this.numRows = numRows;
        this.numCols = numCols;
        this.offset = offset;
        this.rowStride = rowStride;
        this.colStride = colStride;
        this.isZero = isZero;
        this.isEmpty = isEmpty;
        return this;
    }

    public boolean isContiguous() {
        return (this.colStride == 1 && this.rowStride == this.numCols
                || this.rowStride == 1 && this.colStride == this.numRows
        );
    }

    public boolean isRowMaj() {
        return (this.colStride == 1);
    }

    public boolean isColMaj() {
        return (this.rowStride == 1);
    }

    public boolean hasAllElemsNonNull() {
        if (isEmpty || isZero) {
            return true;
        }
        for (int r = 0; r < numRows; r++) {
            int rowRawIdx = offset + r * rowStride;
            for (int c = 0; c < numCols; c++) {
                if (this.raw[rowRawIdx + c * colStride] == null) {
                    return false;
                }
            }
        }
        return true;
    }

}
