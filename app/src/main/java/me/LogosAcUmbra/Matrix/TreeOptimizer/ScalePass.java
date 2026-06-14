package me.LogosAcUmbra.Matrix.TreeOptimizer;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanStack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.LogosAcUmbra.Matrix.OperandTree.OperandTreeList;
import me.LogosAcUmbra.Matrix.SymMatrixExpr.*;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;

public class ScalePass implements OptimizationPass {

    @Override
    public @NonNull SymMatrixAdvancedExpr apply(@NonNull SymMatrixAdvancedExpr root, @NonNull ExprEvaluator symjaExprEvaluator) {
        SymMatrixAdvancedExpr optimized = optimize(root, symjaExprEvaluator);
        if (optimized == null) {
            return ZeroExpr.ofSize(root.getNumRows(), root.getNumCols());
        }
        return optimized;
    }

    private @Nullable SymMatrixAdvancedExpr optimize(@NonNull SymMatrixAdvancedExpr root, @NonNull ExprEvaluator symjaExprEvaluator) {
        final int ZERO_OP_SPECIFIER = -1;
        final int SCALE_OP_SPECIFIER = -2;
        final int ONE_OP_NOT_SCALE_SPECIFIER = -3;
        assert SCALE_OP_SPECIFIER != ONE_OP_NOT_SCALE_SPECIFIER && SCALE_OP_SPECIFIER != ZERO_OP_SPECIFIER && ONE_OP_NOT_SCALE_SPECIFIER != ZERO_OP_SPECIFIER;
        assert SCALE_OP_SPECIFIER < 0 && ONE_OP_NOT_SCALE_SPECIFIER < 0 && ZERO_OP_SPECIFIER < 0;

        // err yes, we have 4 parallel lists / stacks
        ObjectArrayList<@NonNull SymMatrixAdvancedExpr> nodesDealing = new ObjectArrayList<>();
        IntArrayList numChildrenDealtOfNodes = new IntArrayList();
        BooleanArrayList isChildCalledOfNodes = new BooleanArrayList();
        Stack<@Nullable ObjectArrayList<SymMatrixAdvancedExpr>> buildersOfManyOpNodes = new ObjectArrayList<>();

        // align root with conditions in loop
        if (root instanceof ZeroExpr) {
            return null;
        }
        optimizeHelper_pushOperand(
                root,
                nodesDealing, numChildrenDealtOfNodes, isChildCalledOfNodes, buildersOfManyOpNodes,
                ZERO_OP_SPECIFIER, SCALE_OP_SPECIFIER, ONE_OP_NOT_SCALE_SPECIFIER
        );

        SymMatrixAdvancedExpr returnVal = null; // initialize with null for compile, but logically, should be no need to initialize

        while (!nodesDealing.isEmpty()) { // this whole loop uses unsafeSetOperand or unsafeSetOperands

            final SymMatrixAdvancedExpr topExpr = nodesDealing.top();
            final int numChildrenDealt = numChildrenDealtOfNodes.topInt();
            final boolean isChildCalled = isChildCalledOfNodes.topBoolean();

            if (!isChildCalled) {
                if (numChildrenDealt == ZERO_OP_SPECIFIER) {
                    // # Unwind
                    returnVal = null;
                    optimizeHelper_pop(nodesDealing, numChildrenDealtOfNodes, isChildCalledOfNodes, buildersOfManyOpNodes);
                    continue;
                }
                if (numChildrenDealt == SCALE_OP_SPECIFIER) {
                    // if topExpr instanceof ScaleExpr
                    // deal with current expr
                    ScaleExpr topFlattenedScale = flattenScaleExpr( (ScaleExpr) topExpr );
                    if (topFlattenedScale == null) {
                        // # Unwind
                        returnVal = null;
                        optimizeHelper_pop(nodesDealing, numChildrenDealtOfNodes, isChildCalledOfNodes, buildersOfManyOpNodes);
                        continue;
                    }
                    IExpr evaluatedScalar = symjaExprEvaluator.eval(topFlattenedScale.getScalar());
                    if (evaluatedScalar.equals(F.C0)) {
                        // # Unwind
                        returnVal = null;
                        optimizeHelper_pop(nodesDealing, numChildrenDealtOfNodes, isChildCalledOfNodes, buildersOfManyOpNodes);
                        continue;
                    }

                    SymMatrixAdvancedExpr operand = topFlattenedScale.getOperandRef();
                    ScaleExpr topEvaluatedFlattenedScale = ScaleExpr.of(operand, evaluatedScalar);
                    arrayListHelper_setTopOf(nodesDealing, topEvaluatedFlattenedScale);
                    // deal with children (recursion)
                    // the following is actually the same as branch ONE_OP_NOT_SCALE_SPECIFIER
                    {
                        // set current expr to childCalled (have called optimize recursively to child)
                        arrayListHelper_setTopOf(isChildCalledOfNodes, true);
                        // # Wind-up
                        optimizeHelper_pushOperand(
                                operand,
                                nodesDealing, numChildrenDealtOfNodes, isChildCalledOfNodes, buildersOfManyOpNodes,
                                ZERO_OP_SPECIFIER, SCALE_OP_SPECIFIER, ONE_OP_NOT_SCALE_SPECIFIER);
                    }
                    continue;
                } // end bracket of: if topExpr instanceof ScaleExpr

                if (numChildrenDealt == ONE_OP_NOT_SCALE_SPECIFIER) {
                    // if topExpr instanceof OneOperandExpr
                    SymMatrixAdvancedExpr operand = ((OneOperandExpr) topExpr).getOperandRef();
                    {
                        // set current expr to childCalled (have called optimize recursively to child)
                        arrayListHelper_setTopOf(isChildCalledOfNodes, true);
                        // # Wind-up
                        optimizeHelper_pushOperand(
                                operand,
                                nodesDealing, numChildrenDealtOfNodes, isChildCalledOfNodes, buildersOfManyOpNodes,
                                ZERO_OP_SPECIFIER, SCALE_OP_SPECIFIER, ONE_OP_NOT_SCALE_SPECIFIER);
                    }
                    continue;
                } // end bracket of: if topExpr instanceof OneOperandExpr

                assert topExpr instanceof ManyOperandExpr;

                ManyOperandExpr topManyExpr = (ManyOperandExpr) topExpr;
                OperandTreeList operands = topManyExpr.getOperandsRef();

                if (operands.isEmpty()) {
                    // # Unwind
                    returnVal = null;
                    optimizeHelper_pop(nodesDealing, numChildrenDealtOfNodes, isChildCalledOfNodes, buildersOfManyOpNodes);
                    continue;
                }

                if (numChildrenDealt == 0) {
                    operands = operands.toCollapsedAndUpdateParent();
                    topManyExpr.unsafeSetOperands(operands);
                }

                assert numChildrenDealt >= 0 && numChildrenDealt < operands.size();
                SymMatrixAdvancedExpr operand = operands.get(  numChildrenDealt  );
                // the following is actually the same as branch ONE_OP_NOT_SCALE_SPECIFIER
                {
                    // set current expr to childCalled (have called optimize recursively to child)
                    arrayListHelper_setTopOf(isChildCalledOfNodes, true);
                    // # Wind-up
                    optimizeHelper_pushOperand(
                            operand,
                            nodesDealing, numChildrenDealtOfNodes, isChildCalledOfNodes, buildersOfManyOpNodes,
                            ZERO_OP_SPECIFIER, SCALE_OP_SPECIFIER, ONE_OP_NOT_SCALE_SPECIFIER);
                }
                continue;
            } // end bracket of: if !isChildCalled

            // isChildCalled
            assert !(topExpr instanceof ZeroExpr);
            if (numChildrenDealt == SCALE_OP_SPECIFIER) {
                ScaleExpr topEvaluatedFlattenedScale = (ScaleExpr) topExpr;

                // return topEvaluatedFlattenedScale.operand (reduce a useless scale layer) if exprScale has scalar of 1
                if (topEvaluatedFlattenedScale.getScalar().equals(F.C1)) {
                    // # Unwind
                    // returnVal = returnVal; // let the returnVal return up without modifying
                    optimizeHelper_pop(nodesDealing, numChildrenDealtOfNodes, isChildCalledOfNodes, buildersOfManyOpNodes);
                    continue;
                }
                // MAY NOT BE continued (MAY NOT go back up)
            } // end bracket of: if (numChildrenDealt == SCALE_OP_SPECIFIER)
            if (numChildrenDealt == SCALE_OP_SPECIFIER
                    || numChildrenDealt == ONE_OP_NOT_SCALE_SPECIFIER) { // if OneOp (including Scale)
                assert topExpr instanceof OneOperandExpr; // this assert is useless, if I am correct. so this assert is for if I am incorrect

                if (returnVal == null) {
                    // # Unwind
                    // return null
                    optimizeHelper_pop(nodesDealing, numChildrenDealtOfNodes, isChildCalledOfNodes, buildersOfManyOpNodes);
                    continue;
                }
                OneOperandExpr topOneOp = (OneOperandExpr) topExpr;
                topOneOp.unsafeSetOperand(returnVal);
                // # Unwind
                returnVal = topOneOp;
                optimizeHelper_pop(nodesDealing, numChildrenDealtOfNodes, isChildCalledOfNodes, buildersOfManyOpNodes);
                continue;
            }
            // for this branch: topExpr is instanceof ManyOperandExpr
            assert topExpr instanceof ManyOperandExpr; // this assert should be useless

            ObjectArrayList<SymMatrixAdvancedExpr> opBuilder = buildersOfManyOpNodes.top();
            ManyOperandExpr topManyOp = (ManyOperandExpr) topExpr;
            assert numChildrenDealt < topManyOp.getOperandsRef().size();
            assert opBuilder != null;

            if (returnVal == null) { // do nth
            } else {
                opBuilder.add(  returnVal  );
            }
            int updatedNumChildrenDealt = numChildrenDealt + 1;
            if (updatedNumChildrenDealt < topManyOp.getOperandsRef().size()) { // current manyOpExpr not finished
                // # Not Wind-up Or Unwind
                arrayListHelper_setTopOf(numChildrenDealtOfNodes, updatedNumChildrenDealt);
                arrayListHelper_setTopOf(isChildCalledOfNodes, false);
                // let the next loop do the recursive call to child
                continue;
            }
            // current manyOpExpr is finished
            OperandTreeList optimizedOperands = OperandTreeList.of(  opBuilder  );
            topManyOp.unsafeSetOperands(  optimizedOperands  );
            // # Unwind
            returnVal = topManyOp;
            optimizeHelper_pop(nodesDealing, numChildrenDealtOfNodes, isChildCalledOfNodes, buildersOfManyOpNodes);
            continue;
        } // end bracket of: while (!stack.empty())
        return returnVal;
    }

