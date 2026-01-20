import java.util.HashMap;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lexer is able to build an AST for given input in the form "(simplify (op num1 num2))" where
 * op is one of +,- or *. num1 and num2 are either 32-bit integers or an expression like (op num1 num2)
 * themselves.
 * Build the AST by calling the lexing method.
 */
public class Lexer {
    // Counts opened and closed brackets
    private int opened;
    private int closed;
    private TreeNode<String> rootNode;
    private TreeNode<String> currentOperator;
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public Lexer() {
        log.debug("Instantiated Lexer.");
    }

    /* Responsible for declare-const statements, returns the variable name and value */
    public Pair<String, String> lexDeclareConst(String toLex, HashMap<String, String> variables) throws Exception {
        // Tokenize input string by using a Scanner
        Scanner scanner = new Scanner(toLex.replaceAll(".$", ""));

        // String has to start with declare-const statement and end with a bracket
        if (!"(declare-const".equals(scanner.next()) || !(toLex.endsWith(")"))) {
            scanner.close();
            throw new Exception("Input has to begin with declare-const statement.");
        }

        // Get variable name
        String varName = scanner.next();
        
        // Remove declare-const statement and last bracket from input string
        toLex = toLex.replaceAll("^\\(declare-const " + varName + " ", "").replaceAll(".$", "");

        // Get variable value
        // Case variable value is a number
        if (toLex.matches("^-?\\d+$")) {
            scanner.close();
            return new Pair<String, String>(varName, toLex);
        }

        // Check if variable value is an if statement
        if (toLex.startsWith("(if")) {
            scanner.close();
            // if statement should still have a bracket in the end
            if (!toLex.endsWith(")")) {
                throw new Exception("Missing closing bracket in if statement.");
            }
            return new Pair <String, String>(varName, evaluateIf(toLex, variables));
        }

        // Otherwise vairable value is an expression
        TreeNode<String> resultAst = lexing(toLex, variables);

        // Evaluate AST
        TreeParser parser = new TreeParser();
        String varValue = parser.evaluateTree(resultAst);

        // Strip variable value of brackets and minus sign
        varValue = stripNegative(varValue);

        Pair<String, String> result = new Pair<String, String>(varName, varValue);
        scanner.close();
        return result;
    }

    /* Responsible for simplify statements, returns the AST for the expression in the input */
    public TreeNode<String> lexSimplify(String toLex, HashMap<String, String> variables) throws Exception {
        // Tokenize input string by using a Scanner
        Scanner scanner = new Scanner(toLex.replaceAll(".$", ""));
        if (!("(simplify".equals(scanner.next())) || !(toLex.endsWith(")"))){
            scanner.close();
            throw new Exception("Input has to begin with simplify statement.");
        }

        // Remove simplify statement and last bracket from input string
        toLex = toLex.replaceAll("^\\(simplify ", "").replaceAll(".$", "");
        scanner.close();
        return lexing(toLex, variables);
    }

    // Builds AST for simplify input and calls parser to return result for the expression in the input
    public TreeNode<String> lexing(String toLex, HashMap<String, String> variables) throws Exception {
        log.debug("Start lexing input string.");

        // Set open and close counter to zero
        opened = 0;
        closed = 0;

        // Tokenize input string by using a Scanner
        Scanner scanner = new Scanner(toLex);
        
        // Handle first operator and put it as root
        String root = scanner.next();
        if (root.matches("^\\([\\+\\-\\*]$")) {
            opened++;
            rootNode = new TreeNode<String>(root.replaceAll("\\(", ""));
            currentOperator = rootNode;
        } else {
            scanner.close();
            throw new Exception("First operator wrong.");
        }
        // Handle rest of input string
        while (scanner.hasNext()) {
            addSafe(scanner.next(), variables, scanner);
        }

        if (!(opened == closed)) {
            scanner.close();
            throw new Exception("Wrong number of brackets");
        }

        scanner.close();

        log.debug("Lexing input string complete.");
        return (currentOperator);
    }

