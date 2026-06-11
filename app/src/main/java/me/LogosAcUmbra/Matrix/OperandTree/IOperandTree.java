package me.LogosAcUmbra.Matrix.OperandTree;

import me.LogosAcUmbra.Matrix.SymMatrixExpr.ISymMatrixAdvancedExpr;
import org.jspecify.annotations.NonNull;

public sealed interface IOperandTree extends IOperandList permits OperandTreeLeaf {

    // TODO

    @Override
    @NonNull
    IOperandTree add(@NonNull ISymMatrixAdvancedExpr expr);
    @Override
    @NonNull
    IOperandTree addAll(@NonNull ISymMatrixAdvancedExpr... args);

    IOperandTree addAll(@NonNull IOperandTree other);

}