    private void optimizeHelper_pushOperand(
            @NonNull SymMatrixAdvancedExpr operand,
            Stack<@NonNull SymMatrixAdvancedExpr> nodesDealing,
            IntStack numChildrenDealtOfNodes,
            BooleanArrayList isChildCalledOfNodes,
            Stack<@Nullable ObjectArrayList<SymMatrixAdvancedExpr>> buildersOfManyOpNodes,
            final int ZERO_OP_SPECIFIER,
            final int SCALE_OP_SPECIFIER,
            final int ONE_OP_NOT_SCALE_SPECIFIER
    ) {

        nodesDealing.push(operand);
        isChildCalledOfNodes.push(false);

        switch (operand) {
            // # Wind-up
            case ZeroExpr ignored: {
                numChildrenDealtOfNodes.push(ZERO_OP_SPECIFIER);
                buildersOfManyOpNodes.push(null);
                return;
            }
            // # Wind-up
            case ScaleExpr ignored: {
                numChildrenDealtOfNodes.push(SCALE_OP_SPECIFIER);
                buildersOfManyOpNodes.push(null);
                return;
            }
            // # Wind-up
            case OneOperandExpr ignored: {
                numChildrenDealtOfNodes.push(ONE_OP_NOT_SCALE_SPECIFIER);
                buildersOfManyOpNodes.push(null);
                return;
            }
            // # Wind-up
            case ManyOperandExpr exprManyOp: {
                numChildrenDealtOfNodes.push(0);
                buildersOfManyOpNodes.push(new ObjectArrayList<>(  exprManyOp.getOperandsRef().size()  ));
                return;
            }
            default: throw new IllegalStateException("UnreachableCodeReached: illegal type of operand. Info: { operand: " + operand + " }");
        }
    }

