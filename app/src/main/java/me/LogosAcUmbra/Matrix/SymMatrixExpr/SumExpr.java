package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.LogosAcUmbra.Matrix.OperandTree.IOperandList;
import me.LogosAcUmbra.Matrix.OperandTree.OperandList;
import me.LogosAcUmbra.Matrix.SymMatrixBuffer;
import me.LogosAcUmbra.Matrix.SymMatrixBufferPool;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.matheclipse.core.expression.F;

import static me.LogosAcUmbra.Matrix.SymMatrixExpr.ISymMatrixAdvancedExpr.ensureIsAdvanced;

public class SumExpr implements IManyOperandExpr { // mutable

    final int numRows;
    final int numCols;
    @NonNull IOperandList operands;

    SumExpr(
            int numRows,
            int numCols,
            @NonNull IOperandList operands
    ) {
        // at least for now, SumExpr does not have cases that needs to hold 0 operands
        assert numRows >= 0 && numCols >= 0 && !operands.isEmpty();
        this.numRows = numRows;
        this.numCols = numCols;
        this.operands = operands;
    }

    public static SumExpr of(@NonNull ISymMatrixExpr expr) {
        ISymMatrixAdvancedExpr advExpr = ensureIsAdvanced(expr);
        if (advExpr instanceof SumExpr sumExpr) {
            return sumExpr;
        }
        return new SumExpr( advExpr.getNumRows(), advExpr.getNumCols(), OperandList.of(advExpr) );
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
        IOperandList operands = (
                (advExpr2 instanceof SumExpr sumExpr)
                ? (OperandList.Builder
                    .of(1 + sumExpr.operands.size())
                    .add(advExpr1)
                    .addAll(sumExpr.operands)
                    .build())
                : OperandList.of(advExpr1, advExpr2)
        );
        return new SumExpr(numRows, numCols, operands);
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
        IOperandList operands = OperandList.of(advExpr1, NegExpr.of(advExpr2));
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
    public @NonNull ISymMatrixExpr plus(@NonNull ISymMatrixExpr expr) {
        ISymMatrixAdvancedExpr advExpr =  ensureIsAdvanced(expr);
        if (advExpr.getNumRows() != numRows || advExpr.getNumCols() != numCols) {
            throw illegalDimensionException(advExpr);
        }
        if (advExpr instanceof SumExpr sumExpr) {
            return new SumExpr(  numRows, numCols, operands.withAll(sumExpr.operands)  );
        }
        return new SumExpr(  numRows, numCols, operands.with(advExpr)  );
    }

    @Override
    public @NonNull ISymMatrixExpr minus(@NonNull ISymMatrixExpr expr) {
        ISymMatrixAdvancedExpr advExpr =  ensureIsAdvanced(expr);
        if (expr.getNumRows() != numRows || expr.getNumCols() != numCols) {
            throw illegalDimensionException(advExpr);
        }
        return new SumExpr(  numRows, numCols, operands.with(NegExpr.of(advExpr)) );
    }

    // advanced

    public void computeIntoBuffer(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool pool) {
        // at least for now, sumExpr must have this.operands.size() >= 1
        assert !operands.isEmpty();

        ObjectArrayList<@NonNull ISymMatrixAdvancedExpr> ops = operands.getOperandsAndFlatten();
        ops.getFirst().computeIntoBuffer(target, pool);
        assert numRows == target.getNumRows() && numCols == target.getNumCols();

        SymMatrixBuffer scratch = pool.lease0ContRowMaj(numRows, numCols);
        for (int i = 1; i < operands.size(); i++) {
            ISymMatrixAdvancedExpr expr = operands.get(i);
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
    public @NonNull IOperandList getOperandsRef() {
        return operands;
    }

    @Override
    public @NonNull IManyOperandExpr unsafeSetOperands(@NonNull IOperandList operandsWithSameVal) {
        this.operands = operandsWithSameVal;
        return this;
    }

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
