package me.LogosAcUmbra.SimpleMatrixCalculator;

import me.LogosAcUmbra.UiText.*;
import me.LogosAcUmbra.Utils.Utils;
import org.ejml.data.DMatrixRMaj;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.BiFunction;


public class Interactor {
    private final int indentSize = 2;
    private final Scanner scanner = new Scanner(System.in);
    private final Section section = new Section();
    private final UiTextManager textManager;
    private final boolean zeroIndexing = false;
    private final int matSizeUpperBound = 20;

    public Interactor() {
        textManager = UiTextManager.getInstance();
    }


    public void start() {
        startupFunc();
        menu();
    }

    private void startupFunc() {
        createMatrix();
    }

    private void menu() {
        while (true) {
            PromptNode choicePrompt = textManager.root().prompts().choices();
            DirNode menuDir = textManager.root().dirs().menu();
            String[] choiceKeys = {"1", "2", "3", "S", "Q"};
            System.out.print(menuDir.title().txt());
            System.out.print(menuDir.body().path("choices").txt());

            String choice = askUntilMatch(
                    choiceKeys,
                    choicePrompt.addIndentOf(menuDir).ask().txt(),
                    choicePrompt.addIndentOf(menuDir).err().txt(),
                    this::choicesLaxEq
            );
            switch (choice) {
                case ("1"): createMatrix(); break;
                case ("Q"): return;
                default: System.out.println("function not finished"); continue;
            }
            continue;
        }
    }

