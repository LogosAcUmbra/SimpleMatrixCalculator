package me.LogosAcUmbra.Matrix.SymMatrixExpr;

import me.LogosAcUmbra.Matrix.IMatrixBase;
import me.LogosAcUmbra.Matrix.OperandTree.IOperandList;
import org.jspecify.annotations.NonNull;
import org.matheclipse.core.interfaces.IExpr;

import java.util.List;

public interface ISymMatrixExpr extends IMatrixBase {


    default @NonNull ISymMatrixExpr negate() {
        return NegExpr.of(this); // recursive dependency marked
    }
    default @NonNull ISymMatrixExpr plus(@NonNull ISymMatrixExpr expr) {
        return SumExpr.of(this, expr); // recursive dependency marked
    }
    default @NonNull ISymMatrixExpr minus(@NonNull ISymMatrixExpr expr) {
        return SumExpr.ofMinus(this, expr); // recursive dependency marked
    }
    default @NonNull ISymMatrixExpr scale(@NonNull IExpr scalar) {
        return ScaleExpr.of(this, scalar); // recursive dependency marked
    }
    default @NonNull ISymMatrixExpr times(@NonNull ISymMatrixExpr expr) {
        return MulExpr.of(this, expr); // recursive dependency marked
    }

    @NonNull List<? extends @NonNull ISymMatrixExpr> getOperands();

//    @NonNull SymMatrix compute();
//
//    @NonNull SymMatrix eval();


}