    private void optimizeHelper_pop(
            Stack<@NonNull SymMatrixAdvancedExpr> nodesDealing,
            IntStack numChildrenDealtOfNodes,
            BooleanStack isChildCalledOfNodes,
            Stack<@Nullable ObjectArrayList<SymMatrixAdvancedExpr>> buildersOfManyOpNodes
    ) {
        nodesDealing.pop();
        buildersOfManyOpNodes.pop();
        numChildrenDealtOfNodes.popInt();
        isChildCalledOfNodes.popBoolean();
    }

//    private @Nullable SymMatrixAdvancedExpr optimizeRecursive(@NonNull SymMatrixAdvancedExpr expr, @NonNull ExprEvaluator symjaExprEvaluator) {
//        switch (expr) {
//            case ScaleExpr exprScale: {
//                // exprScale (current layer)
//                // flatten the exprScale
//                ScaleExpr exprFlattenedScale = flattenScaleExpr(exprScale);
//                // return null if exprScale is Zero
//                if (exprFlattenedScale == null) {
//                    return null;
//                }
//                IExpr evaluatedScalar = symjaExprEvaluator.eval(exprFlattenedScale.getScalar());
//                if (evaluatedScalar.equals(F.C0)) {
//                    return null;
//                }
//
//                // operand (child layer)
//                SymMatrixAdvancedExpr operand = exprFlattenedScale.getOperandRef();
//                SymMatrixAdvancedExpr operandOptimized = optimize(operand, symjaExprEvaluator);
//                // return null if operand is Zero
//                if (operandOptimized == null) {
//                    return null;
//                }
//                // return operand (reduce a useless scale layer) if exprScale has scalar of 1
//                if (evaluatedScalar.equals(F.C1)) {
//                    return operandOptimized;
//                }
//                // return a new ScaleExpr instance with optimized operand and evaluated scalar
//                return ScaleExpr.of(operandOptimized, evaluatedScalar);
//            }
//            case OneOperandExpr exprOneOp: {
//                // operand (child layer)
//                SymMatrixAdvancedExpr operand = exprOneOp.getOperandRef();
//                SymMatrixAdvancedExpr operandOptimized = optimize(operand, symjaExprEvaluator);
//                // return null if operand is Zero
//                if (operandOptimized == null) {
//                    return null;
//                }
//                // return the IOneOperandExpr instance with optimized operand
//                return exprOneOp.unsafeSetOperand(operandOptimized);
//            }
//            case ManyOperandExpr exprManyOp: {
//                OperandTreeList operands = exprManyOp.getOperandsRef().toCollapsed();
//                ObjectArrayList<SymMatrixAdvancedExpr> result = new ObjectArrayList<>(operands.size());
//
//                for (SymMatrixAdvancedExpr operand : operands) {
//                    SymMatrixAdvancedExpr optimized = optimize(operand, symjaExprEvaluator);
//                    if (optimized != null) { // null implies 0
//                        result.add(optimized);
//                    }
//                }
//                // return the IManyOperandExpr instance with optimized operands
//                return exprManyOp.unsafeSetOperands(OperandTreeList.of(result));
//            }
//            case ZeroExpr zero: {
//                return null;
//            }
//            default: {
//                throw new IllegalStateException("UnreachableCodeReached");
//            }
//        }
//    }

