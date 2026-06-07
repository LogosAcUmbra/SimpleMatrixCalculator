package me.LogosAcUmbra.Matrix.TreeOptimizer;

import java.util.ArrayList;
import java.util.List;

public class TreeOptimizer {

    private final List<OptimizationPass> pipeline = new ArrayList<>();

    // default pipelines
    public TreeOptimizer() {
        pipeline.add(new ScalePass());
    }


}
