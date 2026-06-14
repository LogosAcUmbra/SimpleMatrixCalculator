package me.LogosAcUmbra.Matrix;

import me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrix;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;

import java.util.Arrays;

/**
 * UNSAFE: this class has many public unsafe-prefixed methods that will lead to illegal state and UB <br>
 * not thread safe <br>
 * does not support negative strides
 */
public class SymMatrixBuffer implements Matrix {
    private final @Nullable IExpr @NonNull [] raw;
    private int numRows, numCols;
    private int offset, rowStride, colStride;
    private boolean isZero, isEmpty;

    protected final static IExpr[] EMPTY_ARR = new IExpr[0];

    private SymMatrixBuffer(
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


    /**
     * create a zero SymMatrix instance with the given dimensions <br>
     * <br>
     * Zero Matrix Invariant: for {@link #SymMatrixBuffer}, any zero matrices strictly follows the following: <br>
     * offset = {@code 0}; isZero = {@code true}; isEmpty = {@code false}; <br>
     * IMPORTANT: UNLIKE {@link SymMatrix#zeroOfSize(int, int)},
     * a non~empty (numRows > 0 or numCols > 0) zero matrix should always
     * have its internal raw array a new dense array filled with {@link F#C0}
     * @param numRows the number of rows
     * @param numCols the number of columns
     * @return the resultant zero SymMatrix instance
     */
    public static SymMatrixBuffer zeroOfSize(int numRows, int numCols) {
        ensureNonNegDimension(numRows, numCols);
        if (isDimensionEmpty(numRows, numCols)) {
            return unsafeEmptyOfSize(0, 0);
        }
        return unsafeZeroOfSize(numRows, numCols);
    }

    /**
     * create an empty SymMatrix instance in dimension of 0x0 <br>
     * <br>
     * Empty Matrix Invariant: for {@link #SymMatrixBuffer},
     * (the invariant is the same as stated in {@link SymMatrix#empty()}) <br>
     * any empty matrices strictly follows the following: <br>
     * raw = {@link #EMPTY_ARR}; offset, rowStride and colStride = {@code 0}; isZero, isEmpty = {@code true};
     * @return the resultant empty SymMatrixBuffer instance
     */
    public static SymMatrixBuffer empty() {
        return unsafeEmptyOfSize(0, 0);
    }
    /**
     * create an empty SymMatrix instance with the given dimensions <br>
     * <br>
     * Empty Matrix Invariant: for {@link #SymMatrixBuffer},
     * (the invariant is the same as stated in {@link SymMatrix#emptyOfSize(int, int)}) <br>
     * any empty matrices strictly follows the following: <br>
     * @param numRows the number of rows
     * @param numCols the number of columns
     * @return the resultant empty SymMatrixBuffer instance
     */
    public static SymMatrixBuffer emptyOfSize(int numRows, int numCols) {
        ensureNonNegDimension(numRows, numCols);
        if (!isDimensionEmpty(numRows, numCols)) {
            throw new IllegalArgumentException( String.format(
                    "Illegal Dimensions: given numRows(%s) and numCols(%s) is invalid for constructing an empty matrix",
                    numRows, numCols
            ));
        }
        return unsafeEmptyOfSize(numRows, numCols);
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
        if (val.equals(F.C0)) {
            return unsafeZeroOfSize(numRows, numCols);
        }
        IExpr[] arr = new IExpr[ size ];
        Arrays.fill(arr, val);
        return new SymMatrixBuffer(
                arr, numRows, numCols,
                0, numCols, 1, false, false);
    }

    /**
     * @param capacity the size of the underlying array
     * @return a SymMatrixBuffer instance with array of the given capacity but all other fields set to 0
     */
    public static SymMatrixBuffer unsafeOfCapacity(int capacity) {
        return new SymMatrixBuffer(
                new IExpr[capacity],
                0, 0, 0, 0, 0, false, false
        );
    }

    public static SymMatrixBuffer unsafeOfContRowMaj
            (@Nullable IExpr @NonNull [] mat, int numRows, int numCols) {
        return new SymMatrixBuffer(mat, numRows, numCols, 0, numCols, 1, false, false);
    }
    public static SymMatrixBuffer unsafeOfContColMaj(
            @Nullable IExpr @NonNull [] mat, int numRows, int numCols) {
        return new SymMatrixBuffer(mat, numRows, numCols, 0, 1, numRows, false, false);
    }


    public static SymMatrixBuffer unsafeEmptyOfSize(int numRows, int numCols) {
        assert isDimensionEmpty(numRows, numCols);
        return new SymMatrixBuffer(EMPTY_ARR, numRows, numCols, 0, 0, 0, true, true  );
    }
    public static SymMatrixBuffer unsafeZeroOfSize(int numRows, int numCols) {
        assert !isDimensionEmpty(numRows, numCols);
        IExpr[] raw = new IExpr[numRows * numCols];
        Arrays.fill(raw, F.C0);
        return new SymMatrixBuffer(raw, numRows, numCols, 0, numCols, 1, true, false  );
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

    /**
     * UNSAFE:
     * @return the direct reference to the internal raw array
     */
    public @Nullable IExpr @NonNull [] unsafeGetRaw() {
        return raw;
    }
//
//    public SymMatrixBuffer unsafeSetRaw(@Nullable IExpr @Nullable [] raw) {
//        this.raw = raw;
//        return this;
//    }

    public SymMatrixBuffer unsafeSetFields(
            int numRows, int numCols,
            int offset, int rowStride, int colStride,
            boolean isZero, boolean isEmpty) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.offset = offset;
        this.rowStride = rowStride;
        this.colStride = colStride;
        this.isZero = isZero;
        this.isEmpty = isEmpty;
        return this;
    }
//
//    public SymMatrixBuffer unsafeSetFields(
//            @Nullable IExpr @Nullable [] raw,
//            int numRows, int numCols,
//            int offset, int rowStride, int colStride,
//            boolean isZero, boolean isEmpty) {
//        return unsafeSetRaw(raw)
//                .unsafeSetFields(numRows, numCols, offset, rowStride, colStride, isZero, isEmpty);
//    }



    /**
     * create a new SymMatrix instance with all the primitive fields copied from this, and
     * DIRECTLY USEing the INTERNAL MUTABLE ARRAY of this. <br> <br>
     * UNSAFE: the "this" instance should not be used after this function
     * @return the new SymMatrix instance
     */
    public SymMatrix unsafeToMatrix() {
        assert this.hasAllElemsNonNull();
        //noinspection NullableProblems
        return SymMatrix.unsafeOf(
                raw, numRows, numCols,
                offset, rowStride, colStride, isZero, isEmpty);
    }

    /**
     * This function recycles 'this' buffer back to the bufferPool IF AND ONLY IF
     * a new buffer had to be allocated for re-layout.
     * If the current buffer is already in the correct layout (or is empty), it returns 'this'
     * directly and no recycling happens. <br> <br>
     * UNSAFE: Always use the returned buffer and do NOT manually recycle 'this' variable afterward.
     * @param bufferPool the bufferPool
     * @return a SymMatrixBuffer instance (may new or not new) that is in Contiguous Row Major layout with 0 offset
     */
    public SymMatrixBuffer unsafeTo0ContRowMaj(@NonNull SymMatrixBufferPool bufferPool) {
        if (isEmpty || this.is0ContRowMaj()) {
            return this;
        }
        SymMatrixBuffer newBuffer = bufferPool.lease0ContRowMaj(numRows, numCols);
        for (int r = 0; r < numRows; r++) {
            int newRowRawIdx = r * numCols;
            int thisRowRawIdx = offset + r * rowStride;
            for (int c = 0; c < numCols; c++) {
                newBuffer.raw[newRowRawIdx + c]
                        = this.raw[thisRowRawIdx + c * colStride];
            }
        }
        bufferPool.recycle(this);
        return newBuffer;
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

    /**
     * throw if numRows or numCols < 0
     * @param numRows numRows
     * @param numCols numCols
     * @throws IllegalArgumentException the exception
     */
    private static void ensureNonNegDimension(int numRows, int numCols) throws IllegalArgumentException {
        if (numRows < 0 || numCols < 0) {
            throw new IllegalArgumentException( String.format(
                    "Invalid Dimensions: the given numRows(%d) and numCols(%d) are invalid",
                    numRows, numCols
            ));
        }
    }
    private static boolean isDimensionEmpty(int numRows, int numCols) {
        return numRows == 0 || numCols == 0;
    }

}
