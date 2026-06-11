package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import me.LogosAcUmbra.Matrix.OperandTree.IOperandList;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface IManyOperandExpr extends ISymMatrixAdvancedExpr {
    @NonNull IOperandList getOperandsRef();

    @NonNull IManyOperandExpr unsafeSetOperands(@NonNull IOperandList operandsWithSameVal);

    @Override
    default @NonNull List<? extends @NonNull ISymMatrixExpr> getOperands() {
        return getOperandsRef().getOperandsNotAdvanced();
    }
}
