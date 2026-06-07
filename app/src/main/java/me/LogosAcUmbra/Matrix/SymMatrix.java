package me.LogosAcUmbra.Matrix;

import org.matheclipse.core.expression.F;

import org.jspecify.annotations.NonNull;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IAST;
import org.matheclipse.core.interfaces.IExpr;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SymMatrix implements ISymMatrix, ISymMatrixExpr {

    protected static final int useParallelThreshold = 64;
    protected static final ThreadLocal<ExprEvaluator> EVALUATOR
            = ThreadLocal.withInitial(ExprEvaluator::new);

    protected final @NonNull IExpr @NonNull [] raw;
    protected final int numRows, numCols;
    protected final int offset, rowStride, colStride;


    protected final static IExpr[] ZERO_MAT_RAW = new IExpr[0];
    protected final static SymMatrix MAT0 = new SymMatrix(
            ZERO_MAT_RAW, 0, 0, 0, 0, 0
    );

    SymMatrix(
            @NonNull IExpr @NonNull [] raw,
            int numRows, int numCols,
            int offset, int rowStride, int colStride
    ) {
        this.raw = raw;
        this.numRows = numRows;
        this.numCols = numCols;
        this.offset = offset;
        this.rowStride = rowStride;
        this.colStride = colStride;
    }

    public static SymMatrix zeroOfSize(int numRows, int numCols) {
        return new SymMatrix(ZERO_MAT_RAW, numRows, numCols, 0, numCols, 1);
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
        return Optional.of(unsafeOfRowMaj(matInArr, numRows, numCols));
    }

    public static Optional<SymMatrix> optOf(@NonNull IExpr @NonNull [] @NonNull [] mat) {
        int numRows = mat.length;
        if (numRows == 0) {
            return Optional.of(MAT0);
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
        return Optional.of(unsafeOfRowMaj(matInArr, numRows, numCols));
    }

    protected static SymMatrix unsafeOfRowMaj(@NonNull IExpr @NonNull [] mat, int numRows, int numCols) {
        return new SymMatrix(
                mat,
                numRows, numCols,
                0, numCols, 1);
    }
    protected static SymMatrix unsafeOfColMaj(@NonNull IExpr @NonNull [] mat, int numRows, int numCols) {
        return new SymMatrix(
                mat,
                numRows, numCols,
                0, 1, numRows);
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
    public @NonNull ISymMatrixExpr plus(@NonNull ISymMatrixExpr expr) {
        return SumExpr.of(this, expr);
    }

    @Override
    public @NonNull ISymMatrixExpr minus(@NonNull ISymMatrixExpr expr) {
        return SumExpr.ofMinus(this, expr);
    }

    @Override
    public @NonNull ISymMatrixExpr scale(@NonNull IExpr scalar) {
        return ScaleExpr.of(this, scalar);
    }

    @Override
    public @NonNull ISymMatrixExpr times(@NonNull ISymMatrixExpr expr) {
        return MulExpr.of(this, expr);
    }

    @Override
    public void evalInto(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool bufferPool) {
        assert this.numRows == target.numRows && this.numCols == target.numCols;
        // copy, suiting offsets and strides
        for (int r = 0; r < numRows; ++r) {
            int rawRowIdx = offset + r * this.rowStride;
            int bufferRowIdx = target.offset + r * target.rowStride;

            for (int c = 0; c < numCols; ++c) {
                target.raw[bufferRowIdx + c * colStride] = this.raw[rawRowIdx + c * this.colStride];
            }
        }
    }

    @Override
    public @NonNull List<ISymMatrixExpr> getOperands() {
        return List.of();
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
                colStride, rowStride // swap
        );
    }

    public boolean isZero() {
        return this.raw == ZERO_MAT_RAW;
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
