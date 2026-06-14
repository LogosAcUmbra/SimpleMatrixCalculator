package me.LogosAcUmbra.Matrix;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NonNull;


public class SymMatrixBufferPool {

    private final @NonNull Int2ObjectMap<Stack<@NonNull SymMatrixBuffer>>
            buffers = new Int2ObjectOpenHashMap<>();

    /**
     * lease a matrix buffer with its internal array of the given capacity, all elements being null,
     * all other fields contain GARBAGE values
     * @param capacity the capacity of the internal array of the matrix buffer
     * @return the leased matrix
     */
    public synchronized @NonNull SymMatrixBuffer lease(int capacity) {
        Stack<SymMatrixBuffer> bucket = buffers.computeIfAbsent(capacity, k -> new ObjectArrayList<>());
        if (bucket.isEmpty()) {
            return SymMatrixBuffer.unsafeOfCapacity(capacity);
        }
        return bucket.pop();
    }
    /**
     * lease a Contiguous Row Major layout matrix buffer of the given size, with all elements being null
     * @param numRows numRows
     * @param numCols numCols
     * @return a Contiguous Row Major layout matrix buffer with all elements being null
     */
    public synchronized @NonNull SymMatrixBuffer lease0ContRowMaj(int numRows, int numCols) {
        int capacity = numRows * numCols;
        Stack<SymMatrixBuffer> bucket = buffers.computeIfAbsent(capacity, k -> new ObjectArrayList<>());
        if (bucket.isEmpty()) {
            return SymMatrixBuffer.of(numRows, numCols);
        }
        return bucket.pop().unsafeSetFields(numRows, numCols, 0, numCols, 1, false, false);
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
        int capacity = buffer.unsafeGetRaw().length;
        java.util.Arrays.fill(buffer.unsafeGetRaw(), 0, capacity, null); // GC
        Stack<SymMatrixBuffer> bucket = buffers.get(capacity);
        if (bucket != null) {
            bucket.push(buffer);
        } else {
            throw new IllegalArgumentException(
                    "Buffer capacity " + capacity + " does not belong to this pool. "
                    + "Info: { buffer: " + buffer + " }"
            );
        }
    }

}