    // Safely add content to AST
    private void addSafe(String content, HashMap<String, String> variables, Scanner scanner) throws Exception{
        // Case input is a number or a variable that may or may not have closing brackets in the end
        if (content.matches("^-?\\d+\\)*$") || variables.containsKey(content.replaceAll("\\)+", ""))) {

            // Current operator has no operands yet
            if (currentOperator.children.isEmpty()) {
                if (content.matches("^-?\\d+$")) {
                    currentOperator.addChild(content);
                } else if (variables.containsKey(content)) {
                    currentOperator.addChild(variables.get(content));
                } else {
                    throw new Exception("Wrong number of inputs.");
                }

            // Current operator has exactly one operand so far
            } else if (currentOperator.children.size() == 1 && content.matches("^-?\\d+\\)+$")) {
                currentOperator.addChild(content.replaceAll("\\)+", ""));
                closeBrackets(content, variables);
            } else if (currentOperator.children.size() == 1 && variables.containsKey(content.replaceAll("\\)+", ""))) {
                currentOperator.addChild(variables.get(content.replaceAll("\\)+", "")));
                closeBrackets(content, variables);
            } else {
                throw new Exception ("Wrong number of inputs.");
            }
        } 

        // Case input is an operator
        else if (content.matches("^\\([\\+\\-\\*]$") && currentOperator.children.size() < 2) {
            currentOperator.addChild(content.replaceAll("\\(", ""));
            currentOperator = currentOperator.children.getLast();
            opened++;
        }

        // Case input is an if statement
        else if (content.startsWith("(if")) {
            Pair<String, Integer> ifStatementDiff = extractIfStatement(scanner);

            // save current state of variables
            TreeNode<String> temp = currentOperator;
            int tempOpened = opened;
            int tempClosed = closed;

            // prepare if statement by stripping last "diff" brackets
            String ifStatement = ifStatementDiff.getFirst();
            ifStatement = ifStatement.substring(0, ifStatement.length() - ifStatementDiff.getSecond());
            String evaluatedIf = evaluateIf(ifStatement, variables);

            // restore previous state of variables
            opened = tempOpened;
            closed = tempClosed + ifStatementDiff.getSecond();
            currentOperator = temp;
            currentOperator.addChild(evaluatedIf);

            // close brackets
            StringBuilder brackets = new StringBuilder();
            for (int i = 0; i < ifStatementDiff.getSecond(); i++) {
                brackets.append(")");
            }
            closeBrackets(brackets.toString(), variables);
        }

        // No other cases are desired input
        else {
            throw new Exception("Undesired input");
        }
    }

    // Correctly close all brackets for content and return new current operator
    private void closeBrackets(String content, HashMap<String, String> variables) throws Exception {
        String brackets = "";
        if (content.matches("^-?\\d+\\)*$")) {
            brackets = content.replaceAll("-?\\d+", "");
        } else if (variables.containsKey(content.replaceAll("\\)+", ""))) {
            brackets = content.replaceAll("^[a-zA-Z]+", "");
        }
        
        if (brackets.length() + closed > opened) {
            throw new Exception("There are too many closing brackets.");
        }
        for (int i = 0; i < brackets.length(); i++) {
            if (!(currentOperator.parent == null && brackets.length() == i + 1 && opened == closed + 1)) {
                currentOperator = currentOperator.parent;
                closed++;
            } else {
                closed++;
            }
        }
    }

    // Strips negative value in string of brackets and whitespace
    private String stripNegative(String toStrip) {
        if (toStrip.startsWith("()")) {
            return toStrip;
        }
        return toStrip.replaceAll("^\\(- ", "-").replaceAll("\\)$", "");
    }

    // Evaluates if statement and returns value
    private String evaluateIf(String toLex, HashMap<String, String> variables) throws Exception {
        // remove if statement and last bracket from input string
        toLex = toLex.replaceAll("^\\(if ", "").replaceAll(".$", "");
        
        // evaluate ([<|<=|>|>=|=] <var|num> <var|num>) term
        String condition = toLex.substring(0, toLex.indexOf(")"));
        condition = condition.replaceAll("^\\(", "");

        // separate condition into operator and operands
        String[] conditionParts = condition.split(" ");

        // evaluate condition
        boolean result = evaluateCondition(conditionParts, variables);

        // get first expression
        String expressions = toLex.substring(toLex.indexOf(")") + 2);
        String first = extractExpression(expressions);

        // get second expression
        expressions = expressions.substring(first.length() + 1);
        String second = extractExpression(expressions);

        // make sure we have correct expressions
        if (!correctExpression(first, variables) || !correctExpression(second, variables)) {
            throw new Exception("Invalid expression.");
        }

        // check if first and second expressions are just numbers or variables
        if (first.matches("^-?\\d+$") && result) {
            return first;
        } else if (variables.containsKey(first) && result) {
            return variables.get(first);
        } else if (second.matches("^-?\\d+$") && !result) {
            return second;
        } else if (variables.containsKey(second) && !result) {
            return variables.get(second);
        }

        // Make sure after extracting the expressions, the string is empty
        String leftOver = expressions.substring(second.length());
        if (!leftOver.isEmpty()) {
            throw new Exception("Invalid if statement.");
        }

        // if condition is true, evaluate first expression otherwise evaluate second expression
        TreeParser parser = new TreeParser();
        TreeNode<String> resultAst = result ? lexing(first, variables) : lexing(second, variables);
        String resultValue = parser.evaluateTree(resultAst);

        // strip result of brackets and minus sign
        resultValue = stripNegative(resultValue);
        return resultValue;
    }

