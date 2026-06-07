package me.LogosAcUmbra.Matrix;

import org.jspecify.annotations.NonNull;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;;

public class SymMatrixEvaluator {

    SymMatrixBufferPool bufferPool = new SymMatrixBufferPool();
    ExprEvaluator exprEvaluator = new ExprEvaluator();

    public SymMatrixEvaluator() {

    }

    public @NonNull SymMatrix eval(ISymMatrixExpr matExpr) {
        return evalElems(evalToMat(matExpr));
    }

    public @NonNull SymMatrix evalToMat(ISymMatrixExpr matExpr) {
        switch (matExpr) {
            case ()
        }
    }

    private @NonNull SymMatrix evalElems(@NonNull SymMatrix mat) {
        IExpr[] matInArr = new IExpr[mat.raw.length];
        for (int i = 0; i < matInArr.length; i++) {
            matInArr[i] = exprEvaluator.eval(mat.raw[i]);
        }
        return new SymMatrix(
                matInArr,
                mat.numRows, mat.numCols, mat.offset, mat.rowStride, mat.colStride);
    }
}
