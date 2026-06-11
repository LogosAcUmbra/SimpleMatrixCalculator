package me.LogosAcUmbra.Matrix.OperandTree;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.LogosAcUmbra.Matrix.SymMatrixExpr.ISymMatrixAdvancedExpr;
import org.jspecify.annotations.NonNull;

public interface IOperandList {

    int size();
    boolean isEmpty();

    @NonNull IOperandList add(@NonNull ISymMatrixAdvancedExpr expr);
    @NonNull IOperandList addAll(@NonNull ISymMatrixAdvancedExpr... args);
    @NonNull IOperandList addAll(@NonNull ObjectArrayList<@NonNull ISymMatrixAdvancedExpr> objArrList);
    @NonNull IOperandList addAll(@NonNull IOperandList other);

    @NonNull IOperandList subList(int fromIndex, int toIndex);

    @NonNull ISymMatrixAdvancedExpr get(int index);

    @NonNull ObjectArrayList<@NonNull ISymMatrixAdvancedExpr> getOperands();
    @NonNull ObjectArrayList<@NonNull ISymMatrixAdvancedExpr> getOperandsAndFlatten();

}
