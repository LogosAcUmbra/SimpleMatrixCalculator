package me.LogosAcUmbra.SimpleMatrixCalculator;

import me.LogosAcUmbra.UiText.*;
import me.LogosAcUmbra.Utils.Utils;
import org.ejml.data.DMatrixRMaj;

import java.util.Arrays;
import java.util.Scanner;


public class Interactor {
    private final int indentSize = 2;
    private int currentIndentLev = 0;
    private final Scanner scanner = new Scanner(System.in);
    private final Section section = new Section();
    private final UiTextManager textManager;
    private final boolean zeroIndexing = false;
    private final int matSizeUpperBound = 20;

    public Interactor() {
        textManager = UiTextManager.getInstance();
    }


    public void start() {
        createMatrix();
        menu();
        scanner.next(); // pause to let me see result of program
    }

    private void menu() {
        DirNode menuDir = textManager.root().dirs().menu();
        System.out.print(menuDir.title().txt());
        System.out.print(menuDir.body().path("choices").txt());
    }

    private void createMatrix() {
        PromptGroupNode prompts = textManager.root().prompts();
        DirNode createMatrixDir = textManager.root().dirs().createMatrix();
        // title
        System.out.print(createMatrixDir.title().txt());

        // size header
        UiTextNode sizeHeaderNode = createMatrixDir.body().path("sizeHeader");
        PromptGroupNode sizeIndentedPrompts = prompts.useIndentOf(sizeHeaderNode);
        System.out.print(sizeHeaderNode.txt());
        // rows
        PromptNode rowsNode = sizeIndentedPrompts.rows();
        int rows = askUntilPositiveInt(
                rowsNode.ask().txt(),
                matSizeUpperBound,
                prompts.rows().quitSpecifier().txt(), // no need indent
                rowsNode.quitMsg().txt()
        );
        createMatrixDir.interruptMsg().txt();

        if (rows == -1) {
            System.out.println(prompts.rows().useIndentOf(createMatrixDir).quitMsg().txt());
            return;
        }

        // cols
        PromptNode colsNode = sizeIndentedPrompts.cols();
        int cols = askUntilPositiveInt(
                colsNode.ask().txt(),
                matSizeUpperBound,
                prompts.cols().quitSpecifier().txt(), // no need indent
                colsNode.err().txt(matSizeUpperBound)
        );

        if (cols == -1) {
            System.out.println(prompts.cols().useIndentOf(createMatrixDir).quitMsg().txt());
            currentIndentLev = 0;
            return;
        }

        // elements header
        UiTextNode elementsHeaderNode = createMatrixDir.body().path("elementsHeader");
        PromptGroupNode elementsIndentedPrompts = prompts.useIndentOf(elementsHeaderNode);
        // elementAt
        PromptNode elementAtNode = elementsIndentedPrompts.elementAt();
        String name = section.getUnusedDefaultName();
        DMatrixRMaj mat = section.addMatrix(name, rows, cols);

        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                int rOut = (zeroIndexing) ? (r) : (r + 1);
                int cOut = (zeroIndexing) ? (c) : (c + 1);

                StringBuilder sb = new StringBuilder();
                sb.append(elementsHeaderNode.txt());
                sbAppendMat(sb, mat, 2);

                System.out.println(sb);
                double elem = askUntilFiniteDouble(
                        elementAtNode.ask().txt(rOut, cOut),
                        prompts.elementAt().quitSpecifier().txt(), // no need indent
                        elementAtNode.err().txt()
                );

                if (Double.isNaN(elem)) {
                    System.out.println(createMatrixDir.interruptMsg().txt());
                    return;
                }
                mat.set(r, c, elem);
            }
        }

        // Final Success Message
        System.out.print(createMatrixDir.finishMsg().txt(section.getNumMatrices()));
        StringBuilder finalSb = new StringBuilder();
        sbAppendMat(finalSb, mat, 1);
        System.out.println(finalSb.toString());
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


    private int askUntilPositiveInt(String askMsg, int upperBound, String quitChar, String incorrectMsg) {
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

    private double askUntilFiniteDouble(String askMsg, String quitChar, String incorrectMsg) {
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
