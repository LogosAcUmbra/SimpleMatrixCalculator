package me.LogosAcUmbra.Matrix;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NonNull;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;

import java.util.List;

public class SumExpr implements ISymMatrixExpr { // mutable

    final int numRows;
    final int numCols;
    final @NonNull ObjectArrayList<ISymMatrixExpr> operands;

    SumExpr(
            int numRows,
            int numCols,
            @NonNull ObjectArrayList<ISymMatrixExpr> operands) {
        // at least for now, SumExpr does not have cases that needs to hold 0 terms
        assert numRows >= 0 && numCols >= 0 && !operands.isEmpty();
        this.numRows = numRows;
        this.numCols = numCols;
        this.operands = operands;
    }

    static SumExpr of(@NonNull ISymMatrixExpr expr) {
        if (expr instanceof SumExpr sumExpr) {
            return sumExpr;
        }
        return new SumExpr( expr.getNumRows(), expr.getNumCols(), ObjectArrayList.of(expr) );
    }

    static SumExpr of(@NonNull ISymMatrixExpr expr1, @NonNull ISymMatrixExpr expr2) {
        return of2(expr1, expr2);
    }
    static SumExpr ofMinus(@NonNull ISymMatrixExpr expr1, @NonNull ISymMatrixExpr expr2) {
        return of2(expr1, NegExpr.of(expr2));
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
    public @NonNull ISymMatrixExpr plus(@NonNull ISymMatrixExpr expr) {
        if (expr.getNumRows() != numRows || expr.getNumCols() != numCols) {
            throw illegalDimensionException(expr);
        }
        if (expr instanceof SumExpr sumExpr) {
            operands.addAll(sumExpr.operands);
        } else {
            operands.add(expr);
        }
        return this;
    }

    @Override
    public @NonNull ISymMatrixExpr minus(@NonNull ISymMatrixExpr expr) {
        if (expr.getNumRows() != numRows || expr.getNumCols() != numCols) {
            throw illegalDimensionException(expr);
        }
        if (expr instanceof SumExpr sumExpr) {
            operands.addAll(sumExpr.operands);
        } else {
            operands.add(expr);
        }
        return this;
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
        // at least for now, sumExpr must have operands.size() >= 1
        assert !operands.isEmpty();
        operands.getFirst().evalInto(target, bufferPool);
        assert numRows == target.numRows && numCols == target.numCols;

        SymMatrixBuffer scratch = bufferPool.lease(numRows, numCols);
        for (int i = 1; i < operands.size(); i++) {
            ISymMatrixExpr expr = operands.get(i);
            expr.evalInto(scratch, bufferPool);
            for (int r = 0; r < numRows; ++r) {
                int sRowIdx = r * scratch.rowStride;
                int tRowIdx = r * target.rowStride;

                for (int c = 0; c < numCols; ++c) {
                    int sIdx = sRowIdx + c * scratch.colStride;
                    int tIdx = tRowIdx + c * target.colStride;
                    target.raw[tIdx] = F.Plus(
                            target.raw[tIdx],
                            scratch.raw[sIdx]
                    );
                }
            }
        }
        bufferPool.recycle(scratch);
    }

    @Override
    public @NonNull List<ISymMatrixExpr> getOperands() {
        return operands;
    }

    /**
     * UNSAFE <br>
     * return the direct reference to the internal ArrayList for storing operands
     *
     * @return the reference to the internal ArrayList for storing operands
     */
    public @NonNull ObjectArrayList<ISymMatrixExpr> unsafeGetOperands() {
        return operands;
    }

    private static SumExpr of2(@NonNull ISymMatrixExpr expr1, @NonNull ISymMatrixExpr expr2) {
        assert !(expr1 instanceof SumExpr);
        int numRows = expr1.getNumRows();
        int numCols = expr1.getNumCols();
        if (numRows != expr2.getNumRows() || numCols != expr2.getNumCols() ) {
            throw illegalDimensionException(expr1, expr2, "expr1", "expr2");
        }
        ObjectArrayList<ISymMatrixExpr> operands;
        if (expr2 instanceof SumExpr sumExpr) {
            operands = new ObjectArrayList<>(1 + sumExpr.operands.size());
            operands.add(expr1);
            operands.addAll(sumExpr.operands);
        } else {
            operands = ObjectArrayList.of(expr1, expr2);
        }
        return new SumExpr(numRows, numCols, operands);
    }

    private IllegalArgumentException illegalDimensionException(
            @NonNull ISymMatrixExpr expr
    ) {
        return illegalDimensionException(this, expr, "this", "expr");
    }
    private static IllegalArgumentException illegalDimensionException(
            @NonNull ISymMatrixExpr expr1,
            @NonNull ISymMatrixExpr expr2,
            @NonNull String expr1Name,
            @NonNull String expr2Name
    ) {
        return new IllegalArgumentException(String.format(
                "illegal dimension: %s.numRows(%d) != %s.numCols(%d), or %s.numRows(%d) != %s.numCols(%d). " +
                        "Infos: { this: (%s), expr: (%s) }",
                expr1Name, expr1.getNumRows(), expr1Name, expr1.getNumCols(),
                expr2Name, expr2.getNumRows(), expr2Name, expr2.getNumCols(),
                expr1, expr2
        ));
    }

}