    /**
     * flattens all continuous flatten-able layer of expr below the given scaleExpr <br>
     * <br>
     * Flatten-able: ScaleExpr, NegExpr
     * @param scaleExpr the scaleExpr
     * @return if the given scaleExpr has operand of ZeroExpr, null. <br>
     * If the given scaleExpr cannot be flattened: scaleExpr. <br>
     * else: a flattened new ScaleExpr instance
     */
    private @Nullable ScaleExpr flattenScaleExpr(@NonNull ScaleExpr scaleExpr) {
        SymMatrixExpr operandOfScale = scaleExpr.getOperandRef();
        if (operandOfScale instanceof ZeroExpr) {
            return null;
        }
        IExpr scalar = scaleExpr.getScalar();
        return switch (operandOfScale) {
            case NegExpr negOperandOfScale ->
                    flattenToScaleFromNeg(negOperandOfScale, scalar);
            case ScaleExpr scaleOperandOfScale -> // logger.log("double layer of scale found when ScalePass of tree-optimizing")
                    flattenToScaleFromScale(scaleOperandOfScale, scalar);
            default -> scaleExpr; // cannot flatten
        };
    }

    /**
     * for each function call, we do: <br>
     * <br>
     * if the {@code scaleExpr.operand} is ZeroExpr (expr of zero matrix): {@code return null} <br>
     * <br>
     * else, if the operand is flatten-able (ScaleExpr or NegExpr):
     * according to the type of scaleExpr,
     * we recursively calls and {@code return} this function or {@link #flattenToScaleFromNeg}
     * with arguments: <br>
     * child={@code scaleExpr.operand}, and
     * scalar={@link F#Times}( {@code scalar}, {@code scaleExpr.scalar} ) <br>
     * <br>
     * else (the operand is not flatten-able), {@code return} a new ScalarExpr instance with <br>
     * operand=scaleExpr.operand, and scalar={@link F#Times}( {@code scalar}, {@code scaleExpr.scalar} )
     * @param scaleExpr the scaleExpr with its scalar going to be extracted
     * @param scalar the scalar from parents of scaleExpr
     * @return a flattened new ScaleExpr instance
     */
    private @Nullable ScaleExpr flattenToScaleFromScale(@NonNull ScaleExpr scaleExpr, @NonNull IExpr scalar) {
        SymMatrixExpr operandOfScale = scaleExpr.getOperandRef();
        if (operandOfScale instanceof ZeroExpr) {
            return null;
        }
        IExpr newScalar = F.Times(scalar, scaleExpr.getScalar());
        return switch (operandOfScale) {
            case NegExpr negOperandOfScale ->
                    flattenToScaleFromNeg(negOperandOfScale, newScalar);
            case ScaleExpr scaleOperandOfScale -> // logger.log("double layer of scale found when ScalePass of tree-optimizing")
                    flattenToScaleFromScale(scaleOperandOfScale, newScalar);
            default -> ScaleExpr.of(operandOfScale, newScalar); // cannot flatten further
        };
    }

