package me.LogosAcUmbra.Matrix;

import org.jspecify.annotations.NonNull;
import org.matheclipse.core.interfaces.IExpr;

import java.util.List;

public interface ISymMatrixExpr extends IBaseSymMatrix {

    @NonNull ISymMatrixExpr plus(@NonNull ISymMatrixExpr expr);
    @NonNull ISymMatrixExpr minus(@NonNull ISymMatrixExpr expr);
    @NonNull ISymMatrixExpr scale(@NonNull IExpr scalar);
    @NonNull ISymMatrixExpr times(@NonNull ISymMatrixExpr expr);

    void evalInto(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool bufferPool);
    @NonNull List<ISymMatrixExpr> getOperands();
}
