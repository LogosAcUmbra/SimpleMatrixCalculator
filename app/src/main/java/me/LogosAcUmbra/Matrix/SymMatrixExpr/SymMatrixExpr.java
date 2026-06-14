package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import me.LogosAcUmbra.Matrix.MatrixBase;
import org.jspecify.annotations.NonNull;
import org.matheclipse.core.interfaces.IExpr;

import java.util.List;

public interface SymMatrixExpr extends MatrixBase {


    default @NonNull SymMatrixExpr negate() {
        return NegExpr.of(this); // recursive dependency marked
    }
    default @NonNull SymMatrixExpr plus(@NonNull SymMatrixExpr expr) {
        return SumExpr.of(this, expr); // recursive dependency marked
    }
    default @NonNull SymMatrixExpr minus(@NonNull SymMatrixExpr expr) {
        return SumExpr.ofMinus(this, expr); // recursive dependency marked
    }
    default @NonNull SymMatrixExpr scale(@NonNull IExpr scalar) {
        return ScaleExpr.of(this, scalar); // recursive dependency marked
    }
    default @NonNull SymMatrixExpr times(@NonNull SymMatrixExpr expr) {
        return MulExpr.of(this, expr); // recursive dependency marked
    }

    @NonNull List<? extends @NonNull SymMatrixExpr> getOperands();

//    @NonNull SymMatrix compute();
//
//    @NonNull SymMatrix eval();


}
