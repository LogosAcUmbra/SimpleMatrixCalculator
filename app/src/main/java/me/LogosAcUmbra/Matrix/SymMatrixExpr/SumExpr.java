package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import me.LogosAcUmbra.Matrix.OperandTree.OperandTreeList;
import me.LogosAcUmbra.Matrix.SymMatrixBuffer;
import me.LogosAcUmbra.Matrix.SymMatrixBufferPool;
import org.jspecify.annotations.NonNull;
import org.matheclipse.core.expression.F;

import static me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrixAdvancedExpr.ensureIsAdvanced;

public class SumExpr implements ManyOperandExpr { // mutable

    final int numRows;
    final int numCols;
    @NonNull OperandTreeList operands;

    SumExpr(
            int numRows,
            int numCols,
            @NonNull OperandTreeList operands
    ) {
        // at least for now, SumExpr does not have cases that needs to hold 0 operands
        assert numRows >= 0 && numCols >= 0 && !operands.isEmpty();
        this.numRows = numRows;
        this.numCols = numCols;
        this.operands = operands;
    }

    public static SumExpr of(@NonNull SymMatrixExpr expr) {
        SymMatrixAdvancedExpr advExpr = ensureIsAdvanced(expr);
        if (advExpr instanceof SumExpr sumExpr) {
            return sumExpr;
        }
        return new SumExpr( advExpr.getNumRows(), advExpr.getNumCols(), OperandTreeList.of(advExpr) );
    }

    public static SumExpr of(@NonNull SymMatrixExpr expr1, @NonNull SymMatrixExpr expr2) {
        SymMatrixAdvancedExpr advExpr1 = ensureIsAdvanced(expr1);
        SymMatrixAdvancedExpr advExpr2 = ensureIsAdvanced(expr2);
        assert !(advExpr1 instanceof SumExpr);

        int numRows = advExpr1.getNumRows();
        int numCols = advExpr1.getNumCols();
        if (numRows != advExpr2.getNumRows() || numCols != advExpr2.getNumCols() ) {
            throw illegalDimensionException(advExpr1, advExpr2, "expr1", "expr2");
        }
        OperandTreeList operands = (
                (advExpr2 instanceof SumExpr sumExpr)
                ? (OperandTreeList.builder(1 + sumExpr.operands.size())
                    .add(advExpr1)
                    .addAll(sumExpr.operands)
                    .build())
                : OperandTreeList.of(advExpr1, advExpr2)
        );
        return new SumExpr(numRows, numCols, operands);
    }
    static SumExpr ofMinus(@NonNull SymMatrixExpr expr1, @NonNull SymMatrixExpr expr2) {
        SymMatrixAdvancedExpr advExpr1 = ensureIsAdvanced(expr1);
        SymMatrixAdvancedExpr advExpr2 = ensureIsAdvanced(expr2);
        assert !(advExpr1 instanceof SumExpr);

        int numRows = advExpr1.getNumRows();
        int numCols = advExpr1.getNumCols();
        if (numRows != expr2.getNumRows() || numCols != expr2.getNumCols() ) {
            throw illegalDimensionException(advExpr1, advExpr2, "expr1", "expr2");
        }
        OperandTreeList operands = OperandTreeList.of(advExpr1, NegExpr.of(advExpr2));
        return new SumExpr(numRows, numCols, operands);
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
    public @NonNull SymMatrixExpr plus(@NonNull SymMatrixExpr expr) {
        SymMatrixAdvancedExpr advExpr =  ensureIsAdvanced(expr);
        if (advExpr.getNumRows() != numRows || advExpr.getNumCols() != numCols) {
            throw illegalDimensionException(advExpr);
        }
        if (advExpr instanceof SumExpr sumExpr) {
            return new SumExpr(  numRows, numCols, operands.withAll(sumExpr.operands)  );
        }
        return new SumExpr(  numRows, numCols, operands.with(advExpr)  );
    }

    @Override
    public @NonNull SymMatrixExpr minus(@NonNull SymMatrixExpr expr) {
        SymMatrixAdvancedExpr advExpr =  ensureIsAdvanced(expr);
        if (expr.getNumRows() != numRows || expr.getNumCols() != numCols) {
            throw illegalDimensionException(advExpr);
        }
        return new SumExpr(  numRows, numCols, operands.with(NegExpr.of(advExpr)) );
    }

    // advanced

    public void computeIntoBuffer(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool pool) {
        // at least for now, sumExpr must have this.operands.size() >= 1
        assert !operands.isEmpty();

        operands = operands.toCollapsed();
        operands.getFirst().computeIntoBuffer(target, pool);
        assert numRows == target.getNumRows() && numCols == target.getNumCols();

        SymMatrixBuffer scratch = pool.lease0ContRowMaj(numRows, numCols);
        for (int i = 1; i < operands.size(); i++) {
            SymMatrixAdvancedExpr expr = operands.get(i);
            expr.computeIntoBuffer(scratch, pool);
            for (int r = 0; r < numRows; ++r) {
                int sRowIdx = r * scratch.getRowStride();
                int tRowIdx = r * target.getRowStride();

                for (int c = 0; c < numCols; ++c) {
                    int sIdx = sRowIdx + c * scratch.getColStride();
                    int tIdx = tRowIdx + c * target.getColStride();
                    target.unsafeGetRaw()[tIdx] = F.Plus(
                            target.unsafeGetRaw()[tIdx],
                            scratch.unsafeGetRaw()[sIdx]
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
    public @NonNull OperandTreeList getOperandsRef() {
        return operands;
    }

    @Override
    public void unsafeSetOperands(@NonNull OperandTreeList operandsWithSameVal) {
        this.operands = operandsWithSameVal;
    }

    private IllegalArgumentException illegalDimensionException(
            @NonNull SymMatrixAdvancedExpr expr
    ) {
        return illegalDimensionException(this, expr, "this", "expr");
    }
    private static IllegalArgumentException illegalDimensionException(
            @NonNull SymMatrixAdvancedExpr expr1,
            @NonNull SymMatrixAdvancedExpr expr2,
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
