package me.LogosAcUmbra.Matrix;

import org.jspecify.annotations.NonNull;
import org.matheclipse.core.interfaces.IExpr;

public class NegExpr implements ISymMatrixExpr {

    @NonNull ISymMatrixExpr term;

    NegExpr(@NonNull ISymMatrixExpr term) {
        this.term = term;
    }

    static NegExpr of(@NonNull ISymMatrixExpr term) {
        return new NegExpr(term);
    }

    @Override
    public int getNumRows() {
        return term.getNumRows();
    }

    @Override
    public int getNumCols() {
        return term.getNumCols();
    }

    @Override
    public @NonNull ISymMatrixExpr plus(@NonNull ISymMatrixExpr expr) {
        return SumExpr.of(this, expr);
    }

    @Override
    public @NonNull ISymMatrixExpr minus(@NonNull ISymMatrixExpr expr) {
        return SumExpr.ofMinus(this, expr);
    }

    @Override
    public @NonNull ISymMatrixExpr scale(@NonNull IExpr scalar) {
        return ScaleExpr.of(this, scalar);
    }

    @Override
    public @NonNull ISymMatrixExpr times(@NonNull ISymMatrixExpr expr) {
        return MulExpr.of(this, expr);
    }
}
