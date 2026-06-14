package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import me.LogosAcUmbra.Matrix.OperandTree.OperandTreeList;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface ManyOperandExpr extends SymMatrixAdvancedExpr {
    @NonNull OperandTreeList getOperandsRef();

    void unsafeSetOperands(@NonNull OperandTreeList operandsWithSameVal);

    @Override
    default @NonNull List<? extends @NonNull SymMatrixExpr> getOperands() {
        return getOperandsRef().getOperandsNotAdvanced();
    }
}
