/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.actions;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A condition tree specifies in which order a group of conditions will be
 * executed.
 *
 * @author chris
 */
public class ConditionTree {

    public static enum OPERATION {
        AND, OR, VAR, NOT, NOOP
    }
       
    private ConditionTree leftArg = null;

    private ConditionTree rightArg = null;

    private int argument = -1;

    private OPERATION op;
    
    private ConditionTree(final OPERATION op, final ConditionTree leftArg,
            final ConditionTree rightArg) {
        assert(op != OPERATION.VAR);
        assert(op != OPERATION.NOT);
        assert(leftArg != null);
        assert(rightArg != null);
        
        this.op = op;
        this.leftArg = leftArg;
        this.rightArg = rightArg;
    }
    
    private ConditionTree(final OPERATION op, final ConditionTree argument) {
        assert(op == OPERATION.NOT);
        
        this.op = op;
        this.leftArg = argument;
    }    
    
    private ConditionTree(final int argument) {
        this.op = OPERATION.VAR;
        this.argument = argument;
    }
    
    private ConditionTree() {
        this.op = OPERATION.NOOP;
    }
    
    /**
     * Retrieves the highest argument number that is used in this condition tree.
     * 
     * @return The highest argument number used in this tree
     */
    public int getMaximumArgument() {
        if (this.op == OPERATION.NOOP) {
            return 0;
        } else if (this.op == OPERATION.VAR) {
            return argument;
        } else if (this.op == OPERATION.NOT) {
            return leftArg.getMaximumArgument();
        } else {
            return Math.max(leftArg.getMaximumArgument(), rightArg.getMaximumArgument());
        }
    }

    /**
     * Evaluates this tree with the specified conditions. Returns the result
     * of the evaluation.
     * 
     * @param conditions The binary values of each of the conditions used in
     * this three
     * @return The result of the evaluation of this tree
     */
    public boolean evaluate(final boolean[] conditions) {
        switch (op) {
            case VAR:
                return conditions[argument];
            case NOT:
                return !leftArg.evaluate(conditions);
            case AND:
                return leftArg.evaluate(conditions) && rightArg.evaluate(conditions);
            case OR:
                return leftArg.evaluate(conditions) || rightArg.evaluate(conditions);
            default:
                return true;
        }
    }
    
    /**
     * Retrieves a String representation of this ConditionTree. The string
     * representation is a normalised formula describing this tree and all of
     * its children. The output of this method will generate an identical tree
     * if passed to parseString.
     * 
     * @return A string representation of this tree
     */
    @Override
    public String toString() {
        switch (op) {
        case NOOP:
            return "";
        case VAR:
            return String.valueOf(argument);
        case NOT:
            return "!" + leftArg;
        case AND:
            return "(" + leftArg + "&" + rightArg + ")";
        case OR:
            return "(" + leftArg + "|" + rightArg + ")";
        default:
            return "<unknown>";
        }
    }    
    
    /**
     * Parses the specified string into a condition tree.
     * 
     * @param string The string to be parsed
     * @return The corresponding condition tree, or null if there as an error
     * while parsing the data
     */
    public static ConditionTree parseString(final String string) {
        final Deque<Object> stack = new ArrayDeque<Object>();
        
        for (int i = 0; i < string.length(); i++) {
            final char m = string.charAt(i);
            
            if (isInt(m)) {
                String temp = "" + m;
                
                while (i + 1 < string.length() && isInt(string.charAt(i + 1))) {
                    temp = temp + string.charAt(i + 1);
                    i++;
                }
                
                stack.add(new ConditionTree(Integer.parseInt(temp)));
            } else if (m != ' ' && m != '\t' && m != '\n' && m != '\r') {
                stack.add(m);
            }
        }
        
        return parseStack(stack);
    }
    
    @SuppressWarnings("fallthrough")
    private static ConditionTree parseStack(final Deque<Object> stack) {
        final Deque<Object> myStack = new ArrayDeque<Object>();
        
        while (!stack.isEmpty()) {
            final Object object = stack.poll();
            
            if (object instanceof Character && ((Character) object) == ')') {
                final ConditionTree bracket = readBracket(myStack);

                if (bracket == null) {
                    return null;
                } else {
                    myStack.add(bracket);
                }
            } else {
                myStack.add(object);
            }
        }
        
        while (!myStack.isEmpty()) {
            switch (myStack.size()) {
                case 1:
                    final Object first = myStack.pollFirst();
                    if (first instanceof ConditionTree) {
                        return (ConditionTree) first;
                    }            
                case 0:
                    return null;
            }
            
            final ConditionTree first = readTerm(myStack);
            
            if (first == null) {
                return null;
            } else if (myStack.isEmpty()) {
                return first;
            }
            
            final Object second = myStack.pollFirst();
            
            if (!myStack.isEmpty()) {
                final ConditionTree third = readTerm(myStack);
                
                if (first != null && third != null && second instanceof Character) {
                    OPERATION op;
                    
                    if ((Character) second == '&') {
                        op = OPERATION.AND;
                    } else if ((Character) second == '|') {
                        op = OPERATION.OR;
                    } else {
                        return null;
                    }
                    
                    myStack.addFirst(new ConditionTree(op, first, third));
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        
        return new ConditionTree();
    }
    
    private static ConditionTree readTerm(final Deque<Object> stack) {
        final Object first = stack.pollFirst();
        
        if (first instanceof Character && (Character) first == '!') {
            if (stack.isEmpty()) {
                return null;
            }
            
            return new ConditionTree(OPERATION.NOT, readTerm(stack));
        } else {
            if (!(first instanceof ConditionTree)) {
                return null;
            }
            
            return (ConditionTree) first;
        }
    }
    
    private static ConditionTree readBracket(final Deque<Object> stack) {
        final Deque<Object> tempStack = new ArrayDeque<Object>();
        boolean found = false;
        
        while (!found && !stack.isEmpty()) {
            final Object object = stack.pollLast();
            
            if (object instanceof Character && ((Character) object) == '(') {
                found = true;
            } else {
                tempStack.addFirst(object);
            }
        }
        
        if (!found) {
            return null;
        } else {
            return parseStack(tempStack);
        }
    }
    
    private static boolean isInt(final char target) {
        return target >= '0' && target <= '9';
    }
    
    /**
     * Creates a condition tree by disjoining the specified number of arguments
     * together.
     * 
     * @param numArgs The number of arguments to be disjoined
     * @return The corresponding condition tree
     */
    public static ConditionTree createDisjunction(final int numArgs) {
        final StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < numArgs; i++) {
            if (builder.length() != 0) {
                builder.append('|');
            }
            
            builder.append(i);
        }
        
        return parseString(builder.toString());        
    }

    /**
     * Creates a condition tree by conjoining the specified number of arguments
     * together.
     * 
     * @param numArgs The number of arguments to be conjoined
     * @return The corresponding condition tree
     */    
    public static ConditionTree createConjunction(final int numArgs) {
        final StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < numArgs; i++) {
            if (builder.length() != 0) {
                builder.append('&');
            }
            
            builder.append(i);
        }
        
        return parseString(builder.toString());
    }
       
}