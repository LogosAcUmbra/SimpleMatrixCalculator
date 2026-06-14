//package me.LogosAcUmbra.Matrix.OperandTree;
//
//
//import it.unimi.dsi.fastutil.ints.IntObjectPair;
//import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//import me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrixAdvancedExpr;
//import me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrixExpr;
//import org.jspecify.annotations.NonNull;
//
//import java.util.*;
//
//
///**
// * <p><b>Method Guidance:</b>
// * <ul>
// *   <li>Use {@link #toCollapsed()} at major execution boundaries (e.g., prior to
// *       buffered evaluation or multithreaded processing) to collapse the reverse-links
// *       into a contiguous array for peak CPU cache performance.</li>
// * </ul>
// */
//public class OperandReversedLinkedList implements OperandList, OperandList.Unsafe {
//
//
//    public static final @NonNull OperandReversedLinkedList ROOT = createRoot();
//
//    private @NonNull OperandReversedLinkedList prev;
//    private @NonNull SymMatrixAdvancedExpr @NonNull [] segment;
//    private final int numOperands;
//
//    private OperandReversedLinkedList(
//            @NonNull OperandReversedLinkedList prev,
//            @NonNull SymMatrixAdvancedExpr @NonNull [] segment,
//            int numOperands
//    ) {
//        this.prev = prev;
//        this.segment = segment;
//        this.numOperands = numOperands;
//    }
//
//    @SuppressWarnings({"ConstantConditions", "DataFlowIssue"})
//    private static OperandReversedLinkedList createRoot() {
//        // it is the only case (root) that we need to pass null into the prev argument of constructor
//        OperandReversedLinkedList root = new OperandReversedLinkedList(
//                (OperandReversedLinkedList) null, new SymMatrixAdvancedExpr[0], 0);
//        root.prev = root;
//        return root;
//    }
//
//    public static OperandReversedLinkedList of(@NonNull Collection<? extends @NonNull SymMatrixAdvancedExpr> operands) {
//        if (operands.isEmpty()) {  return ROOT;  }
//        return new OperandReversedLinkedList(ROOT, operands.toArray(SymMatrixAdvancedExpr[]::new), operands.size());
//    }
//
//    /**
//     * @param operands the operands <br>
//     *                 [Ownership Transferal] the given operands will be directly used
//     *                 as the internal array of the new instance
//     * @return a new {@link OperandReversedLinkedList} instance containing only the given operands
//     */
//    public static OperandReversedLinkedList of(@NonNull SymMatrixAdvancedExpr... operands) {
//        return new OperandReversedLinkedList(ROOT, operands, operands.length);
//    }
//
//    @Override
//    public int size() {
//        return numOperands;
//    }
//    public boolean isEmpty() {
//        return this.numOperands == 0;
//    }
//
//    @Override
//    public @NonNull OperandReversedLinkedList with(@NonNull SymMatrixAdvancedExpr newOperand) {
//        return new OperandReversedLinkedList(this, new SymMatrixAdvancedExpr[]{newOperand}, numOperands + 1);
//    }
//
//    /**
//     * @param newOperands the new operands to be appended at the back <br>
//     *                 [Ownership Transferal] the given newOperands will be directly used
//     *                 as the internal array of the new instance
//     * @return a new {@link OperandReversedLinkedList} instance containing previous operands and the given new operands
//     */
//    @Override
//    public @NonNull OperandReversedLinkedList withAll(@NonNull SymMatrixAdvancedExpr... newOperands) {
//        if (newOperands.length == 0) {  return this;  }
//        return new OperandReversedLinkedList(this, newOperands, numOperands + newOperands.length);
//    }
//
//    @Override
//    public @NonNull OperandList withAll(@NonNull Collection<? extends SymMatrixAdvancedExpr> collection) {
//        if (collection.isEmpty()) {  return this;  }
//        return new OperandReversedLinkedList(this,
//                collection.toArray(SymMatrixAdvancedExpr[]::new),
//                numOperands + collection.size());
//    }
//
//    @Override
//    public @NonNull OperandList withAll(@NonNull OperandList operandList) {
//        if (operandList == ROOT) {  return this;  }
//        return new OperandReversedLinkedList(this, ((Unsafe) operandList).getOperandsFast(), numOperands + operandList.size());
//    }
//
//    public OperandReversedLinkedList withAll(@NonNull OperandReversedLinkedList other) {
//        if (other == ROOT) {  return this;  }
//        return new OperandReversedLinkedList(this, other.getOperandsFast(), numOperands + other.numOperands);
//    }
//
//    @Override
//    public @NonNull OperandList subList(int from, int to) {
//        Objects.checkFromToIndex(from, to, numOperands);
//        if (from > to) {
//            throw new IndexOutOfBoundsException("Begin index (" + from + ") is greater than end index (" + to + ")");
//        }
//        if (from == to) {
//            return ROOT;
//        }
//        if (from == 0) {
//            if (to == numOperands) {
//                return this;
//            }
//            return subListHelperFromRoot(to);
//        }
//        IntObjectPair<OperandReversedLinkedList> toIdxAndNode = findNodeHavingIdx(to);
//        if (toIdxAndNode.right().isIdxInThisSeg(from)) {
//            return subListHelperFromTgtWithTo(from, toIdxAndNode);
//        }
//        return subListHelperFromNotTgtWithTo(from, toIdxAndNode);
//    }
//
//    @Override
//    public @NonNull SymMatrixAdvancedExpr get(int index) {
//        Objects.checkIndex(index, numOperands);
//        IntObjectPair<OperandReversedLinkedList> toIdxAndNode = findNodeHavingIdx(index);
//        return toIdxAndNode.right().segment[  toIdxAndNode.leftInt()  ];
//    }
//
//    public @NonNull SymMatrixAdvancedExpr @NonNull [] getOperands() {
//        if (this.prev == ROOT) {  return Arrays.copyOf(this.segment, numOperands);  }
//        return getOperandsHelper();
//    }
//
//    public @NonNull OperandList toCollapsed() {
//        if (this.prev == ROOT) {  return this;  }
//        SymMatrixAdvancedExpr[] operands = getOperandsFast();
//        return new OperandReversedLinkedList(ROOT, operands, operands.length);
//    }
//
//    @Override
//    public void setElem(int index, @NonNull SymMatrixAdvancedExpr exprWithSameVal) {
//        Objects.checkIndex(index, numOperands);
//        var idxAndNode = findNodeHavingIdx(index);
//        int idx = idxAndNode.leftInt();
//        var node = idxAndNode.right();
//        node.segment[idx] = exprWithSameVal;
//    }
//
//    @Override
//    public @NonNull List<? extends @NonNull SymMatrixExpr> getOperandsNotAdvanced() {
//        return List.of(  getOperandsFast()  );
//    }
//
//    @Override
//    public @NonNull Iterator<SymMatrixExpr> iterator() {
//        return new Iterator<>() {
//            final @NonNull SymMatrixAdvancedExpr @NonNull[] operands = getOperandsFast();
//            int idx = 0;
//            @Override
//            public boolean hasNext() {
//                return idx < operands.length;
//            }
//
//            @Override
//            public SymMatrixExpr next() {
//                if (!hasNext()) {
//                    throw new NoSuchElementException();
//                }
//                return operands[idx++];
//            }
//        };
//    }
//
//
//    public static OperandListBuilder<OperandReversedLinkedList> builder() {
//        return new Builder();
//    }
//    public static OperandListBuilder<OperandReversedLinkedList> builder(int capacity) {
//        return new Builder(capacity);
//    }
//    public static OperandListBuilder<OperandReversedLinkedList> builder(@NonNull OperandReversedLinkedList prev, int capacity) {
//        return new Builder(prev, capacity);
//    }
//    public static class Builder implements OperandListBuilder<OperandReversedLinkedList> {
//
//        private @NonNull OperandReversedLinkedList prev;
//        private @NonNull ObjectArrayList<@NonNull SymMatrixAdvancedExpr> newOperands;
//        private Builder() {
//            this.prev = ROOT;
//            this.newOperands = new ObjectArrayList<>();
//        }
//        private Builder(int capacity) {
//            this.prev = ROOT;
//            this.newOperands = new ObjectArrayList<>(capacity);
//        }
//        private Builder(@NonNull OperandReversedLinkedList prev, int capacity) {
//            this.prev = prev;
//            this.newOperands = new ObjectArrayList<>(capacity);
//        }
//
//        public @NonNull OperandReversedLinkedList build() {
//            return new OperandReversedLinkedList(prev, newOperands.elements(), prev.numOperands + newOperands.size());
//        }
//
//        public static @NonNull Builder of(int capacity) {
//            return new Builder(capacity);
//        }
//        public @NonNull Builder setPrev(@NonNull OperandReversedLinkedList prev) {
//            this.prev = prev;
//            return this;
//        }
//        public @NonNull Builder add(@NonNull SymMatrixAdvancedExpr expr) {
//            newOperands.add(expr);
//            return this;
//        }
//        public @NonNull Builder add(int index, @NonNull SymMatrixAdvancedExpr expr) {
//            newOperands.add(index, expr);
//            return this;
//        }
//        public @NonNull Builder addAll(OperandList operandList) {
//            newOperands.addAll(Arrays.asList(((Unsafe) operandList).getOperandsFast()));
//            return this;
//        }
//        public @NonNull Builder addAll(int index, OperandList operandList) {
//            newOperands.addAll(index, Arrays.asList(((Unsafe) operandList).getOperandsFast()));
//            return this;
//        }
//        public @NonNull Builder addAll(int index, final Collection<? extends SymMatrixAdvancedExpr> collection) {
//            newOperands.addAll(index, collection);
//            return this;
//        }
//        public @NonNull Builder addAll(final Collection<? extends SymMatrixAdvancedExpr> collection) {
//            newOperands.addAll(collection);
//            return this;
//        }
//        public @NonNull Builder set(int idx, @NonNull SymMatrixAdvancedExpr expr) {
//            newOperands.set(idx, expr);
//            return this;
//        }
//        public @NonNull Builder remove(int idx) {
//            newOperands.remove(idx);
//            return this;
//        }
//        public @NonNull Builder remove(@NonNull SymMatrixAdvancedExpr expr) {
//            newOperands.remove(expr);
//            return this;
//        }
//    }
//
//
//    private boolean isIdxInThisSeg(int index) {
//        return index >= this.prev.numOperands;
//    }
//
//
//    private @NonNull OperandReversedLinkedList subListHelperFromRoot(int to) {
//        assert to >= 1; // should be checked and return ROOT directly by call site
//        IntObjectPair<OperandReversedLinkedList> idxAndNode = findNodeHavingIdx(to);
//        int idxOfToNodeSeg = idxAndNode.leftInt();
//        OperandReversedLinkedList node = idxAndNode.right();
//        if (idxOfToNodeSeg == 0) {
//            return node.prev;
//        }
//        return node.prev.withAll(  Arrays.copyOfRange(node.segment, 0, idxOfToNodeSeg)  );
//    }
//
//    private @NonNull OperandReversedLinkedList subListHelperFromTgtWithTo(int from, IntObjectPair<OperandReversedLinkedList> toIdxAndNode) {
//        var node = toIdxAndNode.right();
//        int to = toIdxAndNode.leftInt();
//        from -= node.prev.numOperands;
//        return new OperandReversedLinkedList(ROOT, Arrays.copyOfRange(node.segment, from, to), to - from);
//    }
//
//    private @NonNull OperandReversedLinkedList subListHelperFromNotTgtWithTo(int from, IntObjectPair<OperandReversedLinkedList> toIdxAndNode) {
//        int idxOfToNodeSeg = toIdxAndNode.leftInt();
//        OperandReversedLinkedList toNode = toIdxAndNode.right();
//
//        SymMatrixAdvancedExpr[] toNodePrevOps = toNode.prev.getOperandsFast();
//        int firstHalfLen = toNodePrevOps.length - from;
//        SymMatrixAdvancedExpr[] resultantCopy = new SymMatrixAdvancedExpr[  firstHalfLen + idxOfToNodeSeg  ];
//        System.arraycopy(toNodePrevOps, from, resultantCopy, 0, firstHalfLen);
//
//        System.arraycopy(toNode.segment, 0, resultantCopy, firstHalfLen, idxOfToNodeSeg);
//        return new OperandReversedLinkedList(ROOT, resultantCopy, resultantCopy.length);
//    }
//
//    @NonNull
//    public SymMatrixAdvancedExpr @NonNull [] getOperandsFast() {
//        if (this.prev == ROOT) {  return segment;  }
//        return getOperandsHelper();
//    }
//
//    @NonNull
//    private SymMatrixAdvancedExpr @NonNull [] getOperandsHelper() {
//        SymMatrixAdvancedExpr[] operands = new SymMatrixAdvancedExpr[numOperands];
//        OperandReversedLinkedList current = this;
//        OperandReversedLinkedList previous = this.prev;
//        for (int i = numOperands - 1; i >= 0; --i) { // java using signed int as indexing is so cool
//            int previousNumOperands = previous.numOperands;
//            if (i >= previousNumOperands) {
//                operands[i] = current.segment[  i - previousNumOperands  ];
//                if (i == previousNumOperands) {
//                    current = previous;
//                    previous = previous.prev;
//                }
//            }
//        }
//        return operands;
//    }
//
//    /**
//     * find the node containing the target index, and the relative index of the target index at that node
//     * @param index the target index (absolute)
//     * @return a new pair of the relative index and the node
//     */
//    private @NonNull IntObjectPair<OperandReversedLinkedList> findNodeHavingIdx(int index) {
//        OperandReversedLinkedList current = this;
//        OperandReversedLinkedList previous = prev;
//        while (index < previous.numOperands) {
//            current = previous;
//            previous = previous.prev;
//        }
//        return IntObjectPair.of(index - previous.numOperands, current);
//    }
//}
