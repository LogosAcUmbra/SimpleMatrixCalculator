package me.LogosAcUmbra.Matrix;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.matheclipse.core.expression.F;

import static me.LogosAcUmbra.Matrix.ISymMatrixAdvancedExpr.ensureIsAdvanced;

import java.util.Collections;
import java.util.List;

public class SumExpr implements IManyOperandExpr { // mutable

    @Nullable ISymMatrixExpr parent;
    final int numRows;
    final int numCols;
    final @NonNull ObjectArrayList<ISymMatrixAdvancedExpr> operands;
    final int numOperands;

    SumExpr(
            int numRows,
            int numCols,
            @NonNull ObjectArrayList<ISymMatrixAdvancedExpr> operands,
            int numOperands
    ) {
        // at least for now, SumExpr does not have cases that needs to hold 0 operands
        assert numRows >= 0 && numCols >= 0 && !operands.isEmpty();
        this.numRows = numRows;
        this.numCols = numCols;
        this.operands = operands;
        this.numOperands = numOperands;
    }

    public static SumExpr of(@NonNull ISymMatrixExpr expr) {
        ISymMatrixAdvancedExpr advExpr = ensureIsAdvanced(expr);
        if (advExpr instanceof SumExpr sumExpr) {
            return sumExpr;
        }
        return new SumExpr( advExpr.getNumRows(), advExpr.getNumCols(), ObjectArrayList.of(advExpr), 1 );
    }

    public static SumExpr of(@NonNull ISymMatrixExpr expr1, @NonNull ISymMatrixExpr expr2) {
        ISymMatrixAdvancedExpr advExpr1 = ensureIsAdvanced(expr1);
        ISymMatrixAdvancedExpr advExpr2 = ensureIsAdvanced(expr2);
        assert !(advExpr1 instanceof SumExpr);
        int numRows = advExpr1.getNumRows();
        int numCols = advExpr1.getNumCols();
        if (numRows != advExpr2.getNumRows() || numCols != advExpr2.getNumCols() ) {
            throw illegalDimensionException(advExpr1, advExpr2, "expr1", "expr2");
        }
        ObjectArrayList<ISymMatrixAdvancedExpr> operands;
        if (advExpr2 instanceof SumExpr sumExpr) {
            operands = new ObjectArrayList<>(1 + sumExpr.operands.size());
            operands.add(advExpr1);
            operands.addAll(sumExpr.operands);
        } else {
            operands = ObjectArrayList.of(advExpr1, advExpr2);
        }
        return new SumExpr(numRows, numCols, operands, 2);
    }
    static SumExpr ofMinus(@NonNull ISymMatrixExpr expr1, @NonNull ISymMatrixExpr expr2) {
        ISymMatrixAdvancedExpr advExpr1 = ensureIsAdvanced(expr1);
        ISymMatrixAdvancedExpr advExpr2 = ensureIsAdvanced(expr2);
        assert !(advExpr1 instanceof SumExpr);
        int numRows = advExpr1.getNumRows();
        int numCols = advExpr1.getNumCols();
        if (numRows != expr2.getNumRows() || numCols != expr2.getNumCols() ) {
            throw illegalDimensionException(advExpr1, advExpr2, "expr1", "expr2");
        }
        ObjectArrayList<ISymMatrixAdvancedExpr> operands = ObjectArrayList.of(advExpr1, NegExpr.of(advExpr2));
        return new SumExpr(numRows, numCols, operands, 2);
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
        ISymMatrixAdvancedExpr advExpr =  ensureIsAdvanced(expr);
        if (advExpr.getNumRows() != numRows || advExpr.getNumCols() != numCols) {
            throw illegalDimensionException(advExpr);
        }
        if (advExpr instanceof SumExpr sumExpr) {
            operands.addAll(sumExpr.operands);
            return new SumExpr(  numRows, numCols, operands, this.numOperands + sumExpr.numOperands  );
        }
        operands.add(advExpr);
        return new SumExpr(  numRows, numCols, operands, this.numOperands + 1  );
    }

    @Override
    public @NonNull ISymMatrixExpr minus(@NonNull ISymMatrixExpr expr) {
        ISymMatrixAdvancedExpr advExpr =  ensureIsAdvanced(expr);
        if (expr.getNumRows() != numRows || expr.getNumCols() != numCols) {
            throw illegalDimensionException(advExpr);
        }
        operands.add(NegExpr.of(advExpr));
        return new SumExpr(  numRows, numCols, operands, this.numOperands + 1 );
    }

    // advanced

    public void computeIntoBuffer(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool pool) {
        // at least for now, sumExpr must have this.operands.size() >= 1
        assert !operands.isEmpty();

        operands.getFirst().computeIntoBuffer(target, pool);
        assert numRows == target.numRows && numCols == target.numCols;

        SymMatrixBuffer scratch = pool.lease0ContRowMaj(numRows, numCols);
        for (int i = 1; i < operands.size(); i++) {
            ISymMatrixAdvancedExpr expr = operands.get(i);
            expr.computeIntoBuffer(scratch, pool);
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
        pool.recycle(scratch);
    }
    @Override
    public @NonNull SymMatrixBuffer computeToBuffer(@NonNull SymMatrixBufferPool pool) {
        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull List<ISymMatrixExpr> getOperands() {
        return Collections.unmodifiableList(operands.subList(0, numOperands));
    }

    /**
     * @return the direct reference to the internal ArrayList for storing operands
     */
    public @NonNull ObjectArrayList<ISymMatrixAdvancedExpr> getOperandsRef() {
        return operands;
    }

    @Override
    public @Nullable ISymMatrixExpr getParent() {
        return parent;
    }

    @Override
    public @NonNull SumExpr setParent(@NonNull ISymMatrixExpr parent) {
        this.parent = parent;
        return this;
    }

//    private ObjectArrayList<ISymMatrixAdvancedExpr> advancedOperands() {
//        return (ObjectArrayList<ISymMatrixAdvancedExpr>) (ObjectArrayList<?>) operands;
//    }

    private IllegalArgumentException illegalDimensionException(
            @NonNull ISymMatrixAdvancedExpr expr
    ) {
        return illegalDimensionException(this, expr, "this", "expr");
    }
    private static IllegalArgumentException illegalDimensionException(
            @NonNull ISymMatrixAdvancedExpr expr1,
            @NonNull ISymMatrixAdvancedExpr expr2,
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
