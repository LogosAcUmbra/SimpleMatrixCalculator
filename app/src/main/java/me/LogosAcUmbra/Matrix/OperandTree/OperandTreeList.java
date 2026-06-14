package me.LogosAcUmbra.Matrix.OperandTree;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrixAdvancedExpr;
import me.LogosAcUmbra.Matrix.SymMatrixExpr.SymMatrixExpr;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public sealed interface OperandTreeList extends Collection<SymMatrixAdvancedExpr> {
    
    static @NonNull OperandTreeList empty() {
        return Unsafe.Internal.Empty.getInstance();
    }

    /**
     * @param operands the operands <br>
     *                 [Ownership Transferal] if an array of operands is given,
     *                 this array will be directly used as the internal array of the new instance
     * @return a new {@link OperandTreeList.Unsafe.Internal.Leaf} instance containing only the given operands
     */
    static @NonNull OperandTreeList of(@NonNull SymMatrixAdvancedExpr... operands) {
        return Unsafe.Internal.Leaf.of(  operands  );
    }

    static @NonNull OperandTreeList of(@NonNull OperandList operandList) {
        return Unsafe.Internal.Leaf.of(  operandList  );
    }

    static @NonNull OperandTreeList of(@NonNull Collection<? extends SymMatrixAdvancedExpr> collection) {
        return Unsafe.Internal.Leaf.of(  collection  );
    }

    static @NonNull Builder builder() {
        return new Builder(  new ObjectArrayList<>()  );
    }

    static @NonNull Builder builder(int capacity) {
        return new Builder(  new ObjectArrayList<>(capacity)  );
    }
    static @NonNull Builder builder(
            @NonNull ObjectArrayList<@NonNull SymMatrixAdvancedExpr> objArrList) {
        return new Builder(  objArrList.clone()  );
    }
    static @NonNull Builder builder(
            @NonNull SymMatrixAdvancedExpr... operands) {
        return new Builder(  ObjectArrayList.of(operands)  );
    }

    default @NonNull Builder toBuilder() {
        return new Builder(  ObjectArrayList.of( this.getOperands() )  );
    }

    @NonNull OperandTreeList with(@NonNull SymMatrixAdvancedExpr expr);


    @NonNull OperandTreeList withAll(@NonNull Collection<? extends SymMatrixAdvancedExpr> collection);

    /**
     * @param operands the new operands to be appended at the back of the list <br>
     *                 [Ownership Transferal] the given newOperands will be directly used
     *                 as the internal array of the new instance
     * @return a new {@link OperandTreeList} instance containing previous operands and the given new operands
     */
    @NonNull OperandTreeList withAll(@NonNull SymMatrixAdvancedExpr... operands);

    @NonNull OperandTreeList withAll(@NonNull OperandList operandList);

    @NonNull OperandTreeList withAll(@NonNull OperandTreeList other);


    @NonNull OperandTreeList subList(int from, int to);


    @NonNull SymMatrixAdvancedExpr get(int index) throws IndexOutOfBoundsException;

    @NonNull SymMatrixAdvancedExpr getFirst();

    @NonNull SymMatrixAdvancedExpr getLast();


    @NonNull SymMatrixAdvancedExpr @NonNull [] getOperands();


    @NonNull OperandTreeList toCollapsed();

    @NonNull OperandTreeList toCollapsedAndUpdateParent();

    /**
     *
     * @return a not modifiable (cannot add, remove, set elements) <br>
     * and not advanced (each element is immutable without down casting) <br>
     * list of operands stored
     */
    @NonNull List<? extends @NonNull SymMatrixExpr> getOperandsNotAdvanced();

    boolean isLeaf();

    boolean isConcat();

    @Nullable OperandTreeList getParent();

    final class Builder {

        private @NonNull ObjectArrayList<@NonNull SymMatrixAdvancedExpr> elements;

        private Builder(@NonNull ObjectArrayList<@NonNull SymMatrixAdvancedExpr> elements) {
            this.elements = elements;
        }

        public Unsafe.@NonNull Internal build() {
            if (this.elements.isEmpty()) {
                return Unsafe.Internal.Empty.getInstance();
            }
            // Seamlessly creates a Leaf node containing the backing array
            return Unsafe.Internal.Leaf.of(this.elements);
        }

        public @NonNull Builder ensureCapacity(int capacity) {
            this.elements.ensureCapacity(capacity);
            return this;
        }

        public @NonNull Builder setOperands(@NonNull OperandTreeList other) {
            this.elements = new ObjectArrayList<>();
            if (!other.isEmpty()) {
                this.elements.addAll(Arrays.asList(((Unsafe) other).getOperandsFast()));
            }
            return this;
        }

        public @NonNull Builder setOperands(@NonNull SymMatrixAdvancedExpr... operands) {
            this.elements = new ObjectArrayList<>(operands);
            return this;
        }

        public @NonNull Builder setOperands(@NonNull Collection<? extends SymMatrixAdvancedExpr> collection) {
            this.elements = new ObjectArrayList<>(collection);
            return this;
        }

        public @NonNull Builder setOperands(@NonNull ObjectArrayList<@NonNull SymMatrixAdvancedExpr> operands) {
            // Defensively copy
            this.elements = operands.clone();
            return this;
        }

        public @NonNull Builder add(@NonNull SymMatrixAdvancedExpr expr) {
            this.elements.add(expr);
            return this;
        }

        public @NonNull Builder add(int index, @NonNull SymMatrixAdvancedExpr expr) {
            this.elements.add(index, expr);
            return this;
        }

        public @NonNull Builder addAll(OperandTreeList other) {
            if (other != null && !other.isEmpty()) {
                this.elements.addAll(Arrays.asList(((Unsafe) other).getOperandsFast()));
            }
            return this;
        }

        public @NonNull Builder addAll(int index, OperandTreeList other) {
            if (other != null && !other.isEmpty()) {
                this.elements.addAll(index, Arrays.asList(((Unsafe) other).getOperandsFast()));
            }
            return this;
        }

        public @NonNull Builder addAll(int index, Collection<? extends SymMatrixAdvancedExpr> collection) {
            if (collection != null && !collection.isEmpty()) {
                this.elements.addAll(index, collection);
            }
            return this;
        }

        public @NonNull Builder addAll(Collection<? extends SymMatrixAdvancedExpr> collection) {
            if (collection != null && !collection.isEmpty()) {
                this.elements.addAll(collection);
            }
            return this;
        }

        public @NonNull Builder set(int idx, @NonNull SymMatrixAdvancedExpr expr) {
            this.elements.set(idx, expr);
            return this;
        }
    }

    sealed interface Unsafe extends OperandTreeList {

    void setElem(int index, @NonNull SymMatrixAdvancedExpr exprWithSameVal);

    /**
     * Use this method when you're only reading the elements
     * @return fastest way of getting array of operands, MAYBE and MAY NOT BE a DIRECT REFERENCE to internal array
     */
    @NonNull SymMatrixAdvancedExpr @NonNull [] getOperandsFast();

    sealed interface Internal extends OperandTreeList, Unsafe {

    @Override
    @NonNull Internal with(@NonNull SymMatrixAdvancedExpr expr);

    @Override
    @NonNull Internal withAll(@NonNull Collection<? extends SymMatrixAdvancedExpr> collection);

    @Override
    @NonNull Internal withAll(@NonNull SymMatrixAdvancedExpr... operands);

    @Override
    @NonNull Internal withAll(@NonNull OperandList operandList);

    @Override
    @NonNull Internal withAll(@NonNull OperandTreeList other);

    @Override
    @Nullable Concat getParent();

    void setParent(@NonNull Concat parent);

    // OperandList.Unsafe
    @Override
    void setElem(int index, @NonNull SymMatrixAdvancedExpr exprWithSameVal);

    @Override
    @NonNull SymMatrixAdvancedExpr @NonNull [] getOperandsFast();


    /**
     * TreeList Invariants:
     * <p> 1.  an Empty node should not be included in a Concat node at any circumstances
     * <p> 2. any Concat node contains TWO non-empty node, it is NOT possible for a Concat node to just hold a valid left (or just a valid right)
     * <p> 3. from 1 and 2, any node representing only 1 segment of operands is always a Leaf, it is NOT possible to be Concat{Leaf, EMPTY}
     */
    final class Empty extends AbstractCollection<SymMatrixAdvancedExpr> implements Internal {
        private static final @NonNull Empty INSTANCE = new Empty();
        private static final @NonNull SymMatrixAdvancedExpr @NonNull [] EMPTY_ARR = new SymMatrixAdvancedExpr[0];

        private Empty() {
        }

        public static Empty getInstance() {
            return INSTANCE;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public @NonNull Leaf with(@NonNull SymMatrixAdvancedExpr operand) {
            return Leaf.of(operand);
        }

        @Override
        public @NonNull Leaf withAll(@NonNull Collection<? extends SymMatrixAdvancedExpr> collection) {
            return Leaf.of(collection);
        }

        @Override
        public @NonNull Leaf withAll(@NonNull OperandList operandList) {
            return Leaf.of(operandList);
        }

        @Override
        public @NonNull Leaf withAll(@NonNull SymMatrixAdvancedExpr... operands) {
            return Leaf.of(operands);
        }

        @Override
        public @NonNull Internal withAll(@NonNull OperandTreeList other) {
            return (Internal) other;
        }

        @Override
        public @NonNull Empty subList(int from, int to) {
            Objects.checkFromIndexSize(from, to, 0);
            return INSTANCE;
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr get(int index)
                throws IndexOutOfBoundsException {
            Objects.checkIndex(0, 0);
            throw new IndexOutOfBoundsException("Unreachable");
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr getFirst() {
            throw new NoSuchElementException();
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr getLast() {
            throw new NoSuchElementException();
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr @NonNull [] getOperands() {
            return EMPTY_ARR;
        }

        @Override
        public @NonNull Empty toCollapsed() {
            return this;
        }

        @Override
        public @NonNull OperandTreeList toCollapsedAndUpdateParent() {
            return this;
        }

        @Override
        public void setElem(int index, @NonNull SymMatrixAdvancedExpr exprWithSameVal) {
            Objects.checkIndex(index, 0);
            throw new IndexOutOfBoundsException("Unreachable");
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public boolean isConcat() {
            return false;
        }

        @Override
        public @NonNull List<? extends @NonNull SymMatrixExpr> getOperandsNotAdvanced() {
            return List.of();
        }


        @Override
        public @NonNull Iterator<SymMatrixAdvancedExpr> iterator() {
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public SymMatrixAdvancedExpr next() {
                    throw new NoSuchElementException();
                }
            };
        }

        // Tree

        @Override
        public @Nullable Concat getParent() {
            throw new IllegalStateException("node is Empty");
        }

        // internal

        @Override
        public void setParent(@NonNull Concat parent) {
            throw new IllegalStateException("node is Empty");
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr @NonNull [] getOperandsFast() {
            throw new IllegalStateException("node is Empty");
        }
    }

    /**
     * TreeList Invariants:
     * <p> 1.  an Empty node should not be included in a Concat node at any circumstances
     * <p> 2. any Concat node contains TWO non-empty node, it is NOT possible for a Concat node to just hold a valid left (or just a valid right)
     * <p> 3. from 1 and 2, any node representing only 1 segment of operands is always a Leaf, it is NOT possible to be Concat{Leaf, EMPTY}
     */
    final class Leaf extends AbstractCollection<SymMatrixAdvancedExpr> implements Internal {
        @Nullable Concat parent;
        @NonNull SymMatrixAdvancedExpr @NonNull [] segment;

        private Leaf(@NonNull SymMatrixAdvancedExpr @NonNull [] segment) {
            this.parent = null;
            this.segment = segment;
        }
        private Leaf(@Nullable Concat parent, @NonNull SymMatrixAdvancedExpr @NonNull [] segment) {
            this.parent = parent;
            this.segment = segment;
        }

        private static @NonNull Leaf of(@NonNull SymMatrixAdvancedExpr... operands) {
            assert operands.length > 0;
            return new Leaf(operands);
        }

        private static @NonNull Leaf of(@NonNull OperandList operandList) {
            return new Leaf(((OperandList.Unsafe) operandList).getOperandsFast());
        }

        private static @NonNull Leaf of(@NonNull Collection<? extends SymMatrixAdvancedExpr> collection) {
            return new Leaf(collection.toArray(SymMatrixAdvancedExpr[]::new));
        }

        @Override
        public int size() {
            return this.segment.length;
        }

        @Override
        public boolean isEmpty() {
            return false; // an empty OperandTree should be Empty.INSTANCE
        }

        @Override
        public @NonNull Concat with(@NonNull SymMatrixAdvancedExpr expr) {
            return Concat.of(this, Leaf.of(expr));
        }

        @Override
        public @NonNull Internal withAll(@NonNull Collection<? extends SymMatrixAdvancedExpr> collection) {
            if (collection.isEmpty()) {
                return this;
            }
            return Concat.of(this, Leaf.of(collection));
        }

        @Override
        public @NonNull Internal withAll(@NonNull SymMatrixAdvancedExpr... operands) {
            if (operands.length == 0) {
                return this;
            }
            return Concat.of(this, Leaf.of(operands));
        }

        @Override
        public @NonNull Internal withAll(@NonNull OperandList operandList) {
            if (operandList.isEmpty()) {
                return this;
            }
            return Concat.of(this, Leaf.of(operandList));
        }

        @Override
        public @NonNull Internal withAll(@NonNull OperandTreeList other) {
            if (other.isEmpty()) {
                return this;
            }
            return Concat.of(this, (Internal) other);
        }

        @Override
        public @NonNull Leaf subList(int from, int to) {
            int length = to - from;
            SymMatrixAdvancedExpr[] subSegment = new SymMatrixAdvancedExpr[length];
            System.arraycopy(segment, from, subSegment, 0, length);
            return Leaf.of(subSegment);
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr get(int index) {
            return this.segment[index];
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr getFirst() {
            return this.segment[0];
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr getLast() {
            return this.segment[  this.segment.length - 1  ];
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr @NonNull [] getOperands() {
            return Arrays.copyOf(this.segment, this.segment.length);
        }

        @Override
        public @NonNull Leaf toCollapsed() {
            return this;
        }

        @Override
        public @NonNull Leaf toCollapsedAndUpdateParent() {
            return this;
        }

        // Unsafe

        @Override
        public void setElem(int index, @NonNull SymMatrixAdvancedExpr exprWithSameVal) {
            this.segment[index] = exprWithSameVal;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public boolean isConcat() {
            return false;
        }

        @Override
        public @NonNull List<? extends @NonNull SymMatrixExpr> getOperandsNotAdvanced() {
            return List.of(segment);
        }

        @Override
        public @NonNull Iterator<SymMatrixAdvancedExpr> iterator() {
            return new Iterator<SymMatrixAdvancedExpr>() {
                int idx = 0;
                @Override
                public boolean hasNext() {
                    return idx < segment.length;
                }

                @Override
                public SymMatrixAdvancedExpr next() {
                    if (!hasNext()) {
                        throw new java.util.NoSuchElementException();
                    }
                    return segment[idx++];
                }
            };
        }

        @Override
        public @Nullable Concat getParent() {
            return parent;
        }

        @Override
        public void setParent(@NonNull Concat parent) {
            this.parent = parent;
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr @NonNull [] getOperandsFast() {
            return segment;
        }
    }

    /**
     * TreeList Invariants:
     * <p> 1.  an Empty node should not be included in a Concat node at any circumstances
     * <p> 2. any Concat node contains TWO non-empty node, it is NOT possible for a Concat node to just hold a valid left (or just a valid right)
     * <p> 3. from 1 and 2, any node representing only 1 segment of operands is always a Leaf, it is NOT possible to be Concat{Leaf, EMPTY}
     */
    final class Concat extends AbstractCollection<SymMatrixAdvancedExpr> implements Internal {
        @Nullable Concat parent;
        @NonNull Internal left;
        @NonNull Internal right;
        final int numOperands;

        private Concat(@NonNull Internal left, @NonNull Internal right) {
            this.parent = null;
            this.left = left;
            this.right = right;
            this.numOperands = left.size() + right.size();
        }

        private static @NonNull Concat of(@NonNull Internal left, @NonNull Internal right) {
            assert !left.isEmpty() && !right.isEmpty();
            Concat result = new Concat(left, right);
            left.setParent(result);
            right.setParent(result);
            return result;
        }

        @Override
        public int size() {
            return numOperands;
        }

        @Override
        public boolean isEmpty() {
            return false; // an empty OperandTree should be Empty.INSTANCE
        }

        @Override
        public @NonNull Internal with(@NonNull SymMatrixAdvancedExpr expr) {
            return Concat.of(this, Leaf.of(expr));
        }

        @Override
        public @NonNull Internal withAll(@NonNull Collection<? extends SymMatrixAdvancedExpr> collection) {
            if (collection.isEmpty()) {
                return this;
            }
            return Concat.of(this, Leaf.of(collection));
        }

        @Override
        public @NonNull Internal withAll(@NonNull SymMatrixAdvancedExpr... operands) {
            if (operands.length == 0) {
                return this;
            }
            return Concat.of(this, Leaf.of(operands));
        }

        @Override
        public @NonNull Internal withAll(@NonNull OperandList other) {
            return Concat.of(this, Leaf.of(other));
        }

        @Override
        public @NonNull Concat withAll(@NonNull OperandTreeList other) {
            return Concat.of(this, (Internal) other);
        }

        @Override
        public @NonNull Internal subList(int from, int to) {
            Leaf leaf = toCollapsed();
            return leaf.subList(from, to);
            // TODO
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr get(int index) {
            IntObjectPair<Leaf> idxAndLeaf = findLeafHavingIdx(index);
            return idxAndLeaf.right().get(idxAndLeaf.leftInt());
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr getFirst() {
            return get(0); // TODO
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr getLast() {
            return get(numOperands - 1); // TODO
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr @NonNull [] getOperands() {
            return getOperandsFast();
        }

        @Override
        public @NonNull Leaf toCollapsed() {
            @NonNull SymMatrixAdvancedExpr [] operands = getOperandsFast();
            return new Leaf(this.parent, operands);
        }

        @Override
        public @NonNull Leaf toCollapsedAndUpdateParent() {
            @NonNull SymMatrixAdvancedExpr [] operands = getOperandsFast();
            // update parent.child
            Concat parent = this.parent;
            if (parent == null) {
                return new Leaf(operands);
            }
            Leaf collapsed = new Leaf(parent, operands);
            if (parent.left == this) {
                parent.left = collapsed;
            } else if (parent.right == this) {
                parent.right = collapsed;
            } else {
                throw new IllegalStateException("this.parent does not have this as its children" +
                        "Info: { this: " + this + " , this.parent: " + parent + " }");
            }
            // break link to parent
            this.parent = null;
            return collapsed;
        }

        @Override
        public void setElem(int index, @NonNull SymMatrixAdvancedExpr exprWithSameVal) {
            IntObjectPair<Leaf> idxAndLeaf = findLeafHavingIdx(index);
            idxAndLeaf.right().setElem(idxAndLeaf.leftInt(), exprWithSameVal);
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public boolean isConcat() {
            return true;
        }

        @Override
        public @NonNull List<? extends @NonNull SymMatrixExpr> getOperandsNotAdvanced() {
            return List.of(getOperands());
        }

        @Override
        public @NonNull Iterator<SymMatrixAdvancedExpr> iterator() {
            return new Iterator<>() {
                // there is nth we can do
                private final @NonNull SymMatrixAdvancedExpr @NonNull [] operands = getOperandsFast();
                private int idx;
                @Override
                public boolean hasNext() {
                    return idx < operands.length;
                }

                @Override
                public SymMatrixAdvancedExpr next() {
                    if (!hasNext()) {
                        throw new java.util.NoSuchElementException();
                    }
                    return operands[idx++];
                }
            };
        }

        @Override
        public @Nullable Concat getParent() {
            return parent;
        }

        @Override
        public void setParent(@NonNull Concat parent) {
            this.parent = parent;
        }

        @Override
        public @NonNull SymMatrixAdvancedExpr @NonNull [] getOperandsFast() {
            return getOperandsHelper();
        }

        public void setLeft(@NonNull Leaf left) {
            this.left = left;
        }

        public void setRight(@NonNull Leaf right) {
            this.right = right;
        }

        private boolean isIndexAtLeft(int index) {
            return index < left.size();
        }

        private boolean isIndexAtRight(int index) {
            return index >= left.size();
        }

        private @NonNull IntObjectPair<@NonNull Leaf> findLeafHavingIdx(int idx) {
            Concat current = this;
            while (idx >= 0) {
                if (isIndexAtLeft(idx)) {
                    switch (current.left) {
                        case Leaf leftLeaf: {
                            return IntObjectPair.of(idx, leftLeaf);
                        }
                        case Concat leftConcat: {
                            current = leftConcat;
                            break;
                        }
                        default: {
                            throw new IllegalStateException("UnexpectedState: left(" + current.left + ") should not be empty.");
                        }
                    }
                } else {
                    idx -= current.left.size();
                    switch (current.right) {
                        case Leaf rightLeaf: {
                            return IntObjectPair.of(idx, rightLeaf);
                        }
                        case Concat rightConcat: {
                            current = rightConcat;
                            break;
                        }
                        default: {
                            throw new IllegalStateException("UnexpectedState: right(" + current.right + ") should not be empty.");
                        }
                    }
                }
            }
            throw new IllegalArgumentException("UnreachableCodeReached: loop ended before return, Info: { index: " + idx + " , this: " + this + " }");
        }

        private @NonNull SymMatrixAdvancedExpr @NonNull [] getOperandsHelper() { // fk
            SymMatrixAdvancedExpr[] result = new SymMatrixAdvancedExpr[numOperands];
            int numFilled = 0;
            Stack<Internal> nodesTotallyNotDealt = new ObjectArrayList<>();


            boolean isCurrentMustConcat = true; // continuation mismatch ( isCurrentMustConcat = not start from middle )
            Concat currentConcat = this; // the one to deal with when isCurrentMustConcat is true
            Internal current = null; // the one to deal with when isCurrentMustConcat is false
            while (true) {
                if (isCurrentMustConcat) {
                    // go to leftmost, and push nodes that passed by
                    while (currentConcat.left instanceof Concat leftConcat) {
                        nodesTotallyNotDealt.push(currentConcat.right);
                        currentConcat = leftConcat;
                    }
                    // note that current.right is not pushed
                    // current is now the leftmost Concat node
                    // deal with current.left
                    // current.left must be a Leaf
                    { // add left to array
                        Leaf leftLeaf = (Leaf) currentConcat.left;
                        int length = leftLeaf.segment.length;
                        System.arraycopy(leftLeaf.segment, 0, result, numFilled, length);
                        numFilled += length;
                    }

                    current = currentConcat.right;
                }
                // if not isCurrentMustConcat, jump to here ------------------------------------------------------------

                if (current instanceof Concat) {
                    currentConcat = (Concat) current;

                    isCurrentMustConcat = true; continue; // go back to top
                }
                // then current is a Leaf node
                Leaf currentLeaf = (Leaf) current;
                { // add currentLeaf into array
                    int length = currentLeaf.segment.length;
                    System.arraycopy(currentLeaf.segment, 0, result, numFilled, length);
                    numFilled += length;
                }
                // the current node is completely dealt, find a new current node
                if (nodesTotallyNotDealt.isEmpty()) {  // leaving point of the loop ------------------------------------
                    break;
                }
                current = nodesTotallyNotDealt.pop();
                isCurrentMustConcat = false; continue; // go back to middle
            }
            return result;
        }

    } // Concat
    } // Internal
    } // Unsafe
} // OperandTreeList
