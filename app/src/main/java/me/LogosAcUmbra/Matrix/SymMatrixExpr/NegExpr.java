package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import me.LogosAcUmbra.Matrix.SymMatrixBuffer;
import me.LogosAcUmbra.Matrix.SymMatrixBufferPool;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrixAdvancedExpr.ensureIsAdvanced;


public class NegExpr implements OneOperandExpr {

    private final int numRows;
    private final int numCols;
    private @NonNull SymMatrixAdvancedExpr operand;

    private NegExpr(@NonNull SymMatrixAdvancedExpr operand) {
        this.numRows = operand.getNumRows();
        this.numCols = operand.getNumCols();
        this.operand = operand;
    }

    public static NegExpr of(@NonNull SymMatrixExpr operand) {
        SymMatrixAdvancedExpr advOperand = ensureIsAdvanced(operand);
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
    public @NonNull SymMatrixExpr negate() {
        return operand;
    }

    @Override
    public @NonNull List<SymMatrixExpr> getOperands() {
        return List.of(operand);
    }


    @Override
    public @NonNull SymMatrixAdvancedExpr getOperandRef() {
        return operand;
    }

    @Override
    public @NonNull NegExpr unsafeSetOperand(@NonNull SymMatrixAdvancedExpr operandWithSameVal) {
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
