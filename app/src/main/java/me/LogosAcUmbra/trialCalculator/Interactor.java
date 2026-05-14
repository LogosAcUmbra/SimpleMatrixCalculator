package me.LogosAcUmbra.trialCalculator;

import me.LogosAcUmbra.Message.MessageManager;
import me.LogosAcUmbra.Message.PromptManager.PromptMsgType;
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
    private final MessageManager messages;
    private final boolean zeroIndexing = false;
    private final int matSizeUpperBound = 100;

    public Interactor() throws IOException {
        messages = new MessageManager(indentSize);
    }


    public void start() {
        createMatrix();
        scanner.next(); // pause to let me see result of program
    }

    // Helper to get spaces based on current level
    private String indent() {
        return " ".repeat(indentSize * currentIndentLev);
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
//        currentIndentLev = messages.dir.createMenu.indentLev();
//        System.out.print(messages.dir.createMenu.title());
//
//        currentIndentLev++;
//        System.out.print(messages.dir.createMenu.txt().get("sizeHeader"));
//
//        currentIndentLev++;
        // Rows
        int rows = askUntilPositiveInt(
                messages.prompt().rows(PromptMsgType.ASK),
                matSizeUpperBound,
                messages.prompt().rows(PromptMsgType.QUIT_SPECIFIER),
                messages.prompt().rows(PromptMsgType.ERR)
        );

        if (rows == -1) {
            System.out.println(messages.prompt().rows(PromptMsgType.QUIT_MSG));
            return;
        }

        // Cols - Updated to use properties
        int cols = askUntilPositiveInt(
                messages.prompt().cols(PromptMsgType.ASK),
                matSizeUpperBound,
                messages.prompt().cols(PromptMsgType.QUIT_SPECIFIER),
                messages.prompt().fCols(PromptMsgType.ERR, matSizeUpperBound)
        );

        if (cols == -1) {
            System.out.println(messages.prompt().cols(PromptMsgType.QUIT_MSG));
            currentIndentLev = 0;
            return;
        }

        String name = "A";
        section.addMatrix(name, rows, cols);
        DMatrixRMaj mat = section.getMat(name).orElseThrow();

        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                int rOut = (zeroIndexing) ? (r) : (r + 1);
                int cOut = (zeroIndexing) ? (c) : (c + 1);

                StringBuilder sb = new StringBuilder();
                sb.append(getMsg("elements.prompt0", "\n"));
                sbAppendMat(sb, mat, 2);

                // Element prompt
                String prompt = messages.prompt().fElementAt(PromptMsgType.ASK, rOut, cOut);
                String error = getMsg("elements.err", indent(1));

                double elem = askUntilFiniteDouble(
                        sb.toString() + prompt,
                        messages.getString("elementAt.quitChar"),
                        error
                );

                if (Double.isNaN(elem)) {
                    System.out.println(messages.getString("createMatrix.quitMsg"));
                    return;
                }
                mat.set(r, c, elem);
            }
        }
//
//        // Final Success Message
//        System.out.printf("\nMatrix %d created.\n", section.getNumMatrices());
//        StringBuilder finalSb = new StringBuilder();
//        sbAppendMat(finalSb, mat, 1);
//        System.out.println(finalSb.toString());
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
            }
            double d = scanner.nextDouble();
            if (Double.isFinite(d)) {
                return d;
            }
            System.out.print(incorrectMsg + askMsg);
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
