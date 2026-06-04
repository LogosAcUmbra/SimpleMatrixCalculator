package me.LogosAcUmbra.Matrix;

import org.matheclipse.core.expression.F;

import org.jspecify.annotations.NonNull;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IAST;
import org.matheclipse.core.interfaces.IExpr;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SymbolicMatrix {

    protected static final int useParallelThreshold = 64;
    protected static final ThreadLocal<ExprEvaluator> EVALUATOR
            = ThreadLocal.withInitial(ExprEvaluator::new);

    protected final @NonNull IExpr @NonNull [] mat;
    protected final int numRows;
    protected final int numCols;
    protected final int rowStride;
    protected final int colStride;


    protected final static SymbolicMatrix MAT0 = new SymbolicMatrix(
            new IExpr[0], 0, 0, 0, 0
    );

    protected SymbolicMatrix(
            @NonNull IExpr @NonNull [] mat,
            int numRows, int numCols,
            int rowStride, int colStride
    ) {
        this.mat = mat;
        this.numRows = numRows;
        this.numCols = numCols;
        this.rowStride = rowStride;
        this.colStride = colStride;
    }

    public static Optional<SymbolicMatrix> optOf(@NonNull IExpr mat) {
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

    public static Optional<SymbolicMatrix> optOf(@NonNull IExpr @NonNull [] @NonNull [] mat) {
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

    protected static SymbolicMatrix unsafeOfRowMaj(@NonNull IExpr @NonNull [] mat, int numRows, int numCols) {
        return new SymbolicMatrix(
                mat,
                numRows, numCols,
                numCols, 1);
    }
    protected static SymbolicMatrix unsafeOfColMaj(@NonNull IExpr @NonNull [] mat, int numRows, int numCols) {
        return new SymbolicMatrix(
                mat,
                numRows, numCols,
                1, numRows);
    }

    public IExpr get(int rowIdx, int colIdx) {
        return mat[rowIdx * rowStride + colIdx * colStride];
    }
    public int getNumRows() {
        return numRows;
    }
    public int getNumCols() {
        return numCols;
    }
    public int getRowDimension() {
        return numRows;
    }
    public int getColumnDimension() {
        return numCols;
    }
    public int getRowStride() {
        return rowStride;
    }
    public int getColStride() {
        return colStride;
    }

    public SymbolicMatrix transpose() {
        return new SymbolicMatrix(
                mat,
                numCols, numRows, // swap
                colStride, rowStride // swap
        );
    }

    public SymbolicMatrix multiply(SymbolicMatrix other) {
        if (this.numCols != other.numRows) {
            throw new IllegalArgumentException(String.format(
                    "invalid multiplication: this.numCols(%d) != other.numRows(%d). Infos: { this: %s, other %s }",
                    this.numCols, other.numRows, this, other
            ));
        }
        return this.unsafeMultiply(other);
    }

    protected SymbolicMatrix unsafeMultiply(SymbolicMatrix other) {
        boolean useParallel = this.numRows * other.numCols >= useParallelThreshold;
        if (useParallel) {
            return unsafeMultiplyParallel(other);
        } else {
            return unsafeMultiplySingleThread(other);
        }
    }

    protected SymbolicMatrix unsafeMultiplySingleThread(SymbolicMatrix other) {
        if (this.colStride == 1) { // this is RowMajor
            if (other.colStride == 1) {
                return unsafeMultiplyRMajXRMaj(other);
            }
            if (other.rowStride == 1) {
                return unsafeMultiplyRMajXCMaj(other);
            }
        } else if (this.rowStride == 1) {
            if (other.colStride == 1) {
                return unsafeMultiplyCMajXRMaj(other);
            }
            if (other.rowStride == 1) {
                return unsafeMultiplyCMajXCMaj(other);
            }
        }
        return unsafeMultiplyGeneral(other);
    }

    protected SymbolicMatrix unsafeMultiplyParallel(SymbolicMatrix other) {
        return unsafeMultiplyParallelGeneral(other);
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
    }

    // rkc
    protected SymbolicMatrix unsafeMultiplyParallelGeneral(SymbolicMatrix other) {
        int resNumRows = this.numRows;
        int resNumCols = other.numCols;
        int commonNum = this.numCols; // this.numCols or other.numRows
        IExpr [] resMat = new IExpr[resNumRows * resNumCols];
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int r = 0; r < resNumRows; ++r) {
                int resRowOffset = r * resNumCols;
                int thisIdxRow = r * this.rowStride;
                executor.submit(() -> {
                    IExpr [] threadLocalArr = new IExpr[resNumCols];
                    Arrays.fill(threadLocalArr, F.C0);
                    for (int k = 0; k < commonNum; ++k) {
                        int thisIdxCol = k * this.colStride;
                        int otherIdxRow = k * other.rowStride;
                        for (int c = 0; c < resNumCols; ++c) {
                            threadLocalArr[c] = F.Plus(
                                    threadLocalArr[c],
                                    F.Times(
                                            this.mat[thisIdxRow + thisIdxCol], // r * thisRStride + k * thisCStride
                                            other.mat[otherIdxRow + c * other.colStride] // k * otherRStride + c * otherCStride
                                    )
                            );
                        }
                    }
                    System.arraycopy(threadLocalArr, 0, resMat, resRowOffset,  resNumCols);
                });
            }
        }
        return unsafeOfRowMaj(resMat, resNumRows, resNumCols);
    }

    protected SymbolicMatrix unsafeMultiplyParallelRMajXOther(SymbolicMatrix other) {
        assert this.colStride == 1;
        int numRows = this.numRows;
        int numCols = other.numCols;
        int commonNum = this.numCols; // this.numCols or other.numRows
        IExpr [] resultMat = new IExpr[numRows * numCols];
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int r = 0; r < numRows; ++r) {
                int resRowOffset = r * numCols;
                int thisRowOffSet = r * this.rowStride;
                if (other.colStride == 1) { // other is RowMajor
                    executor.submit(() -> {
                        unsafeMultiplyRowRMajXRMaj(
                                other, resultMat,
                                numCols, commonNum, resRowOffset, thisRowOffSet);
                    });
                } else if (other.rowStride == 1) { // other is ColMajor
                    executor.submit(() -> {
                        unsafeMultiplyRowRMajXCMaj(
                                other, resultMat,
                                numCols, commonNum, resRowOffset, thisRowOffSet);
                    });
                } else {
                    executor.submit(() -> {
                        unsafeMultiplyRowRMajXGeneral(
                                other, resultMat,
                                numCols, commonNum, resRowOffset, thisRowOffSet);
                    });
                }
            }
        }
        return unsafeOfRowMaj(resultMat, numRows, numCols);
    }

    protected void unsafeMultiplyRowRMajXRMaj(
            SymbolicMatrix other, IExpr[] resMat,
            int resNumCols, int commonSideSize, int resRowOffset, int thisRowOffset) {
        assert other.colStride == 1;
        IExpr[] localRow = new IExpr[resNumCols];
        Arrays.fill(localRow, F.C0);
        for (int k = 0; k < commonSideSize; ++k) {
            int otherRowOffset = k * other.rowStride;
            for (int c = 0; c < resNumCols; ++c) {
                localRow[c] = F.Plus(
                        localRow[c],
                        F.Times(
                                this.mat[thisRowOffset + k],
                                other.mat[otherRowOffset + c]
                        )
                );
            }
        }
        System.arraycopy(localRow, 0, resMat, resRowOffset, resNumCols);
    }
    protected void unsafeMultiplyRowRMajXCMaj(
            SymbolicMatrix other, IExpr[] resMat,
            int resNumCols, int commonSideSize, int resRowOffset, int thisRowOffset) {
        assert other.rowStride == 1;
        IExpr[] localRow = new IExpr[resNumCols];
        Arrays.fill(localRow, F.C0);
        for (int c = 0; c < resNumCols; ++c) { // other is ColumnMajor, IJK (RCK)
            int otherColOffset = c * other.colStride;
            for (int k = 0; k < commonSideSize; ++k) {
                localRow[c] = F.Plus(
                        localRow[c],
                        F.Times(
                                this.mat[thisRowOffset + k],
                                other.mat[k + otherColOffset]
                        )
                );
            }
        }
        System.arraycopy(localRow, 0, resMat, resRowOffset, resNumCols);
    }
    protected void unsafeMultiplyRowRMajXGeneral(
            SymbolicMatrix other, IExpr[] resMat,
            int resNumCols, int commonSideSize, int resRowOffset, int thisRowOffset) {
        IExpr[] localRow = new IExpr[resNumCols];
        Arrays.fill(localRow, F.C0);
        for (int k = 0; k < commonSideSize; ++k) {
            for (int c = 0; c < resNumCols; ++c) {
                localRow[c] = F.Plus(
                        localRow[c],
                        F.Times(
                                this.mat[thisRowOffset + k],
                                other.mat[k * other.rowStride + c * other.colStride]
                        )
                );
            }
        }
        System.arraycopy(localRow, 0, resMat, resRowOffset, resNumCols);
    }
    /**
     * Performs a fast O(N^3) matrix multiplication optimized for Row-Major (this)
     * multiplied by Row-Major (other) layouts using an R-K-C loop order without multi-threading.
     *
     * @param other the right matrix operand, strictly expected to be in Row-Major layout
     * @return a new Row-Major {@link SymbolicMatrix} representing {@code this * other}
     */
    protected SymbolicMatrix unsafeMultiplyRMajXRMaj(SymbolicMatrix other) {
        assert this.colStride == 1 && other.colStride == 1;
        int numRows = this.numRows;
        int numCols = other.numCols;
        int commonNum = this.numCols; // this.numCols or other.numRows
        IExpr [] resultMat = new IExpr[numRows * numCols];
        Arrays.fill(resultMat, F.C0);
        for (int r = 0; r < numRows; ++r) {
            int resRowOffset = r * numCols;
            int thisRowOffset = r * this.rowStride;
            for (int k = 0; k < commonNum; ++k) {
                int otherRowOffset = k * other.rowStride;
                for (int c = 0; c < numCols; ++c) {
                    resultMat[resRowOffset + c] = F.Plus(
                            resultMat[resRowOffset + c],
                            F.Times(
                                    this.mat[thisRowOffset + k],
                                    other.mat[otherRowOffset + c]
                            ));
                }
            }
        }
        return unsafeOfRowMaj(resultMat, numRows, numCols);
    }
    /**
     * Performs a fast O(N^3) matrix multiplication optimized for Row-Major (this)
     * multiplied by Column-Major (other) layouts using an R-C-K loop order without multi-threading.
     *
     * @param other the right matrix operand, strictly expected to be in Column-Major layout
     * @return a new Row-Major {@link SymbolicMatrix} representing {@code this * other}
     */
    protected SymbolicMatrix unsafeMultiplyRMajXCMaj(SymbolicMatrix other) {
        assert this.colStride == 1 && other.rowStride == 1;
        int numRows = this.numRows;
        int numCols = other.numCols;
        int commonNum = this.numCols; // this.numCols or other.numRows
        IExpr [] resultMat = new IExpr[numRows * numCols];
        for (int r = 0; r < numRows; ++r) {
            int resRowOffset = r * numCols;
            int thisRowOffset = r * this.rowStride;
            for (int c = 0; c < numCols; ++c) {
                int otherColOffset = c * other.colStride;
                IExpr sum = F.C0;
                for (int k = 0; k < commonNum; ++k) {
                    sum = F.Plus(
                            sum,
                            F.Times(
                                    this.mat[thisRowOffset + k],
                                    other.mat[k + otherColOffset]
                            ));
                }
                resultMat[resRowOffset + c] = sum;
            }
        }
        return unsafeOfRowMaj(resultMat, numRows, numCols);
    }
    /**
     * Performs a fast O(N^3) matrix multiplication optimized for Column-Major (this)
     * multiplied by Row-Major (other) layouts using an K-R-C loop order without multi-threading.
     *
     * @param other the right matrix operand, strictly expected to be in Row-Major layout
     * @return a new Row-Major {@link SymbolicMatrix} representing {@code this * other}
     */
    protected SymbolicMatrix unsafeMultiplyCMajXRMaj(SymbolicMatrix other) {
        assert this.rowStride == 1 && other.colStride == 1;
        int numRows = this.numRows;
        int numCols = other.numCols;
        int commonNum = this.numCols; // this.numCols or other.numRows
        IExpr [] resultMat = new IExpr[numRows * numCols];
        Arrays.fill(resultMat, F.C0);
        for (int k = 0; k < commonNum; ++k) {
            int thisColOffset = k * this.colStride;
            int otherRowOffset = k * other.rowStride;
            for (int r = 0; r < numRows; ++r) {
                int resRowOffset = r * numCols;
                for (int c = 0; c < numCols; ++c) {
                    resultMat[resRowOffset + c] = F.Plus(
                            resultMat[resRowOffset + c],
                            F.Times(
                                    this.mat[r + thisColOffset],
                                    other.mat[otherRowOffset + c]
                            ));
                }
            }
        }
        return unsafeOfRowMaj(resultMat, numRows, numCols);
    }
    /**
     * Performs a fast O(N^3) matrix multiplication optimized for Column-Major (this)
     * multiplied by Cow-Major (other) layouts using an C-K-R loop order without multi-threading.
     *
     * @param other the right matrix operand, strictly expected to be in Column-Major layout
     * @return a new <strong>Column</strong>-Major {@link SymbolicMatrix} representing {@code this * other}
     */
    protected SymbolicMatrix unsafeMultiplyCMajXCMaj(SymbolicMatrix other) {
        assert this.rowStride == 1 && other.rowStride == 1;
        int numRows = this.numRows;
        int numCols = other.numCols;
        int commonNum = this.numCols; // this.numCols or other.numRows
        IExpr [] resultMat = new IExpr[numRows * numCols];
        Arrays.fill(resultMat, F.C0);
        for (int c = 0; c < numCols; ++c) {
            int otherColOffset = c * other.colStride;
            for (int k = 0; k < commonNum; ++k) {
                int thisColOffset = k * this.colStride;
                for (int r = 0; r < numRows; ++r) {
                    int resIdx = c * numRows + r;
                    resultMat[resIdx] = F.Plus(
                            resultMat[resIdx],
                            F.Times(
                                    this.mat[r + thisColOffset],
                                    other.mat[k + otherColOffset]
                            ));
                }
            }
        }
        return unsafeOfColMaj(resultMat, numRows, numCols);
    }
    /**
     * Performs an O(N^3) matrix multiplication using an R-C-K loop order without multi-threading.
     *
     * @param other the right matrix operand, no restriction
     * @return a new Row-Major {@link SymbolicMatrix} representing {@code this * other}
     */
    protected SymbolicMatrix unsafeMultiplyGeneral(SymbolicMatrix other) {
        int numRows = this.numRows;
        int numCols = other.numCols;
        int commonNum = this.numCols; // this.numCols or other.numRows
        IExpr [] resultMat = new IExpr[numRows * numCols];
        for (int r = 0; r < numRows; ++r) {
            int resRowOffset = r * numCols;
            int thisRowOffset = r * this.rowStride;
            for (int c = 0; c < numCols; ++c) {
                int otherColOffset = c * other.colStride;
                IExpr sum = F.C0;
                for (int k = 0; k < commonNum; ++k) {
                    sum = F.Plus(
                            sum,
                            F.Times(
                                    this.mat[thisRowOffset + k * this.colStride],
                                    other.mat[k * other.rowStride + otherColOffset]
                            ));
                }
                resultMat[resRowOffset + c] = sum;
            }
        }
        return unsafeOfRowMaj(resultMat, numRows, numCols);
    }


    public static void main(String[] args) {
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
    }
}
