package me.LogosAcUmbra.SimpleMatrixCalculator;

public class App {

    public static void main(String[] args) {
        Interactor interactor;
        try {
            interactor = new Interactor();
            interactor.start();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }
}
