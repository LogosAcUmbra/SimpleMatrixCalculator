package me.LogosAcUmbra.Matrix;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class NegExpr implements IOneOperandExpr {

    private @Nullable ISymMatrixExpr parent;
    private final int numRows;
    private final int numCols;
    private final @NonNull ISymMatrixExpr operand;

    private NegExpr(@NonNull ISymMatrixExpr operand) {
        this.numRows = operand.getNumRows();
        this.numCols = operand.getNumCols();
        this.operand = operand;
    }

    public static NegExpr of(@NonNull ISymMatrixExpr operand) {
        return new NegExpr(operand);
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
    public @Nullable ISymMatrixExpr getParent() {
        return parent;
    }

    @Override
    public @NonNull NegExpr setParent(@NonNull ISymMatrixExpr parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public @NonNull ISymMatrixExpr getOperandRef() {
        return operand;
    }

    @Override
    public @NonNull NegExpr immutSetOperand(@NonNull ISymMatrixExpr operand) {
        return new NegExpr(operand);
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
