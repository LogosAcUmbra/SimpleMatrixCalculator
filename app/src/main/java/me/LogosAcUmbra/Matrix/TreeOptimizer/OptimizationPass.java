package me.LogosAcUmbra.Matrix.TreeOptimizer;

import me.LogosAcUmbra.Matrix.ISymMatrixExpr;
import org.jspecify.annotations.NonNull;

public interface OptimizationPass {
    @NonNull ISymMatrixExpr apply(@NonNull ISymMatrixExpr root);
}
