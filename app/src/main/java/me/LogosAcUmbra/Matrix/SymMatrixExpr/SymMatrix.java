package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import me.LogosAcUmbra.Matrix.Matrix;
import me.LogosAcUmbra.Matrix.SymMatrixBuffer;
import me.LogosAcUmbra.Matrix.SymMatrixBufferPool;
import org.jspecify.annotations.NonNull;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IAST;
import org.matheclipse.core.interfaces.IExpr;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * not thread safe (for now) <br>
 * does not support negative strides
 */
public class SymMatrix implements Matrix, SymMatrixAdvancedExpr {

    protected static final ThreadLocal<ExprEvaluator> EVALUATOR
            = ThreadLocal.withInitial(ExprEvaluator::new);


    protected final @NonNull IExpr @NonNull [] raw;
    protected final int numRows, numCols;
    protected final int offset, rowStride, colStride;

    protected final boolean isZero, isEmpty;

    protected final static IExpr[] EMPTY_ARR = new IExpr[0];

    SymMatrix(
            @NonNull IExpr @NonNull [] raw,
            int numRows, int numCols,
            int offset, int rowStride, int colStride,
            boolean isZero, boolean isEmpty
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
     * Zero Matrix Invariant: for {@link #SymMatrix}, any zero matrices strictly follows the following: <br>
     * offset = {@code 0}; isZero = {@code true}; isEmpty = {@code false}; <br>
     * IMPORTANT: raw MAYBE {@link #EMPTY_ARR} for space optimization
     * @param numRows the number of rows
     * @param numCols the number of columns
     * @return the resultant zero SymMatrix instance
     */
    public static SymMatrix zeroOfSize(int numRows, int numCols) {
        ensureNonNegDimension(numRows, numCols);
        if (isDimensionEmpty(numRows, numCols)) {
            return emptyOfSizeHelper(numRows, numCols);
        }
        return zeroOfSizeHelper(numRows, numCols);
    }

    /**
     * create an empty SymMatrix instance in dimension of 0x0 <br>
     * <br>
     * Empty Matrix Invariant: for {@link #SymMatrix}, any empty matrices strictly follows the following: <br>
     * raw = {@link #EMPTY_ARR}; offset, rowStride and colStride = {@code 0}; isZero, isEmpty = {@code true};
     * @return the resultant empty SymMatrix instance
     */
    public static SymMatrix empty() {
        return emptyOfSizeHelper(0, 0);
    }

    /**
     * create an empty SymMatrix instance with the given dimensions <br> <br>
     * Empty Matrix Invariant: for {@link #SymMatrix}, any empty matrices strictly follows the following: <br>
     * raw = {@link #EMPTY_ARR}; offset, rowStride and colStride = {@code 0}; isZero, isEmpty = {@code true};
     * @param numRows the number of rows
     * @param numCols the number of columns
     * @return the resultant empty SymMatrix instance
     */
    public static SymMatrix emptyOfSize(int numRows, int numCols) {
        ensureNonNegDimension(numRows, numCols);
        if (!isDimensionEmpty(numRows, numCols)) {
            throw new IllegalArgumentException( String.format(
                    "Illegal Dimensions: given numRows(%s) and numCols(%s) is invalid for constructing an empty matrix",
                    numRows, numCols
            ));
        }
        return emptyOfSizeHelper(numRows, numCols);
    }

    public static SymMatrix of(int numRows, int numCols, @NonNull IExpr val) {
        ensureNonNegDimension(numRows, numCols);
        if (isDimensionEmpty(numRows, numCols)) {
            return emptyOfSizeHelper(numRows, numCols);
        }
        if (val.equals(F.C0)) { // if it is already 0 without evaluation
            return zeroOfSizeHelper(numRows, numCols);
        }
        IExpr[] arr = new IExpr[numRows * numCols];
        Arrays.fill(arr, val);
        return new SymMatrix(
                arr, numRows, numCols,
                0, numCols, 1, false, false);
    }

    public static Optional<SymMatrix> optOf(@NonNull IExpr mat) {
        int[] numRowsCols = mat.isMatrix();
        if (numRowsCols == null) {
            return Optional.empty();
        }
        assert numRowsCols.length == 2;

        IAST matAST = (IAST) mat;

        int numRows = numRowsCols[0];
        int numCols = numRowsCols[1];
        IExpr[] matInArr = new IExpr[numRows * numCols];
        for (int r = 0; r < numRows; ++r) {
            IAST rowAST = matAST.getAST(r + 1); // refer to AST storing structure
            for (int c = 0; c < numCols; ++c) {
                matInArr[r * numCols + c] = rowAST.get(c + 1); // same as above
            }
        }
        return Optional.of(unsafeOf0ContRowMaj(matInArr, numRows, numCols));
    }

    public static Optional<SymMatrix> optOf(@NonNull IExpr @NonNull [] @NonNull [] mat) {
        int numRows = mat.length;
        if (numRows == 0) {
            return Optional.of(empty());
        }
        int numCols = mat[0].length;
        for (int r = 1; r < numRows; ++r) {
            if (mat[r].length != numCols) {
                return Optional.empty();
            }
        }
        IExpr [] matInArr = new IExpr[numRows * numCols];
        for (int r = 0; r < numRows; ++r) {
            System.arraycopy(mat[r], 0, matInArr, r * numRows, numCols);
        }
        return Optional.of(unsafeOf0ContRowMaj(matInArr, numRows, numCols));
    }

    public static SymMatrix unsafeOf0ContRowMaj(@NonNull IExpr @NonNull [] mat, int numRows, int numCols) {
        return new SymMatrix(
                mat,
                numRows, numCols,
                0, numCols, 1,
                false, false);
    }
    public static SymMatrix unsafeOf0ContColMaj(@NonNull IExpr @NonNull [] mat, int numRows, int numCols) {
        return new SymMatrix(
                mat,
                numRows, numCols,
                0, 1, numRows,
                false, false);
    }
    public static SymMatrix unsafeOf(
            @NonNull IExpr @NonNull [] raw, int numRows, int numCols,
            int offset, int rowStride, int colStride,
            boolean isZero, boolean isEmpty) {
        return new SymMatrix(
                raw, numRows, numCols,
                offset, rowStride, colStride,
                isZero, isEmpty);
    }


    public @NonNull IExpr get(int rowIdx, int colIdx) {
        return raw[offset + rowIdx * rowStride + colIdx * colStride];
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
        return offset;
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
    public void computeIntoBuffer(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool pool) {
        assert this.numRows == target.getNumRows() && this.numCols == target.getNumCols();
        // copy, complying layout of target
        for (int r = 0; r < numRows; ++r) {
            int rawRowIdx = offset + r * this.rowStride;
            int bufferRowIdx = target.getOffset() + r * target.getRowStride();

            for (int c = 0; c < numCols; ++c) {
                target.unsafeGetRaw()[bufferRowIdx + c * colStride] = this.raw[rawRowIdx + c * this.colStride];
            }
        }
    }
    @Override
    public @NonNull SymMatrixBuffer computeToBuffer(@NonNull SymMatrixBufferPool pool) {
        if (isEmpty()) {
            SymMatrixBuffer result = pool.lease(0);
            result.unsafeSetFields(
                    this.numRows, this.numCols, 0, 0, 0, true, true
            );
            return result;
        }
        if (isContAnyMaj()) {
            int capacity = this.numRows * this.numCols;
            SymMatrixBuffer result = pool.lease(capacity);
            System.arraycopy(this.raw, this.offset, result.unsafeGetRaw(), 0, capacity);
            result.unsafeSetFields(
                    this.numRows, this.numCols, this.offset, this.rowStride, this.colStride, this.isZero, false)
            ;
            return result;
        }
        // although we can freely choose the layout, it is still better to be not fragmented,
        // as we decided to change the layout, we change it to the best layout 0ContRowMaj
        SymMatrixBuffer result = pool.lease0ContRowMaj(this.numRows, this.numCols);
        computeIntoBuffer(result, pool);
        return result;
    }

    @Override
    public @NonNull List<SymMatrixExpr> getOperands() {
        return List.of(this);
    }

    public int getRowDimension() {
        return numRows;
    }
    public int getColumnDimension() {
        return numCols;
    }

    public SymMatrix transpose() {
        return new SymMatrix(
                raw,
                numCols, numRows, // swap
                offset,
                colStride, rowStride, // swap
                isZero, isEmpty
        );
    }

    @Override
    public boolean isZero() {
        return isZero;
    }

    @Override
    public boolean isEmpty() {
        return isEmpty;
    }


    private static SymMatrix emptyOfSizeHelper(int numRows, int numCols) {
        assert isDimensionEmpty(numRows, numCols);
        return new SymMatrix(EMPTY_ARR, numRows, numCols, 0, 0, 0, true, true  );
    }
    private static SymMatrix zeroOfSizeHelper(int numRows, int numCols) {
        assert !isDimensionEmpty(numRows, numCols);
        return new SymMatrix(EMPTY_ARR, numRows, numCols, 0, numCols, 1, true, false  );
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
                    "Illegal Dimensions: the given numRows(%d) and numCols(%d) are invalid",
                    numRows, numCols
            ));
        }
    }
    private static boolean isDimensionEmpty(int numRows, int numCols) {
        return numRows == 0 || numCols == 0;
    }

//    public SymMatrix plus(@NonNull SymMatrix other) {
//        if (this.numRows != other.numRows || this.numCols != other.numCols) {
//            throw new IllegalArgumentException(String.format(
//                    "invalid dimension: this.numRows(%d) != other.numRows(%d) || this.numCols(%d) != other.numCols(%d). Infos: { this: %s, other %s }",
//                    this.numRows, other.numRows, this.numCols, other.numCols, this, other
//            ));
//        }
//        return this.unsafeAdd(other);
//    }
//
//    public SymMatrix times(@NonNull SymMatrix other) {
//        if (this.numCols != other.numRows) {
//            throw new IllegalArgumentException(String.format(
//                    "invalid dimension: this.numCols(%d) != other.numRows(%d). Infos: { this: %s, other %s }",
//                    this.numCols, other.numRows, this, other
//            ));
//        }
//        return this.unsafeMultiply(other);
//    }

//    protected SymMatrix unsafeAdd(@NonNull SymMatrix other) {
//        boolean useParallel = this.numRows * this.numCols >= useParallelThreshold;
//        if (useParallel) {
//            return unsafeMultiplyParallel(other);
//        } else {
//            return unsafeMultiplySequential(other);
//        }
//    }
//
//    protected SymMatrix unsafeMultiply(SymMatrix other) {
//        boolean useParallel = this.numRows * other.numCols >= useParallelThreshold;
//        if (useParallel) {
//            return unsafeMultiplyParallel(other);
//        } else {
//            return unsafeMultiplySequential(other);
//        }
//    }
//
//    protected SymMatrix unsafeMultiplySequential(SymMatrix other) {
//        if (this.colStride == 1) { // this is RowMajor
//            if (other.colStride == 1) {
//                return unsafeMultiplyRMajXRMaj(other);
//            }
//            if (other.rowStride == 1) {
//                return unsafeMultiplyRMajXCMaj(other);
//            }
//        } else if (this.rowStride == 1) {
//            if (other.colStride == 1) {
//                return unsafeMultiplyCMajXRMaj(other);
//            }
//            if (other.rowStride == 1) {
//                return unsafeMultiplyCMajXCMaj(other);
//            }
//        }
//        return unsafeMultiplyGeneral(other);
//    }
//
//    protected SymMatrix unsafeMultiplyParallel(SymMatrix other) {
//        return unsafeMultiplyParallelGeneral(other);
////        if (this.colStride == 1) { // this is RowMajor
////            if (other.colStride == 1) {
////                return unsafeMultiplyRMajXRMaj(other);
////            }
////            if (other.rowStride == 1) {
////                return unsafeMultiplyRMajXCMaj(other);
////            }
////        } else if (this.rowStride == 1) {
////            if (other.colStride == 1) {
////                return unsafeMultiplyCMajXRMaj(other);
////            }
////            if (other.rowStride == 1) {
////                return unsafeMultiplyCMajXCMaj(other);
////            }
////        }
////        return unsafeMultiplyGeneral(other);
//    }
//
//    // rkc
//    protected SymMatrix unsafeMultiplyParallelGeneral(SymMatrix other) {
//        int resNumRows = this.numRows;
//        int resNumCols = other.numCols;
//        int commonNum = this.numCols; // this.numCols or other.numRows
//        IExpr [] resMat = new IExpr[resNumRows * resNumCols];
//        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
//            for (int r = 0; r < resNumRows; ++r) {
//                int resRowOffset = r * resNumCols;
//                int thisIdxRow = r * this.rowStride;
//                executor.submit(() -> {
//                    IExpr [] threadLocalArr = new IExpr[resNumCols];
//                    Arrays.fill(threadLocalArr, F.C0);
//                    for (int k = 0; k < commonNum; ++k) {
//                        int thisIdxCol = k * this.colStride;
//                        int otherIdxRow = k * other.rowStride;
//                        for (int c = 0; c < resNumCols; ++c) {
//                            threadLocalArr[c] = F.Plus(
//                                    threadLocalArr[c],
//                                    F.Times(
//                                            this.raw[offset + thisIdxRow + thisIdxCol], // r * thisRStride + k * thisCStride
//                                            other.raw[offset + otherIdxRow + c * other.colStride] // k * otherRStride + c * otherCStride
//                                    )
//                            );
//                        }
//                    }
//                    System.arraycopy(threadLocalArr, 0, resMat, resRowOffset,  resNumCols);
//                });
//            }
//        }
//        return unsafeOfRowMaj(resMat, resNumRows, resNumCols);
//    }
//
//    protected SymMatrix unsafeMultiplyParallelRMajXOther(SymMatrix other) {
//        assert this.colStride == 1;
//        int numRows = this.numRows;
//        int numCols = other.numCols;
//        int commonNum = this.numCols; // this.numCols or other.numRows
//        IExpr [] resultMat = new IExpr[numRows * numCols];
//        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
//            for (int r = 0; r < numRows; ++r) {
//                int resRowOffset = r * numCols;
//                int thisRowOffSet = r * this.rowStride;
//                if (other.colStride == 1) { // other is RowMajor
//                    executor.submit(() -> {
//                        unsafeMultiplyRowRMajXRMaj(
//                                other, resultMat,
//                                numCols, commonNum, resRowOffset, thisRowOffSet);
//                    });
//                } else if (other.rowStride == 1) { // other is ColMajor
//                    executor.submit(() -> {
//                        unsafeMultiplyRowRMajXCMaj(
//                                other, resultMat,
//                                numCols, commonNum, resRowOffset, thisRowOffSet);
//                    });
//                } else {
//                    executor.submit(() -> {
//                        unsafeMultiplyRowRMajXGeneral(
//                                other, resultMat,
//                                numCols, commonNum, resRowOffset, thisRowOffSet);
//                    });
//                }
//            }
//        }
//        return unsafeOfRowMaj(resultMat, numRows, numCols);
//    }
//
//    protected void unsafeMultiplyRowRMajXRMaj(
//            SymMatrix other, IExpr[] resMat,
//            int resNumCols, int commonSideSize, int resRowOffset, int thisRowOffset) {
//        assert other.colStride == 1;
//        IExpr[] localRow = new IExpr[resNumCols];
//        Arrays.fill(localRow, F.C0);
//        for (int k = 0; k < commonSideSize; ++k) {
//            int otherRowOffset = k * other.rowStride;
//            for (int c = 0; c < resNumCols; ++c) {
//                localRow[c] = F.Plus(
//                        localRow[c],
//                        F.Times(
//                                this.raw[offset + thisRowOffset + k],
//                                other.raw[offset + otherRowOffset + c]
//                        )
//                );
//            }
//        }
//        System.arraycopy(localRow, 0, resMat, resRowOffset, resNumCols);
//    }
//    protected void unsafeMultiplyRowRMajXCMaj(
//            SymMatrix other, IExpr[] resMat,
//            int resNumCols, int commonSideSize, int resRowOffset, int thisRowOffset) {
//        assert other.rowStride == 1;
//        IExpr[] localRow = new IExpr[resNumCols];
//        Arrays.fill(localRow, F.C0);
//        for (int c = 0; c < resNumCols; ++c) { // other is ColumnMajor, IJK (RCK)
//            int otherColOffset = c * other.colStride;
//            for (int k = 0; k < commonSideSize; ++k) {
//                localRow[c] = F.Plus(
//                        localRow[c],
//                        F.Times(
//                                this.raw[offset + thisRowOffset + k],
//                                other.raw[offset + k + otherColOffset]
//                        )
//                );
//            }
//        }
//        System.arraycopy(localRow, 0, resMat, resRowOffset, resNumCols);
//    }
//    protected void unsafeMultiplyRowRMajXGeneral(
//            SymMatrix other, IExpr[] resMat,
//            int resNumCols, int commonSideSize, int resRowOffset, int thisRowOffset) {
//        IExpr[] localRow = new IExpr[resNumCols];
//        Arrays.fill(localRow, F.C0);
//        for (int k = 0; k < commonSideSize; ++k) {
//            for (int c = 0; c < resNumCols; ++c) {
//                localRow[c] = F.Plus(
//                        localRow[c],
//                        F.Times(
//                                this.raw[offset + thisRowOffset + k],
//                                other.raw[offset + k * other.rowStride + c * other.colStride]
//                        )
//                );
//            }
//        }
//        System.arraycopy(localRow, 0, resMat, resRowOffset, resNumCols);
//    }
//    /**
//     * Performs a fast O(N^3) matrix multiplication optimized for Row-Major (this)
//     * multiplied by Row-Major (other) layouts using an R-K-C loop order without multi-threading.
//     *
//     * @param other the right matrix operand, strictly expected to be in Row-Major layout
//     * @return a new Row-Major {@link SymMatrix} representing {@code this * other}
//     */
//    protected SymMatrix unsafeMultiplyRMajXRMaj(SymMatrix other) {
//        assert this.colStride == 1 && other.colStride == 1;
//        int numRows = this.numRows;
//        int numCols = other.numCols;
//        int commonNum = this.numCols; // this.numCols or other.numRows
//        IExpr [] resultMat = new IExpr[numRows * numCols];
//        Arrays.fill(resultMat, F.C0);
//        for (int r = 0; r < numRows; ++r) {
//            int resRowOffset = r * numCols;
//            int thisRowOffset = r * this.rowStride;
//            for (int k = 0; k < commonNum; ++k) {
//                int otherRowOffset = k * other.rowStride;
//                for (int c = 0; c < numCols; ++c) {
//                    resultMat[resRowOffset + c] = F.Plus(
//                            resultMat[resRowOffset + c],
//                            F.Times(
//                                    this.raw[offset + thisRowOffset + k],
//                                    other.raw[offset + otherRowOffset + c]
//                            ));
//                }
//            }
//        }
//        return unsafeOfRowMaj(resultMat, numRows, numCols);
//    }
//    /**
//     * Performs a fast O(N^3) matrix multiplication optimized for Row-Major (this)
//     * multiplied by Column-Major (other) layouts using an R-C-K loop order without multi-threading.
//     *
//     * @param other the right matrix operand, strictly expected to be in Column-Major layout
//     * @return a new Row-Major {@link SymMatrix} representing {@code this * other}
//     */
//    protected SymMatrix unsafeMultiplyRMajXCMaj(SymMatrix other) {
//        assert this.colStride == 1 && other.rowStride == 1;
//        int numRows = this.numRows;
//        int numCols = other.numCols;
//        int commonNum = this.numCols; // this.numCols or other.numRows
//        IExpr [] resultMat = new IExpr[numRows * numCols];
//        for (int r = 0; r < numRows; ++r) {
//            int resRowOffset = r * numCols;
//            int thisRowOffset = r * this.rowStride;
//            for (int c = 0; c < numCols; ++c) {
//                int otherColOffset = c * other.colStride;
//                IExpr sum = F.C0;
//                for (int k = 0; k < commonNum; ++k) {
//                    sum = F.Plus(
//                            sum,
//                            F.Times(
//                                    this.raw[offset + thisRowOffset + k],
//                                    other.raw[offset + k + otherColOffset]
//                            ));
//                }
//                resultMat[resRowOffset + c] = sum;
//            }
//        }
//        return unsafeOfRowMaj(resultMat, numRows, numCols);
//    }
//    /**
//     * Performs a fast O(N^3) matrix multiplication optimized for Column-Major (this)
//     * multiplied by Row-Major (other) layouts using an K-R-C loop order without multi-threading.
//     *
//     * @param other the right matrix operand, strictly expected to be in Row-Major layout
//     * @return a new Row-Major {@link SymMatrix} representing {@code this * other}
//     */
//    protected SymMatrix unsafeMultiplyCMajXRMaj(SymMatrix other) {
//        assert this.rowStride == 1 && other.colStride == 1;
//        int numRows = this.numRows;
//        int numCols = other.numCols;
//        int commonNum = this.numCols; // this.numCols or other.numRows
//        IExpr [] resultMat = new IExpr[numRows * numCols];
//        Arrays.fill(resultMat, F.C0);
//        for (int k = 0; k < commonNum; ++k) {
//            int thisColOffset = k * this.colStride;
//            int otherRowOffset = k * other.rowStride;
//            for (int r = 0; r < numRows; ++r) {
//                int resRowOffset = r * numCols;
//                for (int c = 0; c < numCols; ++c) {
//                    resultMat[resRowOffset + c] = F.Plus(
//                            resultMat[resRowOffset + c],
//                            F.Times(
//                                    this.raw[offset + r + thisColOffset],
//                                    other.raw[offset + otherRowOffset + c]
//                            ));
//                }
//            }
//        }
//        return unsafeOfRowMaj(resultMat, numRows, numCols);
//    }
//    /**
//     * Performs a fast O(N^3) matrix multiplication optimized for Column-Major (this)
//     * multiplied by Cow-Major (other) layouts using an C-K-R loop order without multi-threading.
//     *
//     * @param other the right matrix operand, strictly expected to be in Column-Major layout
//     * @return a new <strong>Column</strong>-Major {@link SymMatrix} representing {@code this * other}
//     */
//    protected SymMatrix unsafeMultiplyCMajXCMaj(SymMatrix other) {
//        assert this.rowStride == 1 && other.rowStride == 1;
//        int numRows = this.numRows;
//        int numCols = other.numCols;
//        int commonNum = this.numCols; // this.numCols or other.numRows
//        IExpr [] resultMat = new IExpr[numRows * numCols];
//        Arrays.fill(resultMat, F.C0);
//        for (int c = 0; c < numCols; ++c) {
//            int otherColOffset = c * other.colStride;
//            for (int k = 0; k < commonNum; ++k) {
//                int thisColOffset = k * this.colStride;
//                for (int r = 0; r < numRows; ++r) {
//                    int resIdx = c * numRows + r;
//                    resultMat[resIdx] = F.Plus(
//                            resultMat[resIdx],
//                            F.Times(
//                                    this.raw[offset + r + thisColOffset],
//                                    other.raw[k + otherColOffset]
//                            ));
//                }
//            }
//        }
//        return unsafeOfColMaj(resultMat, numRows, numCols);
//    }
//    /**
//     * Performs an O(N^3) matrix multiplication using an R-C-K loop order without multi-threading.
//     *
//     * @param other the right matrix operand, no restriction
//     * @return a new Row-Major {@link SymMatrix} representing {@code this * other}
//     */
//    protected SymMatrix unsafeMultiplyGeneral(SymMatrix other) {
//        int numRows = this.numRows;
//        int numCols = other.numCols;
//        int commonNum = this.numCols; // this.numCols or other.numRows
//        IExpr [] resultMat = new IExpr[numRows * numCols];
//        for (int r = 0; r < numRows; ++r) {
//            int resRowOffset = r * numCols;
//            int thisRowOffset = r * this.rowStride;
//            for (int c = 0; c < numCols; ++c) {
//                int otherColOffset = c * other.colStride;
//                IExpr sum = F.C0;
//                for (int k = 0; k < commonNum; ++k) {
//                    sum = F.Plus(
//                            sum,
//                            F.Times(
//                                    this.raw[offset + thisRowOffset + k * this.colStride],
//                                    other.raw[offset + k * other.rowStride + otherColOffset]
//                            ));
//                }
//                resultMat[resRowOffset + c] = sum;
//            }
//        }
//        return unsafeOfRowMaj(resultMat, numRows, numCols);
//    }
//
//
//    public static void main(String[] args) {
//        ExprEvaluator exprEvaluator = new ExprEvaluator();
//        IExpr formula = F.Times(F.Subtract(F.a, F.b), F.Plus(F.a, F.b));
//        System.out.println(exprEvaluator.eval(F.Expand(formula)));
//        IExpr gaussian = F.Integrate(
//                F.Exp(F.Negate(F.Power(F.x, F.C2))), F.x
//        );
//        System.out.println(gaussian);
//        System.out.println(exprEvaluator.eval(gaussian));
//        IExpr mat = F.List(F.List(F.a, F.b), F.List(F.c, F.d));
//        System.out.println(mat);
//    }
}
