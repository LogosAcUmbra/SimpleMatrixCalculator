//package me.LogosAcUmbra.Matrix.OperandTree;
//
//import me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrixAdvancedExpr;
//import me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrixExpr;
//import org.jspecify.annotations.NonNull;
//
//import java.util.Collection;
//import java.util.List;
//
//public interface OperandList extends Collection<SymMatrixAdvancedExpr> {
//
//
//    @NonNull OperandListBuilder toBuilder();
//
//    int size();
//    boolean isEmpty();
//
//    @NonNull OperandList with(@NonNull SymMatrixAdvancedExpr newOperand);
//
//    /**
//     * @param newOperands the new operands to be appended at the back <br>
//     *                 [Ownership Transferal] the given newOperands will be directly used
//     *                 as the internal array of the new instance
//     * @return a new {@link OperandList} containing previous operands and the given new operands
//     */
//    @NonNull OperandList withAll(@NonNull SymMatrixAdvancedExpr... newOperands);
//
//    @NonNull OperandList withAll(@NonNull Collection<? extends SymMatrixAdvancedExpr> collection);
//
//    @NonNull OperandList withAll(@NonNull OperandList other);
//
//    @NonNull OperandList subList(int from, int to);
//
//    @NonNull SymMatrixAdvancedExpr get(int index) throws IndexOutOfBoundsException;
//    @NonNull SymMatrixAdvancedExpr getFirst();
//    @NonNull SymMatrixAdvancedExpr getLast();
//
//    @NonNull SymMatrixAdvancedExpr @NonNull [] getOperands();
//    @NonNull OperandList toCollapsed();
//    @NonNull OperandList toCollapsedAndUpdateParent();
//
//
//    /**
//     *
//     * @return a not modifiable (cannot add, remove, set elements) <br>
//     * and not advanced (each element is immutable without down casting) <br>
//     * list of operands stored
//     */
//    @NonNull List<? extends @NonNull SymMatrixExpr> getOperandsNotAdvanced();
//
//    interface Unsafe extends OperandList {
//
//        void setElem(int index, @NonNull SymMatrixAdvancedExpr exprWithSameVal);
//
//        /**
//         * Use this method when you're only reading the elements
//         * @return fastest way of getting array of operands, MAYBE a DIRECT REFERENCE to internal array
//         */
//        @NonNull SymMatrixAdvancedExpr @NonNull [] getOperandsFast();
//
//    }
//}
