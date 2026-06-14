package me.LogosAcUmbra.Matrix;

import me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrixAdvancedExpr;
import me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrixExpr;
import me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrix;
import me.LogosAcUmbra.Matrix.TreeOptimizer.TreeOptimizer;
import org.jspecify.annotations.NonNull;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;;

public class SymMatrixEvaluator {

    SymMatrixBufferPool bufferPool = new SymMatrixBufferPool();
    TreeOptimizer treeOptimizer = new TreeOptimizer();
    ExprEvaluator exprEvaluator = new ExprEvaluator();

    public SymMatrixEvaluator() {

    }

    public @NonNull SymMatrix eval(SymMatrixExpr matExpr) {
        return evalElems(evalToMat(matExpr));
    }

    public @NonNull SymMatrix evalToMat(@NonNull SymMatrixExpr matExpr) {
        SymMatrixAdvancedExpr advancedExpr = SymMatrixAdvancedExpr.ensureIsAdvanced(matExpr);
        advancedExpr = treeOptimizer.optimize(advancedExpr, exprEvaluator);
        SymMatrixBuffer target = bufferPool.lease0ContRowMaj(  advancedExpr.getNumRows(), advancedExpr.getNumCols()  );
        advancedExpr.computeIntoBuffer(target, bufferPool);
        SymMatrixBuffer toDump = target.unsafeTo0ContRowMaj(bufferPool);
        return toDump.unsafeToMatrix();
    }

    private @NonNull SymMatrix evalElems(@NonNull SymMatrix mat) { // TODO
        IExpr[] matInArr = new IExpr[mat.getNumRows() * mat.getNumCols()];
        for (int r = 0; r < mat.getNumRows(); r++) {
            for (int c = 0; c < mat.getNumCols(); c++) {
                matInArr[r * mat.getNumCols() + c] = exprEvaluator.eval(mat.get(r, c));
            }
        }
        return SymMatrix.unsafeOf0ContRowMaj(matInArr, mat.getNumRows(), mat.getNumCols());
    }
}
