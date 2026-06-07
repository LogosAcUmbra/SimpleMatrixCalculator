package me.LogosAcUmbra.Matrix;

import org.jspecify.annotations.NonNull;

// package private
abstract class BaseSymMatrixExpr implements ISymMatrixExpr {
    protected abstract @NonNull SymMatrixBuffer evalInternal(@NonNull SymMatrixBuffer matrix);
}
