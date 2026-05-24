package me.LogosAcUmbra.SimpleMatrixCalculator;

import me.LogosAcUmbra.UiText.*;
import me.LogosAcUmbra.Utils.Utils;
import org.ejml.data.DMatrixRMaj;

import java.io.IOException;
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

    public Interactor() throws IOException {
        textManager = UiTextManager.getInstance();
    }


    public void start() {
        createMatrix();
        scanner.next(); // pause to let me see result of program
    }


//    /**
//     * New Helper: Simplifies getting a message and applying indentation + arguments.
//     */
//    private String getMsg(String key, String indent, Object... args) {
//        // We combine the indent and the args into one array for String.format
//        Object[] formatArgs = new Object[args.length + 1];
//        formatArgs[0] = indent;
//        System.arraycopy(args, 0, formatArgs, 1, args.length);
//        return String.format(messages.getString(key), formatArgs);
//    }

    private void createMatrix() {
        PromptGroupNode prompts = textManager.root().prompts();
        DirNode createMatrixDir = textManager.root().dirs().createMatrix();
        // title
        System.out.print(createMatrixDir.title().get());

        // size header
        UiTextNode sizeHeaderNode = createMatrixDir.body().path("sizeHeader");
        PromptGroupNode sizeIndentedPrompts = prompts.useIndentOf(sizeHeaderNode);
        System.out.print(sizeHeaderNode.get());
        // rows
        PromptNode rowsNode = sizeIndentedPrompts.rows();
        int rows = askUntilPositiveInt(
                rowsNode.ask().get(),
                matSizeUpperBound,
                prompts.rows().quitSpecifier().get(), // no need indent
                rowsNode.quitMsg().get()
        );
        createMatrixDir.interruptMsg().get();

        if (rows == -1) {
            System.out.println(prompts.rows().useIndentOf(createMatrixDir).quitMsg());
            return;
        }

        // cols
        PromptNode colsNode = sizeIndentedPrompts.cols();
        int cols = askUntilPositiveInt(
                colsNode.ask().get(),
                matSizeUpperBound,
                prompts.cols().quitSpecifier().get(), // no need indent
                colsNode.err().get(matSizeUpperBound)
        );

        if (cols == -1) {
            System.out.println(prompts.cols().quitMsg());
            currentIndentLev = 0;
            return;
        }

        // elements header
        UiTextNode elementsHeaderNode = createMatrixDir.body().path("elementsHeader");
        PromptGroupNode elementsIndentedPrompts = prompts.useIndentOf(elementsHeaderNode);
        // elementAt
        PromptNode elementAtNode = elementsIndentedPrompts.elementAt();
        String name = "A";
        section.addMatrix(name, rows, cols);
        DMatrixRMaj mat = section.getMat(name).orElseThrow();

        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                int rOut = (zeroIndexing) ? (r) : (r + 1);
                int cOut = (zeroIndexing) ? (c) : (c + 1);

                StringBuilder sb = new StringBuilder();
                sb.append(elementsHeaderNode.get());
                sbAppendMat(sb, mat, 2);

                System.out.println(sb);
                double elem = askUntilFiniteDouble(
                        elementAtNode.ask().get(rOut, cOut),
                        prompts.elementAt().quitSpecifier().get(), // no need indent
                        elementAtNode.err().get()
                );

                if (Double.isNaN(elem)) {
                    System.out.println(createMatrixDir.interruptMsg().get());
                    return;
                }
                mat.set(r, c, elem);
            }
        }

        // Final Success Message
        System.out.print(createMatrixDir.finishMsg().get(section.getNumMatrices()));
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
            continue;
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
