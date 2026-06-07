package me.LogosAcUmbra.Matrix;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NonNull;
import org.matheclipse.core.interfaces.IExpr;

import java.util.List;

public class MulExpr implements ISymMatrixExpr {

    int numRows;
    int numCols;
    final ObjectArrayList<ISymMatrixExpr> operands;

    MulExpr(int numRows, int numCols, @NonNull ObjectArrayList<ISymMatrixExpr> operands) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.operands = operands;
    }

    static MulExpr of(@NonNull ISymMatrixExpr expr1, @NonNull ISymMatrixExpr expr2) {
        assert (expr1 instanceof MulExpr);
        if (expr1.getNumCols() != expr2.getNumRows()) {
            throw illegalMulDimension(expr1, expr2, "expr1", "expr2");
        }
        int numRows = expr1.getNumRows();
        int numCols = expr2.getNumCols();
        if (expr2 instanceof MulExpr mulExpr) {
            int size = mulExpr.operands.size();
            ObjectArrayList<ISymMatrixExpr> terms = new ObjectArrayList<>(1 + size);
            terms.size(size);
            terms.set(0, expr1);
            System.arraycopy(mulExpr.operands.elements(), 0, terms.elements(), 0, size);
            return new MulExpr(numRows, numCols, terms);
        }
        return new MulExpr(numRows, numCols, ObjectArrayList.of(expr1, expr2));
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
    public @NonNull ISymMatrixExpr plus(@NonNull ISymMatrixExpr expr) {
        return SumExpr.of(this, expr);
    }

    @Override
    public @NonNull ISymMatrixExpr minus(@NonNull ISymMatrixExpr expr) {
        return SumExpr.ofMinus(this, expr);
    }

    @Override
    public @NonNull ISymMatrixExpr scale(@NonNull IExpr scalar) {
        return ScaleExpr.of(this, scalar);
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
    public void evalInto(@NonNull SymMatrixBuffer target, @NonNull SymMatrixBufferPool bufferPool) {

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

}
