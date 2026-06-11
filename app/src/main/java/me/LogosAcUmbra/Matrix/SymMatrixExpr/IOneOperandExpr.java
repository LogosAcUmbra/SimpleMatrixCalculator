package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface IOneOperandExpr extends ISymMatrixAdvancedExpr {

    @NonNull ISymMatrixAdvancedExpr getOperandRef();

    @Nullable IOneOperandExpr unsafeSetOperand(@NonNull ISymMatrixAdvancedExpr operandWithSameVal);

    @Override
    default @NonNull List<? extends @NonNull ISymMatrixExpr> getOperands() {
        return List.of(  getOperandRef()  );
    }
}