    private @Nullable ScaleExpr flattenToScaleFromNeg(@NonNull NegExpr negExpr, @NonNull IExpr scalar) {
        SymMatrixExpr operandOfScale = negExpr.getOperandRef();
        if (operandOfScale instanceof ZeroExpr) {
            return null;
        }
        IExpr newScalar = F.Times(scalar, F.CN1);
        return switch (operandOfScale) {
            case NegExpr negOperandOfScale ->
                    // logger.log("double layer of neg found when ScalePass of tree-optimizing")
                    flattenToScaleFromNeg(negOperandOfScale, newScalar);
            case ScaleExpr scaleOperandOfScale ->
                    flattenToScaleFromScale(scaleOperandOfScale, newScalar);
            default -> ScaleExpr.of(operandOfScale, newScalar); // cannot flatten further
        };
    }

    private void arrayListHelper_setTopOf(IntArrayList intArrayList, int val) {
        intArrayList.set(intArrayList.size()-1, val);
    }
    private void arrayListHelper_setTopOf(BooleanArrayList booleanArrayList, boolean val) {
        booleanArrayList.set(booleanArrayList.size()-1, val);
    }
    private <T> void arrayListHelper_setTopOf(ObjectArrayList<T> objectArrayList, T val) {
        objectArrayList.set(objectArrayList.size()-1, val);
    }


}
