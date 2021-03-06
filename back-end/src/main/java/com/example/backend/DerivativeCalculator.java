package com.example.backend;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.lang.Character;
import java.lang.Math;
public class DerivativeCalculator {
    private final char[] operators = {'+', '*', '-', '/', '^'};

    private char var;
    public DerivativeCalculator(char var) {
        this.var = var;    
    }
    //  worry about input later

    // this is a testing function, ignore
    public ExpressionNode TestDerivative(String s) {
        ExpressionNode root = makeTree(s);
        root = derive(root);
        return root;
    }
    // input Formatting: ex. (x + 1)
    // this sets up the tree
    //
    public ExpressionNode makeTree(String s) {
        s = s.replaceAll(" ", "");
        s = '(' + s + ')' ;
        Stack<ExpressionNode> stN = new Stack<ExpressionNode>();
        Stack<String> stC = new Stack<>();

        ExpressionNode t, t1, t2;

        int[] p = new int[123];
        p['+'] = p['-'] = 1;
        p['/'] = p['*'] = 2;
        p['^'] = 3;
        p[')'] = 0;
        Map<String, Integer> opPriority = new HashMap<>();

        opPriority.put("+", 1);
        opPriority.put("-", 1);
        opPriority.put("/", 2);
        opPriority.put("*", 2);
        opPriority.put("^", 3);
        String checkNum = "";
        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) == '(') {

                // Push '(' in char stack
                stC.add("" + s.charAt(i));
            }

            // Push the operands in node stack
            else if (Character.isDigit(s.charAt(i)) || Character.isLetter(s.charAt(i)))
            {
                if (!Character.isDigit(s.charAt(i+1))) {
                    checkNum += s.charAt(i) ;
                    t = new ExpressionNode(checkNum);
                    stN.add(t);
                    checkNum = "";
                }
                else {
                    checkNum +=  s.charAt(i);
                }

            }
            else if (p[s.charAt(i)] > 0)
            {

                // If an operator with lower or
                // same associativity appears
                while (
                        opPriority.containsKey(stC.peek()) && !stC.isEmpty() && !stC.peek().equals("(")
                                && ((s.charAt(i) != '^' && opPriority.get(stC.peek()) >= p[s.charAt(i)])
                                || (s.charAt(i) == '^'
                                && opPriority.get(stC.peek()) > p[s.charAt(i)])))
                {

                    // Get and remove the top element
                    // from the character stack
                    t = new ExpressionNode(stC.peek());
                    stC.pop();

                    // Get and remove the top element
                    // from the node stack
                    t1 = stN.peek();
                    stN.pop();

                    // Get and remove the currently top
                    // element from the node stack
                    t2 = stN.peek();
                    stN.pop();

                    // Update the tree
                    t.left = t2;
                    t.right = t1;

                    // Push the node to the node stack
                    stN.add(t);
                }

                // Push s[i] to char stack
                stC.push(s.charAt(i) + "");
            }
            else if (s.charAt(i) == ')') {
                while (!stC.isEmpty() && !stC.peek().equals("("))
                {
                    t = new ExpressionNode(stC.peek());
                    stC.pop();
                    t1 = stN.peek();
                    stN.pop();
                    t2 = stN.peek();
                    stN.pop();
                    t.left = t2;
                    t.right = t1;
                    stN.add(t);
                }
                stC.pop();
            }
        }
        t = stN.peek();
        return t;
    }

    private ExpressionNode powerRule(ExpressionNode root) {
        ExpressionNode firstNode = new ExpressionNode("*");
        ExpressionNode exponent = new ExpressionNode(root.right.value);

        root.right = new ExpressionNode( root.right.value,"1", "-");
        ExpressionNode baseCopy = copyTree(root.left);
        firstNode.left = root;
        firstNode.right = new ExpressionNode(derive(baseCopy), exponent, "*");
        // right side, - 1, copy right side, make its parent a subtract, right child is 1
        // left side, multiply by old right side
        // also need to multiply by derivative of old left side
        return firstNode;
    }

    private ExpressionNode derive(ExpressionNode root) {
        if (root == null) {
            return null;
        }
        if (root.value.equals(var +"")) {
            root.value = "1";
        }
        else if (root.value.equals("+") || root.value.equals("-")){
            root.left = derive(root.left);
            root.right = derive(root.right);
        }

        else if (root.value.equals("/")){
            root = simplify(quotientRule(root));
        }
        else if (root.value.equals("*")){
            root = productRule(root);
        }
        else if (root.value.equals("^")){
            root = powerRule(root);
        }
        else {
            root.value = 0 + "";
            
        }
        /* first print data of node */
        // System.out.print(root.value + " ");

        /* then recur on left subtree */
        //derive(root.left);

        /* now recur on right subtree */
        //derive(root.right);
        return root;
    }
    private ExpressionNode productRule(ExpressionNode root) {
        root.value = "+";
        ExpressionNode rootLeftHolder = copyTree(root.left);
        ExpressionNode rootRightHolder = copyTree(root.right);
        root.left = new ExpressionNode(root.left, derive(root.right), "*");
        root.right = new ExpressionNode(rootRightHolder, derive(rootLeftHolder), "*");
        return root;
    }

    private ExpressionNode quotientRule(ExpressionNode root) {       
        // left side is like product rule but -

        // right side is original right side squared
        ExpressionNode rootLeftHolder = copyTree(root.left);
        ExpressionNode rootLeftHolder1 = copyTree(root.left);
        ExpressionNode rootRightHolder = copyTree(root.right);
        ExpressionNode rootRightHolder1 = copyTree(root.right);
        root.left = new ExpressionNode("*", "*", "-");
        root.left.left = new ExpressionNode(derive(rootLeftHolder), rootRightHolder, "*");
        root.left.right = new ExpressionNode(rootLeftHolder1, derive(rootRightHolder1), "*");
        root.right = new ExpressionNode(root.right, "2", "^");
        return root;
    }
    private boolean containsOperator(char str) {
        for(char operator: operators) {
            if(str == operator) {
                return true;
            }
        }
        return false;
    }

    private ExpressionNode copyTree(ExpressionNode root) {
        if (root != null) {
            ExpressionNode newNode = new ExpressionNode(root.value);
            newNode.left = copyTree(root.left);
            newNode.right = copyTree(root.right);
            return newNode;
        }
        return root;
    } 

    public String toString(ExpressionNode root) {
        String result = "";
        if (root == null) {
            return "";
        }
        // System.out.println("prev" + root.value);
        root = simplify(root);
        if (isNumeric(root.value)) {
            result += root.value;
        }  else {
            if (root.left != null && isNumeric(root.left.value)) {
                result += "(" + root.left.value;
            } else if (root.left != null) {
                result += "(" + toString(root.left);
            }
            result += root.value.toString();
            //result += toString(root.right) + ")";
            if (root.right != null && isNumeric(root.right.value)) {
                result += root.right.value + ")";
            } else if (root.right != null) {
                result += toString(root.right) +")";
            } 
        }
        
        return result;
    } 
    public ExpressionNode simplify(ExpressionNode root) {
        if (root == null) {
            return null;
        }
        root.left = simplify(root.left);
        root.right = simplify(root.right);
        String originalValue = root.value;
        if (root.right == null || root.left == null) {
            return root;
        }
        if (root.value.equals("+")) {
            if (root.left.value.equals("0")) {
                root = root.right;
            } else if (root.right.value.equals("0")) {
                root = root.left;
            } else if (isNumeric(root.left.value) && isNumeric(root.right.value)) {
                root.value = "" + (Integer.parseInt(root.right.value) + Integer.parseInt(root.left.value));
            }

        }
        else if (root.value.equals("*")) {
            if (root.left.value.equals("0") || root.right.value.equals("0")) {
                root.value = "0";
            }
            if (isNumeric(root.left.value) && isNumeric(root.right.value)) {
                root.value = "" + (Integer.parseInt(root.left.value) * Integer.parseInt(root.right.value));
            }
        }
        else if (root.value.equals("^")) {
            // if (root.right.value == null || root.right.value == "0") {
            //     root.value = "" + 1;
            // } else 
            if (isNumeric(root.left.value) && isNumeric(root.right.value)) {
                root.value = "" + (Math.pow(Integer.parseInt(root.left.value), Integer.parseInt(root.right.value)));
            } else if(root.right.value.equals("1")) {
                root = root.left;
            }

        }
        else if (root.value.equals("/")) {
            if (root.right.value.equals("1")) {
                root = root.left;
            }
            else if (root.left.value.equals("0")) {
                root.value =  "0";
            }
            else if (root.left.value.equals(root.right.value)) {
                root.value = "1";
            }
        }
        else if (root.value.equals("-")) {
            if (isNumeric(root.left.value) && isNumeric(root.right.value)) {
                root.value = "" + (Integer.parseInt(root.left.value) - Integer.parseInt(root.right.value));
            }
        }      
        return root;
    }

    public String toLatex(ExpressionNode root) {
        StringBuilder result = new StringBuilder();
        if (isNumeric(root.value)|| root.value.matches("[a-zA-Z]+")) {
            result.append(root.value);
        }
        else if (root.value.equals("+")||root.value.equals("-")||root.value.equals("*")) {
            result.append("");
            result.append(toLatex(root.left));
            result.append(root.value);
            result.append(toLatex(root.right));
            result.append("");
        }
        else if (root.value.equals("/")) {
            result.append("\\frac{");
            result.append(toLatex(root.left));
            result.append("}{");
            result.append(toLatex(root.right));
            result.append("}");
        }
        else if (root.value.equals("^")) {
            result.append("{").append(toLatex(root.left));
            result.append("^{");
            result.append(toLatex(root.right));
            result.append("}}");
        }

        return result.toString();
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }      
    

}