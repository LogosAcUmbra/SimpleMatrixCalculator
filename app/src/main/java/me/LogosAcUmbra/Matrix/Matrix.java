package me.LogosAcUmbra.Matrix;

public interface Matrix extends MatrixBase {
    int getOffset();
    int getRowStride();
    int getColStride();
    boolean isZero();
    boolean isEmpty();

    /**
     * @return {@link #getOffset()}{@code == 0} && {@link #isContAnyMaj()}
     */
    default boolean is0ContAnyMaj() {
        return getOffset() == 0 && isContAnyMaj();
    }

    /**
     * @return {@link #getOffset()}{@code == 0} && {@link #isContRowMaj()}
     */
    default boolean is0ContRowMaj() {
        return getOffset() == 0 && isContRowMaj();
    }

    /**
     * @return {@link #getOffset()}{@code == 0} && {@link #isContColMaj()}
     */
    default boolean is0ContColMaj() {
        return getOffset() == 0 && isContColMaj();
    }

    /**
     * @return {@link #isContRowMaj()} || {@link #isContColMaj()}
     */
    default boolean isContAnyMaj() {
        return isContRowMaj() || isContColMaj();
    }

    /**
     * @return {@link #isRowMaj()} && {@link #getRowStride()}{@code == getNumCols()}
     */
    default boolean isContRowMaj() {
        return isRowMaj() && getRowStride() == getNumCols();
    }

    /**
     * @return {@link #isColMaj()} && {@link #getColStride()}{@code == getNumRows()}
     */
    default boolean isContColMaj() {
        return isColMaj() && getColStride() == getNumRows();
    }

    /**
     * @return {@link #getColStride()}{@code == 1}
     */
    default boolean isRowMaj() {
        return getColStride() == 1;
    }

    /**
     * @return {@link #getRowStride()}{@code == 1}
     */
    default boolean isColMaj() {
        return getRowStride() == 1;
    }

}
