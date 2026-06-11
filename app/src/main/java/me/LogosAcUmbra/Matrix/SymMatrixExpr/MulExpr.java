package me.LogosAcUmbra.Matrix;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.matheclipse.core.interfaces.IExpr;

import java.util.Collections;
import java.util.List;

import static me.LogosAcUmbra.Matrix.ISymMatrixAdvancedExpr.ensureIsAdvanced;

public class MulExpr implements IManyOperandExpr {

    @Nullable ISymMatrixExpr parent;
    final int numRows;
    final int numCols;
    final @NonNull ObjectArrayList<ISymMatrixAdvancedExpr> operands;
    final int numOperands;

    MulExpr(int numRows, int numCols, @NonNull ObjectArrayList<ISymMatrixAdvancedExpr> operands, int numOperands) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.operands = operands;
        this.numOperands = numOperands;
    }

    public static MulExpr of(@NonNull ISymMatrixExpr expr1, @NonNull ISymMatrixExpr expr2) {
        ISymMatrixAdvancedExpr advExpr1 = ensureIsAdvanced(expr1);
        ISymMatrixAdvancedExpr advExpr2 = ensureIsAdvanced(expr2);
        assert (advExpr1 instanceof MulExpr);
        if (advExpr1.getNumCols() != advExpr2.getNumRows()) {
            throw illegalMulDimension(advExpr1, advExpr2, "expr1", "expr2");
        }
        int numRows = advExpr1.getNumRows();
        int numCols = advExpr2.getNumCols();
        if (advExpr2 instanceof MulExpr mulExpr) {
            int size = mulExpr.operands.size();
            ObjectArrayList<ISymMatrixAdvancedExpr> operands = new ObjectArrayList<>(1 + size);
            operands.add(advExpr1);
            operands.addAll(mulExpr.operands);
            return new MulExpr(numRows, numCols, operands, operands.size());
        }
        return new MulExpr(numRows, numCols, ObjectArrayList.of(advExpr1, advExpr2), 2);
    }

    @Override
    public int getNumRows() {
        return numRows;
    }
    @Override
    public int getNumCols() {
        return numCols;
    }

    @Override
    public @NonNull ISymMatrixExpr times(@NonNull ISymMatrixExpr expr) {
        if (this.numCols != expr.getNumRows()) {
            throw illegalMulDimension(this, expr, "this", "expr");
        }
        numCols = expr.getNumCols();
        operands.add(expr);
        return this;
    }

    @Override
    public void computeInto(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool bufferPool) {

    }

    @Override
    public @NonNull List<ISymMatrixExpr> getOperands() {
        return operands;
    }

    /**
     * UNSAFE <br>
     * return the direct reference to the internal ArrayList for storing operands
     *
     * @return the reference to the internal ArrayList for storing operands
     */
    public @NonNull ObjectArrayList<ISymMatrixExpr> unsafeGetOperands() {
        return operands;
    }

    protected static IllegalArgumentException illegalMulDimension(ISymMatrixExpr expr1, ISymMatrixExpr expr2, String expr1Name, String expr2Name) {
        return new IllegalArgumentException(String.format(
                "illegal dimensions: %s.numCols(%d) != %s.numRows(%d), Infos: { %s: %s, %s: %s }",
                expr1Name, expr1.getNumCols(), expr2Name, expr2.getNumRows(),
                expr1Name, expr1, expr2Name, expr2
        ));
    }

    @Override
    public @NonNull ObjectArrayList<ISymMatrixAdvancedExpr> getOperandsRef() {
        return Collections.unmodifiableList(operands.subList(0, numOperands));
    }

    @Override
    public @Nullable ISymMatrixExpr getParent() {
        return parent;
    }

    @Override
    public @NonNull MulExpr setParent(@NonNull ISymMatrixExpr parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public void computeIntoBuffer(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool pool) {
        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull SymMatrixBuffer computeToBuffer(@NonNull SymMatrixBufferPool pool) {
        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }

}
