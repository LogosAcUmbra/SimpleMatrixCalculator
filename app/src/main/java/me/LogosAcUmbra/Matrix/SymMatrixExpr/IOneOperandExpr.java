package me.LogosAcUmbra.Matrix;

import org.jspecify.annotations.NonNull;

public interface IOneOperandExpr extends ISymMatrixAdvancedExpr {

    @NonNull ISymMatrixExpr getOperandRef();

    @NonNull IOneOperandExpr immutSetOperand(@NonNull ISymMatrixExpr operand);

    @Override
    @NonNull IOneOperandExpr setParent(@NonNull ISymMatrixExpr parent);

}
