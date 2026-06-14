package me.LogosAcUmbra.Matrix.TreeOptimizer;

import me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrixAdvancedExpr;
import org.jspecify.annotations.NonNull;
import org.matheclipse.core.eval.ExprEvaluator;

import java.util.ArrayList;
import java.util.List;

public class TreeOptimizer {

    private final List<OptimizationPass> pipeline;

    // default pipelines
    public TreeOptimizer() {
        this.pipeline = new ArrayList<>();
        this.pipeline.add(new ScalePass());
    }
    public TreeOptimizer(@NonNull List<OptimizationPass> pipeline) {
        this.pipeline = pipeline;
    }

    public void registerPass(OptimizationPass customPass) {
        pipeline.add(customPass);
    }

    public SymMatrixAdvancedExpr optimize(@NonNull SymMatrixAdvancedExpr root, @NonNull ExprEvaluator symjaExprEvaluator) {
        for (int i = 0; i < pipeline.size(); i++) {
            root = pipeline.get(i).apply(root, symjaExprEvaluator);
        }
        return root;
    }


}
