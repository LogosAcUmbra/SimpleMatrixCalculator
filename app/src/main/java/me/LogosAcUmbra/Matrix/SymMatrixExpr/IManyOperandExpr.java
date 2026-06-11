package me.LogosAcUmbra.Matrix;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NonNull;

public interface IManyOperandExpr extends ISymMatrixAdvancedExpr {
    @NonNull ObjectArrayList<ISymMatrixAdvancedExpr> getOperandsRef();
//    @NonNull IManyOperandExpr setOperands(@NonNull ISymMatrixExpr @NonNull [] operands);

    @Override
    @NonNull IManyOperandExpr setParent(@NonNull ISymMatrixExpr parent);
}
