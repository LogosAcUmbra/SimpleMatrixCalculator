package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import org.jspecify.annotations.NonNull;

import java.util.List;

public interface ConstOperandExpr extends SymMatrixAdvancedExpr {

    boolean isZero();
    boolean isIdentity();

    @Override
    default @NonNull List<? extends @NonNull SymMatrixExpr> getOperands() {
        return List.of();
    }
}
