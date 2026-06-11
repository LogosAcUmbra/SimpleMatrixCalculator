package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import me.LogosAcUmbra.Matrix.OperandTree.IOperandList;
import me.LogosAcUmbra.Matrix.OperandTree.OperandList;
import me.LogosAcUmbra.Matrix.SymMatrixBuffer;
import me.LogosAcUmbra.Matrix.SymMatrixBufferPool;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static me.LogosAcUmbra.Matrix.SymMatrixExpr.ISymMatrixAdvancedExpr.ensureIsAdvanced;

public class MulExpr implements IManyOperandExpr {

    final int numRows;
    final int numCols;
    @NonNull IOperandList operands;

    MulExpr(int numRows, int numCols, @NonNull IOperandList operands) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.operands = operands;
    }

    public static MulExpr of(@NonNull ISymMatrixExpr expr1, @NonNull ISymMatrixExpr expr2) {
        ISymMatrixAdvancedExpr advExpr1 = ensureIsAdvanced(expr1);
        ISymMatrixAdvancedExpr advExpr2 = ensureIsAdvanced(expr2);
        assert (advExpr1 instanceof MulExpr);
        if (advExpr1.getNumCols() != advExpr2.getNumRows()) {
            throw illegalMulDimension(advExpr1, advExpr2, "expr1", "expr2");
        }
        int numRows = advExpr1.getNumRows();
        int numCols = advExpr2.getNumCols();
        if (advExpr2 instanceof MulExpr mulExpr) {
            int size = mulExpr.operands.size();
            IOperandList operands
                    = new OperandList.Builder(1 + size)
                    .add(advExpr1)
                    .addAll(mulExpr.operands.getOperands())
                    .build();
            return new MulExpr(numRows, numCols, operands);
        }
        return new MulExpr(numRows, numCols, OperandList.of(advExpr1, advExpr2));
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
    public @NonNull ISymMatrixExpr times(@NonNull ISymMatrixExpr expr) {
        var advExpr = ensureIsAdvanced(expr);
        if (this.numCols != advExpr.getNumRows()) {
            throw illegalMulDimension(this, advExpr, "this", "expr");
        }
        return new MulExpr(numRows, advExpr.getNumCols(), operands.with(advExpr));
    }

    @Override
    public @NonNull List<? extends @NonNull ISymMatrixExpr> getOperands() {
        return operands.getOperandsNotAdvanced();
    }

    @Override
    public @NonNull IOperandList getOperandsRef() {
        return operands;
    }

    protected static IllegalArgumentException illegalMulDimension(ISymMatrixExpr expr1, ISymMatrixExpr expr2, String expr1Name, String expr2Name) {
        return new IllegalArgumentException(String.format(
                "illegal dimensions: %s.numCols(%d) != %s.numRows(%d), Infos: { %s: %s, %s: %s }",
                expr1Name, expr1.getNumCols(), expr2Name, expr2.getNumRows(),
                expr1Name, expr1, expr2Name, expr2
        ));
    }

    @Override
    public @NonNull MulExpr unsafeSetOperands(@NonNull IOperandList operandsWithSameVal) {
        this.operands = operandsWithSameVal;
        return this;
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
