package me.LogosAcUmbra.Matrix;

public interface ISymMatrix extends IBaseSymMatrix {
    int getOffset();
    int getRowStride();
    int getColStride();
}
