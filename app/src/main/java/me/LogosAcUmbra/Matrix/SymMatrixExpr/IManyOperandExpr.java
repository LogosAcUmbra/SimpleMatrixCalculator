package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import me.LogosAcUmbra.Matrix.OperandTree.IOperandList;
import org.jspecify.annotations.NonNull;

public interface IManyOperandExpr extends ISymMatrixAdvancedExpr {
    @NonNull IOperandList getOperandsRef();

    @NonNull IOneOperandExpr unsafeSetOperands(@NonNull IOperandList operandsWithSameVal);

    @Override
    @NonNull IManyOperandExpr setParent(@NonNull ISymMatrixExpr parent);
}
