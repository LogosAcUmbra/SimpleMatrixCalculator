package me.LogosAcUmbra.Matrix.OperandTree;

import me.LogosAcUmbra.Matrix.SymMatrixExpr.ISymMatrixAdvancedExpr;
import org.jspecify.annotations.NonNull;

public sealed interface IOperandTree extends IOperandList permits OperandTreeLeaf {

    // TODO

    @Override
    @NonNull
    IOperandTree with(@NonNull ISymMatrixAdvancedExpr expr);
    @Override
    @NonNull
    IOperandTree withAll(@NonNull ISymMatrixAdvancedExpr... args);

    IOperandTree withAll(@NonNull IOperandTree other);

}