    // Checks if expression is valid
    private boolean correctExpression(String expression, HashMap<String, String> variables) {
        if (expression.matches("^-?\\d+$") || variables.containsKey(expression)) {
            return true;
        }
        // count opening and closing brackets in expression and make sure they match
        int opened = 0;
        int closed = 0;
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '(') {
                opened++;
            }
            if (expression.charAt(i) == ')') {
                closed++;
            }
        }
        return opened == closed;
    }

    // Extracts if statement from input scanner
    private Pair<String, Integer> extractIfStatement(Scanner scanner) throws Exception {
        StringBuilder ifStatement = new StringBuilder();
        ifStatement.append("(if ");
        // Opened has to be 1 due do the if statement at the beginning
        int opened = 1;
        int closed = 0;
        while (scanner.hasNext()) {
            String next = scanner.next();
            if (next.startsWith("(")) {
                opened++;
            }
            if (next.endsWith(")")) {
                // replace all letters and numbers at the beginning of the string
                String temp = next.replaceAll("^[a-zA-Z]+", "").replaceAll("^-?\\d+", "");
                closed += temp.length();
            }
            ifStatement.append(next + " ");
            if (opened <= closed) {
                break;
            }
        }
        // strip last whitespace
        ifStatement.deleteCharAt(ifStatement.length() - 1);

        // We have closed - opened closing brackets
        int diff = closed - opened;
        if (diff < 0) {
            throw new Exception("Invalid if statement.");
        }
        return new Pair<String, Integer>(ifStatement.toString(), diff);
    }

    // Extracts expression from if statement
    private String extractExpression(String toLex) {
        if (!toLex.startsWith("(")) {
            return toLex.split(" ")[0];
        }
        StringBuilder expression = new StringBuilder();
        int opened = 0;
        int closed = 0;
        Scanner scanner = new Scanner(toLex);
        while (scanner.hasNext()) {
            String next = scanner.next();
            if (next.startsWith("(")) {
                opened++;
            }
            if (next.endsWith(")")) {
                closed++;
            }
            expression.append(next + " ");
            if (opened == closed) {
                break;
            }
        }
        // strip last whitespace
        expression.deleteCharAt(expression.length() - 1);
        scanner.close();
        return expression.toString();
    }

    // Evaluates condition and returns boolean value
    private boolean evaluateCondition(String[] conditionParts, HashMap<String, String> variables) throws Exception {
        // check if condition is valid
        if (conditionParts.length != 3) {
            throw new Exception("Invalid condition.");
        }

        // get operator and operands
        String operator = conditionParts[0];
        String operand1 = conditionParts[1];
        String operand2 = conditionParts[2];

        // check if operands are valid
        if (!operand1.matches("^-?\\d+$") && !variables.containsKey(operand1)) {
            throw new Exception("Variable " + operand1 + " not declared.");
        }
        if (!operand2.matches("^-?\\d+$") && !variables.containsKey(operand2)) {
            throw new Exception("Variable " + operand2 + " not declared.");
        }

        // get values of operands
        String value1 = operand1.matches("^-?\\d+$") ? operand1 : variables.get(operand1);
        BigNum num1 = BigMain.stringToBigNum(value1);
        String value2 = operand2.matches("^-?\\d+$") ? operand2 : variables.get(operand2);
        BigNum num2 = BigMain.stringToBigNum(value2);

        // subtract num1 - num2
        BigNum result = Operations.bignumSubtract(num1, num2);

        // msb of result is 1 if result is negative
        int msb = result.values[result.values.length - 1] >>> 31;

        // return boolean value of condition
        Boolean cond = false;

        // evaluate condition
        switch (operator) {
            case "<":
                cond = msb == 1 ? true : false;
                break;
            case "<=":
                cond = msb == 1 ? true : result.checkZeros() ? true : false;
                break;
            case ">":
                cond = (msb == 0 && !result.checkZeros()) ? true : false;
                break;
            case ">=":
                cond = msb == 0 ? true : result.checkZeros() ? true : false;
                break;
            case "=":
                cond = result.checkZeros() ? true : false;
                break;
            default:
                throw new Exception("Invalid operator.");
        }
        return cond;
    }
}
