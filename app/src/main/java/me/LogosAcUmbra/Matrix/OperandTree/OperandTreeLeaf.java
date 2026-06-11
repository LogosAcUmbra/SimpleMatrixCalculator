package me.LogosAcUmbra.Matrix.OperandTree;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.LogosAcUmbra.Matrix.SymMatrixExpr.ISymMatrixAdvancedExpr;
import org.jspecify.annotations.NonNull;

final class OperandTreeLeaf implements IOperandTree {
    // TODO

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public @NonNull IOperandTree add(@NonNull ISymMatrixAdvancedExpr expr) {
        return null;
    }

    @Override
    public @NonNull IOperandTree addAll(@NonNull ISymMatrixAdvancedExpr... args) {
        return null;
    }

    @Override
    public @NonNull IOperandList addAll(@NonNull IOperandList other) {
        return null;
    }

    @Override
    public @NonNull IOperandList subList(int fromIndex, int toIndex) {
        return null;
    }

    @Override
    public @NonNull ObjectArrayList<ISymMatrixAdvancedExpr> getOperands() {
        return null;
    }

    @Override
    public @NonNull ObjectArrayList<ISymMatrixAdvancedExpr> getOperandsAndFlatten() {
        return null;
    }

    @Override
    public IOperandTree addAll(@NonNull IOperandTree other) {
        return null;
    }
    // TODO
}
