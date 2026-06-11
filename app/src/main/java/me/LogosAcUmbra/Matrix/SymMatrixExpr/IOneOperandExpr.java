package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface IOneOperandExpr extends ISymMatrixAdvancedExpr {

    @NonNull ISymMatrixExpr getOperandRef();

    @Nullable IOneOperandExpr unsafeSetOperand(@NonNull ISymMatrixExpr operandWithSameVal);

    @Override
    @NonNull IOneOperandExpr setParent(@NonNull ISymMatrixExpr parent);

}
