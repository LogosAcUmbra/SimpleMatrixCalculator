package me.LogosAcUmbra.Matrix.OperandTree;


import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.LogosAcUmbra.Matrix.SymMatrixExpr.ISymMatrixAdvancedExpr;
import org.jspecify.annotations.NonNull;

import java.util.Objects;


/**
 * <p><b>Logical Immutability:</b>
 * This class guarantees <i>logical immutability</i>. Although internal state optimization
 * (path compression) occurs via {@link #getOperandsAndFlatten()}, the effective value,
 * order, and elements of this list can never be altered after construction.
 *
 * <p><b>Method Guidance:</b>
 * <ul>
 *   <li>Use {@link #getOperandsAndFlatten()} at major execution boundaries (e.g., prior to
 *       buffered evaluation or multithreaded processing) to collapse the reverse-links
 *       into a contiguous array for peak CPU cache performance.</li>
 * </ul>
 */
public class OperandList implements IOperandList {

    public static final @NonNull OperandList ROOT = createRoot();

    private @NonNull OperandList prev;
    private @NonNull ObjectArrayList<@NonNull ISymMatrixAdvancedExpr> segment;
    private final int numOperands;

    private OperandList(
            @NonNull OperandList prev,
            @NonNull ObjectArrayList<@NonNull ISymMatrixAdvancedExpr> segment,
            int numOperands
    ) {
        this.prev = prev;
        this.segment = segment;
        this.numOperands = numOperands;
    }

    @SuppressWarnings({"ConstantConditions", "DataFlowIssue"})
    private static OperandList createRoot() {
        // it is the only case (root) that we need to pass null into the prev argument of constructor
        OperandList root = new OperandList(
                (OperandList) null, new ObjectArrayList<>(0), 0);
        root.prev = root;
        return root;
    }

    public static OperandList of(@NonNull ObjectArrayList<@NonNull ISymMatrixAdvancedExpr> operands) {
        return new OperandList(ROOT, operands, operands.size());
    }

    @Override
    public int size() {
        return numOperands;
    }
    public boolean isEmpty() {
        return this.numOperands == 0;
    }

    @Override
    public @NonNull OperandList add(@NonNull ISymMatrixAdvancedExpr arg) {
        return new OperandList(this, ObjectArrayList.of(arg), numOperands + 1);
    }

    @Override
    public @NonNull OperandList addAll(@NonNull ISymMatrixAdvancedExpr... args) {
        if (args.length == 0) {  return this;  }
        return new OperandList(this, ObjectArrayList.of(args), numOperands + args.length);
    }

    @Override
    public @NonNull OperandList addAll(@NonNull ObjectArrayList<@NonNull ISymMatrixAdvancedExpr> objArrList) {
        if (objArrList.isEmpty()) {  return this;  }
        return new OperandList(this, objArrList, numOperands + objArrList.size());
    }

    @Override
    public @NonNull IOperandList addAll(@NonNull IOperandList other) {
        if (other == ROOT) {  return this;  }
        return new OperandList(this, other.getOperandsAndFlatten(), numOperands + other.size());
    }

    public OperandList addAll(@NonNull OperandList other) {
        if (other == ROOT) {  return this;  }
        return new OperandList(this, other.getOperandsAndFlatten(), numOperands + other.numOperands);
    }

    @Override
    public @NonNull IOperandList subList(int from, int to) {
        Objects.checkFromToIndex(from, to, numOperands);
        if (from > to) {
            throw new IndexOutOfBoundsException("Begin index (" + from + ") is greater than end index (" + to + ")");
        }
        if (from == to) {
            return ROOT;
        }
        if (from == 0) {
            if (to == numOperands) {
                return this;
            }
            return subListHelperFromRoot(to);
        }
        IntObjectPair<OperandList> toIdxAndNode = findNodeHavingIdx(to);
        if (toIdxAndNode.right().isIdxInThisSeg(from)) {
            return subListHelperFromTgtWithTo(from, toIdxAndNode);
        }
        return subListHelperFromNotTgtWithTo(from, toIdxAndNode);
    }

