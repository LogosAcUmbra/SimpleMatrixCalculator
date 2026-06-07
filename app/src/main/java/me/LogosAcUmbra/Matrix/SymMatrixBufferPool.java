package me.LogosAcUmbra.Matrix;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NonNull;
import org.matheclipse.core.interfaces.IExpr;


public class SymMatrixBufferPool {

    private final @NonNull Int2ObjectMap<Stack<@NonNull SymMatrixBuffer>>
            buffers = new Int2ObjectOpenHashMap<>();

    public synchronized @NonNull SymMatrixBuffer lease(int numRows, int numCols) {
        int capacity = numRows * numCols;
        Stack<SymMatrixBuffer> bucket = buffers.computeIfAbsent(capacity, k -> new ObjectArrayList<>());
        if (bucket.isEmpty()) {
            return SymMatrixBuffer.unsafeOfRowMaj(
                    new IExpr[capacity],
                    numRows, numCols
            );
        }
        return bucket.pop().setFields(numRows, numCols, 0, numCols, 1);
    }

    /**
     * Recycles a buffer back into the pool. <br>
     * the given buffer has its ownership given to the pool <br>
     * <br>
     * details: <br>
     * the given buffer will have all its elements assigned to null <br>
     * the other fields of the buffer remain unchanged
     *
     * @param buffer the buffer to be recycled
     */
    public synchronized void recycle(@NonNull SymMatrixBuffer buffer) {
        int capacity = buffer.raw.length;
        java.util.Arrays.fill(buffer.raw, 0, capacity, null); // GC
        Stack<SymMatrixBuffer> bucket = buffers.get(capacity);
        if (bucket != null) {
            bucket.push(buffer);
        } // else, this buffer did not come from the lease function
    }

}
