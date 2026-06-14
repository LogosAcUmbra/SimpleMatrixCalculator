package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import me.LogosAcUmbra.Matrix.OperandTree.OperandTreeList;
import me.LogosAcUmbra.Matrix.SymMatrixBuffer;
import me.LogosAcUmbra.Matrix.SymMatrixBufferPool;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrixAdvancedExpr.ensureIsAdvanced;

public class MulExpr implements ManyOperandExpr {

    final int numRows;
    final int numCols;
    @NonNull OperandTreeList operands;

    MulExpr(int numRows, int numCols, @NonNull OperandTreeList operands) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.operands = operands;
    }

    public static MulExpr of(@NonNull SymMatrixExpr expr1, @NonNull SymMatrixExpr expr2) {
        SymMatrixAdvancedExpr advExpr1 = ensureIsAdvanced(expr1);
        SymMatrixAdvancedExpr advExpr2 = ensureIsAdvanced(expr2);
        assert (advExpr1 instanceof MulExpr);
        if (advExpr1.getNumCols() != advExpr2.getNumRows()) {
            throw illegalMulDimension(advExpr1, advExpr2, "expr1", "expr2");
        }
        int numRows = advExpr1.getNumRows();
        int numCols = advExpr2.getNumCols();
        if (advExpr2 instanceof MulExpr mulExpr) {
            int size = mulExpr.operands.size();
            OperandTreeList operands
                    = OperandTreeList.builder(1 + size)
                    .add(advExpr1)
                    .addAll(mulExpr.operands)
                    .build();
            return new MulExpr(numRows, numCols, operands);
        }
        return new MulExpr(numRows, numCols, OperandTreeList.of(advExpr1, advExpr2));
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
    public @NonNull SymMatrixExpr times(@NonNull SymMatrixExpr expr) {
        var advExpr = ensureIsAdvanced(expr);
        if (this.numCols != advExpr.getNumRows()) {
            throw illegalMulDimension(this, advExpr, "this", "expr");
        }
        return new MulExpr(numRows, advExpr.getNumCols(), operands.with(advExpr));
    }

    @Override
    public @NonNull List<? extends @NonNull SymMatrixExpr> getOperands() {
        return operands.getOperandsNotAdvanced();
    }

    @Override
    public @NonNull OperandTreeList getOperandsRef() {
        return operands;
    }

    protected static IllegalArgumentException illegalMulDimension(SymMatrixExpr expr1, SymMatrixExpr expr2, String expr1Name, String expr2Name) {
        return new IllegalArgumentException(String.format(
                "illegal dimensions: %s.numCols(%d) != %s.numRows(%d), Infos: { %s: %s, %s: %s }",
                expr1Name, expr1.getNumCols(), expr2Name, expr2.getNumRows(),
                expr1Name, expr1, expr2Name, expr2
        ));
    }

    @Override
    public void unsafeSetOperands(@NonNull OperandTreeList operandsWithSameVal) {
        this.operands = operandsWithSameVal;
    }

    @Override
    public void computeIntoBuffer(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool pool) {
        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull SymMatrixBuffer computeToBuffer(@NonNull SymMatrixBufferPool pool) {
        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }

}