    @Override
    public @NonNull ISymMatrixAdvancedExpr get(int index) {
        Objects.checkIndex(index, numOperands);
        IntObjectPair<OperandList> toIdxAndNode = findNodeHavingIdx(index);
        return toIdxAndNode.right().segment.get(toIdxAndNode.leftInt());
    }

    public @NonNull ObjectArrayList<@NonNull ISymMatrixAdvancedExpr> getOperands() {
        if (this.prev == ROOT) {
            return segment;
        }
        return getOperandsHelper();
    }

    /**
     * UNSAFE
     * @return direct reference to the ObjectArrayList instance holding all operands.
     * This ObjectArrayList instance WILL BE USED internally
     */
    public @NonNull ObjectArrayList<@NonNull ISymMatrixAdvancedExpr> getOperandsAndFlatten() {
        if (this.prev == ROOT) {
            return segment;
        }
        ObjectArrayList<ISymMatrixAdvancedExpr> operands = getOperandsHelper();
        // flatten this
        this.prev = ROOT;
        this.segment = operands;
        return operands;
    }

    private boolean isIdxInThisSeg(int index) {
        return index >= this.prev.numOperands;
    }


    private @NonNull OperandList subListHelperFromRoot(int to) {
        assert to >= 1; // should be checked and return ROOT directly by call site
        IntObjectPair<OperandList> idxAndNode = findNodeHavingIdx(to);
        int idxOfToNodeSeg = idxAndNode.leftInt();
        OperandList node = idxAndNode.right();
        if (idxOfToNodeSeg == 0) {
            return node.prev;
        }
        return node.prev.addAll(  new ObjectArrayList<>(node.segment.subList(0, idxOfToNodeSeg))  );
    }

    private @NonNull OperandList subListHelperFromTgtWithTo(int from, IntObjectPair<OperandList> toIdxAndNode) {
        var node = toIdxAndNode.right();
        int to = toIdxAndNode.leftInt();
        from -= node.prev.numOperands;
        return new OperandList(ROOT, new ObjectArrayList<>(node.segment.subList(from, to)), to - from);
    }

    private @NonNull OperandList subListHelperFromNotTgtWithTo(int from, IntObjectPair<OperandList> toIdxAndNode) {
        int idxOfToNodeSeg = toIdxAndNode.leftInt();
        OperandList toNode = toIdxAndNode.right();
        var toNodePrevOps = toNode.prev.getOperandsAndFlatten();
        var resultantCopy = new ObjectArrayList<>(toNodePrevOps.subList(from, toNodePrevOps.size()));
        resultantCopy.addAll(toNode.segment.subList(0, idxOfToNodeSeg));
        return new OperandList(ROOT, resultantCopy, resultantCopy.size());
    }

    private @NonNull ObjectArrayList<@NonNull ISymMatrixAdvancedExpr> getOperandsHelper() {
        ObjectArrayList<ISymMatrixAdvancedExpr> operands = new ObjectArrayList<>(numOperands);
        operands.size(numOperands);
        OperandList current = this;
        OperandList previous = this.prev;
        for (int i = numOperands - 1; i >= 0; --i) { // java using signed int as indexing is so cool
            int previousNumOperands = previous.numOperands;
            if (i >= previousNumOperands) {
                operands.set(i, current.segment.get(i - previousNumOperands));
                if (i == previousNumOperands) {
                    current = previous;
                    previous = previous.prev;
                }
            }
        }
        return operands;
    }

    /**
     * find the node containing the target index, and the relative index of the target index at that node
     * @param index the target index (absolute)
     * @return a new pair of the relative index and the node
     */
    private @NonNull IntObjectPair<OperandList> findNodeHavingIdx(int index) {
        OperandList current = this;
        OperandList previous = prev;
        while (index < previous.numOperands) {
            current = previous;
            previous = previous.prev;
        }
        return IntObjectPair.of(index - previous.numOperands, current);
    }
}
