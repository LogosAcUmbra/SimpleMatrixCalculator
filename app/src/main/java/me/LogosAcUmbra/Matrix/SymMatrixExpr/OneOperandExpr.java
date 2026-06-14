package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface OneOperandExpr extends SymMatrixAdvancedExpr {

    @NonNull SymMatrixAdvancedExpr getOperandRef();

    @Nullable OneOperandExpr unsafeSetOperand(@NonNull SymMatrixAdvancedExpr operandWithSameVal);

    @Override
    default @NonNull List<? extends @NonNull SymMatrixExpr> getOperands() {
        return List.of(  getOperandRef()  );
    }
}
