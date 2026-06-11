package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import me.LogosAcUmbra.Matrix.SymMatrixBuffer;
import me.LogosAcUmbra.Matrix.SymMatrixBufferPool;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static me.LogosAcUmbra.Matrix.SymMatrixExpr.ISymMatrixAdvancedExpr.ensureIsAdvanced;


public class NegExpr implements IOneOperandExpr {

    private final int numRows;
    private final int numCols;
    private @NonNull ISymMatrixAdvancedExpr operand;

    private NegExpr(@NonNull ISymMatrixAdvancedExpr operand) {
        this.numRows = operand.getNumRows();
        this.numCols = operand.getNumCols();
        this.operand = operand;
    }

    public static NegExpr of(@NonNull ISymMatrixExpr operand) {
        ISymMatrixAdvancedExpr advOperand = ensureIsAdvanced(operand);
        return new NegExpr(advOperand);
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
    public @NonNull ISymMatrixExpr negate() {
        return operand;
    }

    @Override
    public @NonNull List<ISymMatrixExpr> getOperands() {
        return List.of(operand);
    }


    @Override
    public @NonNull ISymMatrixAdvancedExpr getOperandRef() {
        return operand;
    }

    @Override
    public @NonNull NegExpr unsafeSetOperand(@NonNull ISymMatrixAdvancedExpr operandWithSameVal) {
        this.operand = operandWithSameVal;
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
