package me.LogosAcUmbra.trialCalculator;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import org.ejml.data.DMatrixRMaj;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Section {
    private final HashMap<@NonNull String, @NonNull DMatrixRMaj> matrices = new HashMap<>();

    protected boolean isMatNameNotUsed(@NonNull String matName) {
        return Objects.isNull(matrices.get(matName));
    }
    protected void addMatrix(@NonNull String matName, @NonNull DMatrixRMaj mat) {
        matrices.put(matName, mat);
    }
    protected void addMatrix(@NonNull String matName, int r, int c) {
        DMatrixRMaj mat = new DMatrixRMaj(r, c);
        matrices.put(matName, mat);
    }
    protected boolean setMatElem(@NonNull String matName, int r, int c, double elem) {
        DMatrixRMaj mat = matrices.get(matName);
        if (Objects.isNull(mat)) {
            return false;
        }
        mat.set(r, c, elem);
        return true;
    }
    protected Optional<DMatrixRMaj> getMat(@NonNull String matName) {
        return Optional.ofNullable(matrices.get(matName));
    }
    protected int getNumMatrices() {
        return matrices.size();
    }
}
