package me.LogosAcUmbra.Matrix.TreeOptimizer;

import me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrixAdvancedExpr;
import org.matheclipse.core.eval.ExprEvaluator;
import org.jspecify.annotations.NonNull;

public interface OptimizationPass {
    @NonNull SymMatrixAdvancedExpr apply(@NonNull SymMatrixAdvancedExpr root, @NonNull ExprEvaluator symjaExprEvaluator);
}
