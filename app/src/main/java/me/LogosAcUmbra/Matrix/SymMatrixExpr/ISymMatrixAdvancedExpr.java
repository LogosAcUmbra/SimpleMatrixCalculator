package me.LogosAcUmbra.Matrix;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface ISymMatrixAdvancedExpr extends ISymMatrixExpr {
    /**
     * Compute directly into the given pre-allocated target buffer,
     * complying with the layout of the given target buffer
     *
     * @param target the pre-allocated target buffer
     * @param pool the buffer pool
     */
    void computeIntoBuffer(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool pool);

    /**
     * Compute into a SymMatrixBuffer instance
     *
     * @param pool the buffer pool
     * @return the resulting computed buffer
     */
    default @NonNull SymMatrixBuffer computeToBuffer(@NonNull SymMatrixBufferPool pool) {
        // default: use 0ContRowMaj layout
        SymMatrixBuffer target = pool.lease0ContRowMaj(this.getNumRows(), this.getNumCols());
        this.computeIntoBuffer(target, pool);
        return target;
    }

    @Nullable ISymMatrixExpr getParent();
    @NonNull ISymMatrixExpr setParent(@NonNull ISymMatrixExpr parent);

    static @NonNull ISymMatrixAdvancedExpr ensureIsAdvanced(ISymMatrixExpr expr) {
        if (expr instanceof ISymMatrixAdvancedExpr advancedExpr) {
            return advancedExpr;
        }
        throw new IllegalArgumentException(
                "any ISymMatrixExpr instance(" + expr + ") must implement ISymMatrixAdvancedExpr"
        );
    }

}