    private void createMatrix() {
        PromptGroupNode prompts = textManager.root().prompts();
        DirNode createMatrixDir = textManager.root().dirs().createMatrix();
        // title
        System.out.print(createMatrixDir.title().txt());

        // size header
        UiTextNode sizeNode = createMatrixDir.body().path("size");
        System.out.print(sizeNode.path("header").txt());
        // rows
        int rows = askUntilPositiveInt(
                prompts.useIndentOf(sizeNode).rows().ask().txt(),
                matSizeUpperBound,
                prompts.useIndentOf(sizeNode).rows().err().txt(),
                prompts.rows().quitSpecifier().txt() // no need indent
        );
        createMatrixDir.interruptMsg().txt();

        if (rows == -1) {
            System.out.println(prompts.rows().useIndentOfExisting(createMatrixDir).quitMsg().txt());
            return;
        }

        // cols
        int cols = askUntilPositiveInt(
                prompts.useIndentOf(sizeNode).cols().ask().txt(),
                matSizeUpperBound,
                prompts.useIndentOf(sizeNode).cols().err().txt(matSizeUpperBound),
                prompts.cols().quitSpecifier().txt() // no need indent
        );

        if (cols == -1) {
            System.out.println(prompts.useIndentOfExisting(createMatrixDir).cols().quitMsg().txt());
            return;
        }

        // elements
        UiTextNode elementsNode = createMatrixDir.body().path("elements");
        // elementAt
        String name = section.getUnusedDefaultName();
        DMatrixRMaj mat = section.addMatrix(name, rows, cols);

        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                int rOut = (zeroIndexing) ? (r) : (r + 1);
                int cOut = (zeroIndexing) ? (c) : (c + 1);

                StringBuilder sb = new StringBuilder();
                sb.append(elementsNode.path("header").txt());
                sbAppendMat(sb, mat, elementsNode.path("mat").getIndentLev());

                System.out.println(sb);
                double elem = askUntilFiniteDouble(
                        prompts.useIndentOf(elementsNode).elementAt().ask().txt(rOut, cOut),
                        prompts.useIndentOf(elementsNode).elementAt().err().txt(), prompts.elementAt().quitSpecifier().txt() // no need indent
                );

                if (Double.isNaN(elem)) {
                    System.out.println(prompts.useIndentOf(elementsNode).elementAt().quitMsg().txt());
                    return;
                }
                mat.set(r, c, elem);
            }
        }

        // Final Success Message
        System.out.print(createMatrixDir.finishMsg().txt(section.getNumMatrices()));
        StringBuilder finalSb = new StringBuilder();
        sbAppendMat(finalSb, mat, createMatrixDir.body().path("mat").getIndentLev());
        System.out.println(finalSb.toString());
    }



    private boolean choiceLaxEq(String input, String choice) {
        if (input.equals(choice)) {
            return true;
        }
        String strippedInput = input.strip();
        if (strippedInput.equals(choice)) {
            return true;
        }
        String withPoint = choice + ".";
        return (input.equals(withPoint)
                || strippedInput.equals(withPoint)
        );
    }
    private Optional<String> choicesLaxEq(String input, String[] choiceKeys) {
        for (String key : choiceKeys) {
            if (input.equals(key)) {
                return Optional.of(key);
            }
        }
        String strippedInput = input.strip();
        for (String key : choiceKeys) {
            if (strippedInput.equals(key)) {
                return Optional.of(key);
            }
        }
        for (String key : choiceKeys) {
            if (input.equals(key + ".")
                    || strippedInput.equals(key + ".")
            ) {
                return Optional.of(key);
            }
        }
        return Optional.empty();
    }

    private void sbAppendMat(StringBuilder sb, DMatrixRMaj mat, int indentLev) {
        int[] colWidths = new int[mat.numCols];
        for (int c = 0; c < mat.numCols; ++c) {
            for (int r = 0; r < mat.numRows; ++r) {
                int need = String.valueOf(mat.get(r, c)).length();
                if (need > colWidths[c]) {
                    colWidths[c] = need;
                }
            }
        }
        sb.ensureCapacity(mat.numRows * (Arrays.stream(colWidths).sum() + 3 * mat.numCols + 5)); // approx.
        sb.repeat(" ", indentLev * indentSize)
                .append("[[");
        sbAppendMatRow(sb, mat, colWidths, 0);
        for (int r = 1; r < mat.numRows; ++r) {
            sb.append("]\n")
                    .repeat(" ", indentLev * indentSize + 1)
                    .append("[");
            sbAppendMatRow(sb, mat, colWidths, r);
        }
        sb.append("]]");
    }
    private void sbAppendMatRow(StringBuilder sb, DMatrixRMaj mat, int[] colWidths, int rowIndex) {
        int internalIndex = rowIndex * mat.numCols;
        sbAppendMatRowHelper(sb, String.valueOf(mat.get(internalIndex)), colWidths[0]);
        for (int c = 1; c < mat.numCols; ++c) {
            sb.append(",");
            sbAppendMatRowHelper(sb, String.valueOf(mat.get(internalIndex+c)), colWidths[c]);
        }
    }
    private void sbAppendMatRowHelper(StringBuilder sb, String s, int colWidth) {
        sb.repeat(" ", (colWidth - s.length())/2+1)
                .append(s)
                .repeat(" ", Utils.ceilDiv((colWidth - s.length()), 2) + 1);
    }


    private String askUntilMatch(
            @NonNull String[] keys,
            @NonNull String askMsg,
            @NonNull String incorrectMsg,
            @NonNull BiFunction<String, String[], Optional<String>> matchFinder
    ) {
        System.out.print(askMsg);
        while (true) {
            Optional<String> input = matchFinder.apply(scanner.next(), keys);
            if (input.isPresent()) {
                return input.get();
            }
            System.out.print(incorrectMsg + askMsg);
        }
    }

    private int askUntilPositiveInt(String askMsg, int upperBound, String incorrectMsg, String quitChar) {
        System.out.print(askMsg);
        while (true) {
            if (!scanner.hasNextInt()) {
                String input = scanner.next();
                if (input.equals(quitChar)) {
                    return -1;
                }
                System.out.print(incorrectMsg + askMsg);
                continue;
            }
            int value = scanner.nextInt();
            if (value > 0 && value < upperBound) {
                return value;
            }
            System.out.print(incorrectMsg + askMsg);
        }
    }

    private int askUntilInt(String askMsg, String incorrectMsg) {
        System.out.print(askMsg);
        while (!scanner.hasNextInt()) {
            System.out.print(incorrectMsg + askMsg);
            scanner.next();
        }
        return scanner.nextInt();
    }

    private double askUntilFiniteDouble(String askMsg, String incorrectMsg, String quitChar) {
        System.out.print(askMsg);
        while (true) {
            if (!scanner.hasNextDouble()) {
                String input = scanner.next();
                if (input.equals(quitChar)) {
                    return Double.NaN;
                }
                System.out.print(incorrectMsg + askMsg);
                continue;
            }
            double d = scanner.nextDouble();
            if (Double.isFinite(d)) {
                return d;
            }
            System.out.print(incorrectMsg + askMsg);
            // continue
        }
    }
    private double askUntilDouble(String askMsg, String incorrectMsg) {
        System.out.print(askMsg);
        while (!scanner.hasNextDouble()) {
            System.out.print(incorrectMsg + askMsg);
            scanner.next();
        }
        return scanner.nextDouble();
    }


}
