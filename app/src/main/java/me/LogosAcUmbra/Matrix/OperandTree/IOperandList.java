package me.LogosAcUmbra.Matrix.OperandTree;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.LogosAcUmbra.Matrix.SymMatrixExpr.ISymMatrixAdvancedExpr;
import me.LogosAcUmbra.Matrix.SymMatrixExpr.ISymMatrixExpr;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface IOperandList {

    int size();
    boolean isEmpty();

    @NonNull IOperandList with(@NonNull ISymMatrixAdvancedExpr expr);
    @NonNull IOperandList withAll(@NonNull ISymMatrixAdvancedExpr... args);
    @NonNull IOperandList withAll(@NonNull ObjectArrayList<@NonNull ISymMatrixAdvancedExpr> objArrList);
    @NonNull IOperandList withAll(@NonNull IOperandList other);

    @NonNull IOperandList subList(int fromIndex, int toIndex);

    @NonNull ISymMatrixAdvancedExpr get(int index);

    @NonNull ObjectArrayList<@NonNull ISymMatrixAdvancedExpr> getOperands();
    @NonNull ObjectArrayList<@NonNull ISymMatrixAdvancedExpr> getOperandsAndFlatten();

    @NonNull IOperandList unsafeSetElem(int index, @NonNull ISymMatrixAdvancedExpr exprWithSameVal);

    @NonNull List<? extends @NonNull ISymMatrixExpr> getOperandsNotAdvanced();
}
