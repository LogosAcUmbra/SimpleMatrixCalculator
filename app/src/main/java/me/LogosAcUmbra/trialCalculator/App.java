package me.LogosAcUmbra.trialCalculator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class App {

    public static void main(String[] args) {
        Interactor interactor;
        try {
            interactor = new Interactor();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        interactor.start();

    }
}
