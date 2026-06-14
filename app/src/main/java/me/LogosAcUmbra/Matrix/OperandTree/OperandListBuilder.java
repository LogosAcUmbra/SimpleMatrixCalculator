//package me.LogosAcUmbra.Matrix.OperandTree;
//
//import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//import me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrixAdvancedExpr;
//import org.jspecify.annotations.NonNull;
//
//import java.util.Collection;
//
//public interface OperandListBuilder{
//
//    @NonNull OperandList build();
//
//    @NonNull OperandListBuilder ensureCapacity(int capacity);
//
//    @NonNull OperandListBuilder setOperands(@NonNull OperandList operandList);
//
//    @NonNull OperandListBuilder setOperands(@NonNull SymMatrixAdvancedExpr... operands);
//
//    @NonNull OperandListBuilder setOperands(@NonNull Collection<? extends SymMatrixAdvancedExpr> collection);
//
//    @NonNull OperandListBuilder setOperands(@NonNull ObjectArrayList<@NonNull SymMatrixAdvancedExpr> operands);
//
//    @NonNull OperandListBuilder add(@NonNull SymMatrixAdvancedExpr expr);
//
//    @NonNull OperandListBuilder add(int index, @NonNull SymMatrixAdvancedExpr expr);
//
//    @NonNull OperandListBuilder addAll(OperandList operandList);
//
//    @NonNull OperandListBuilder addAll(int index, OperandList operandList);
//
//    @NonNull OperandListBuilder addAll(int index, final Collection<? extends SymMatrixAdvancedExpr> collection);
//
//    @NonNull OperandListBuilder addAll(final Collection<? extends SymMatrixAdvancedExpr> collection);
//
//    @NonNull OperandListBuilder set(int idx, @NonNull SymMatrixAdvancedExpr expr);
//
////    @NonNull OperandListBuilder remove(int idx);
////
////    @NonNull OperandListBuilder remove(@NonNull SymMatrixAdvancedExpr expr);
//}
