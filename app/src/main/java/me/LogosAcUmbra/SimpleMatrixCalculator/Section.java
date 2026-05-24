package me.LogosAcUmbra.SimpleMatrixCalculator;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import org.ejml.data.DMatrixRMaj;
import org.jspecify.annotations.NonNull;

public class Section {
    private final HashMap<@NonNull String, @NonNull DMatrixRMaj> matrices = new HashMap<>();
    private String defaultMatrixName = "T";

    boolean isMatNameNotUsed(@NonNull String matName) {
        return matrices.containsKey(matName);
    }

    String getUnusedDefaultName() {
        String name = defaultMatrixName;
        if (!matrices.containsKey(name)) {
            return name;
        }
        int i = 1;
        while (true) {
            name = defaultMatrixName + i;
            if (!matrices.containsKey(name)) {
                return name;
            }
            ++i;
        }
    }

    DMatrixRMaj addMatrix(@NonNull String matName, int r, int c) {
        DMatrixRMaj mat = new DMatrixRMaj(r, c);
        matrices.put(matName, mat);
        return mat;
    }
    boolean setMatElem(@NonNull String matName, int r, int c, double elem) {
        DMatrixRMaj mat = matrices.get(matName);
        if (Objects.isNull(mat)) {
            return false;
        }
        mat.set(r, c, elem);
        return true;
    }
    Optional<DMatrixRMaj> getMat(@NonNull String matName) {
        return Optional.ofNullable(matrices.get(matName));
    }
    int getNumMatrices() {
        return matrices.size();
    }
}
