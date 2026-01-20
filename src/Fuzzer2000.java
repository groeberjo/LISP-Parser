import java.io.FileWriter;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* 
 * Takes as argument a seed and mode that decides whether we produce invalid input or correct input.
 * In both cases we write in the file "fuzzed.txt" 2000 lines of input.
 * If mode is 0, we produce invalid input. Otherwise, we produce correct input.
 * The seed is used to generate the input. We allow the seed to be 64-bit.
 */
public class Fuzzer2000 {
    // create logger
    private static final Logger log = LoggerFactory.getLogger(Fuzzer2000.class);
    // LinkedList to store the variables
    private static LinkedList<String> variables = new LinkedList<>();
    // LinkedList to store variables that can be used in variable espressions for declare const statements
    private static LinkedList<String> variableExpr = new LinkedList<>();
    // LinkedList to store all variables
    private static LinkedList<String> allVariables = new LinkedList<>();
    // counter to keep track of the variables that have been used
    private static int alreadyUsed = 0;
    public static void main(String[] args) {
        // Check if we have the correct number of arguments
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: java Fuzzer2000 <seed> <mode>");
            System.exit(1);
        }

        // get arguments and initialize random with seed
        long seed = Long.parseLong(args[0]);
        int mode = Integer.parseInt(args[1]);

        // if we have 3 arguments last one is number of lines we produce, default is 2000
        int lines = 2000;
        if (args.length == 3) {
            lines = Integer.parseInt(args[2]);
        }

        Random random = new Random(seed);
        log.info("Seed: " + seed);

        // Write to file
        try (FileWriter writer = new FileWriter("fuzz.smt2", false)) {
            if (mode == 0) {
                invalidInput(writer, random, lines);
            } else {
                validInput(writer, random, lines);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Generates random BigInteger in range of -(2**127-2)/2 to (2**127-2)/2 */
    private static BigInteger randomBigInteger(Random random) {
        // Define the range
        BigInteger lowerBound = new BigInteger("-85070591730234615865843651857942052863");
        BigInteger upperBound = new BigInteger("85070591730234615865843651857942052863");

        // Generate a random BigInteger in the specified range
        BigInteger range = upperBound.subtract(lowerBound).add(BigInteger.ONE); // inclusive range
        int bitLength = range.bitLength();
        BigInteger randomBigInt;

        do {
            randomBigInt = new BigInteger(bitLength, random);
        } while (randomBigInt.compareTo(range) >= 0);

        // Shift the number to fit within the desired range
        randomBigInt = randomBigInt.add(lowerBound);

        return randomBigInt;
    }

    /* Generates a random long */
    private static long randomLong(Random random) {
        return random.nextLong();
    }

    /* Generates a random BigInteger that is out of range */
    private static BigInteger randomInvalidBig(Random random) {
        // lower and upper bounds are out of range
        BigInteger lowerBound = new BigInteger("-170141183460469231731687303715884105728");
        BigInteger upperBound = new BigInteger("170141183460469231731687303715884105728");

        BigInteger valid = randomBigInteger(random);

        // add lower or upper bound to BigInteger so that it is out of range
        if (valid.compareTo(BigInteger.ZERO) > 0) {
            return valid.add(upperBound);
        } else {
            return valid.add(lowerBound);
        }

    }
    
    /* Generates 2000 correct expressions and writes them in "writer" */
    private static void validInput(FileWriter writer, Random random, int lines) throws Exception {
        for (int i = 0; i < lines; i++) {
            String expression = "";
            // Either produce declare-const or simplify
            switch (random.nextInt(2)) {
                // simplify case
                case 0:
                    if (variables.size() == 0) {
                        expression = createCorrectExpression(random);
                    } else {
                        expression = createVariableExpression(random);
                    }
                    break;

                // declare-const case
                case 1:
                    expression = createDeclareConst(random);
                    break;
                default:
                    break;
            }
            writer.write(expression + "\n");
        }
    }

    /* Generates 2000 invalid  */
    private static void invalidInput(FileWriter writer, Random random, int lines) throws Exception {
        // 4 categories of invalid input
        String expression = "";
        for (int i = 0; i < lines; i++) {
            switch(random.nextInt(4)) {
                case 0:
                    // number error
                    expression = numberError(random);
                    break;
                case 1:
                    // syntax error
                    expression = syntaxError(random);
                    break;
                case 2:
                    // unsupported operator
                    expression = operatorError(random);
                    break;
                case 3:
                    // declare-const error
                    expression = declareConstError(random);
                    break;
                default:
                    break;
            }
            writer.write(expression + "\n");
        }
    }

    /* Produces an invalid declare-const statement */
    private static String declareConstError(Random random) {
        String correctExpression = createDeclareConst(random);
        // 3 types of errors
        switch(random.nextInt(5)) {
            case 0:
                // missing parenthesis
                return correctExpression.substring(0, correctExpression.length() - 1);
            case 1:
                // extra parenthesis
                return correctExpression + ")";
            case 2:
                String var = createNonVar(random);
                String num2 = String.valueOf(randomLong(random));
                String[] operators = {"+", "-", "*"};
                String op = operators[random.nextInt(3)];
                return "(simplify (" + op + " " + var + " " + num2 + "))";
            case 3:
                // nested declare-const statement
               return "(declare-const " + createVariable(random) + " " + correctExpression + ")";
            case 4:
                // faulty if statement in declare-const statement
                long value1 = randomLong(random);
                long value2 = randomLong(random);
                long value3 = randomLong(random);
                String novar = createNonVar(random);
                String[] conOps = {"<", "<=", ">", ">=", "="};
                String conOp = conOps[random.nextInt(5)];
                return "(declare-const " + createVariable(random) + " (if (" + conOp + " " + novar + " " + value3 +
                       ") " + value1 + " " + value2 + "))";
            default:
                return correctExpression;
        }
    }

    /* Produces a simplify statement that containts a syntax error */
    private static String syntaxError(Random random) {
        String correctExpression = createCorrectExpression(random);
        // 4 types of syntax errors
        switch(random.nextInt(4)) {
            case 0:
                // missing parenthesis
                return correctExpression.substring(0, correctExpression.length() - 1);
            case 1:
                // extra parenthesis
                return correctExpression + ")";
            case 2:
                // missing operator
                return correctExpression.substring(0, 11) + correctExpression.substring(12);
            case 3:
                // extra operator
                return correctExpression.substring(0, 11) + "+" + correctExpression.substring(11);
            default:
                return correctExpression;
        }
    }

    /* Produces a simplify statement that containts a number error */
    private static String numberError(Random random) {
        String[] operators = {"+", "-", "*"};
        String op = operators[random.nextInt(3)];

        // either num1 or num2 is invalid, or both
        String num1 = "";
        String num2 = "";
        switch (random.nextInt(3)) {
            case 0:
                num1 = randomInvalidBig(random).toString();
                num2 = randomBigInteger(random).toString();
                break;
            case 1:
                num1 = randomBigInteger(random).toString();
                num2 = randomInvalidBig(random).toString();
                break;
            case 2:
                num1 = randomInvalidBig(random).toString();
                num2 = randomInvalidBig(random).toString();
                break;
            default:
                break;
        }
        return "(simplify (" + op + " " + num1 + " " + num2 + "))";
    }

    /* Produces a simplify statement that containts an operator error */
    private static String operatorError(Random random) {
        String[] unsupported = {"%", "/", "mod", "div", "$", "!", "&", "|", "^", ">>", "<<", ">>>", "<<<"};
        String op = unsupported[random.nextInt(unsupported.length)];
        String correct = createCorrectExpression(random);
        return correct.substring(0, 11) + op + correct.substring(12);
    }

    /* Produces a declare-const statement */
    private static String createDeclareConst(Random random) {
        String var = "";
        do {
            var = createVariable(random);
        } while (variables.contains(var));

        // Store var name into all variables
        allVariables.add(var);

        // flip coin to either assign random long, or assign a variable expression
        int coin = random.nextInt(3);
        if (coin == 0) {
            // get random valid long
            int value = random.nextInt();

            // variables can be used in variable expressions
            variableExpr.add(var);

            // store variable name
            variables.add(var.toString());
            return "(declare-const " + var + " " + value + ")";
        } else if (coin == 1) {
            // get random variable expression
            if (variableExpr.size() - alreadyUsed < 2) {
                // store variable name
                variables.add(var.toString());

                // variables can be used in variable expressions
                variableExpr.add(var);
                return "(declare-const " + var + " " + random.nextInt() + ")";
            } else {
                String varEx = "(+ " + variableExpr.get(alreadyUsed) + " " + variableExpr.get(alreadyUsed + 1) + ")";
                alreadyUsed += 2;
                return "(declare-const " + var + " " + varEx + ")";
            }
        } else {
            // create if statement
            String ifStatement = createIfStatement(random);
            return "(declare-const " + var + " " + ifStatement + ")";
        }
            
        
    }

    /* Produces a valid if statement */
    private static String createIfStatement(Random random) {
        // get random operator
        String[] operators = {"<", "<=", ">", ">=", "="};
        String op = operators[random.nextInt(operators.length)];

        // get random longs
        String value1 = String.valueOf(randomLong(random));
        String value2 = String.valueOf(randomLong(random));

        // if there are spare variables and coin flip is 0, use variables for condition
        if (variableExpr.size() - alreadyUsed >= 1 && random.nextInt(2) == 0) {
            value1 = variableExpr.get(alreadyUsed);
            alreadyUsed++;
        }
        if (variableExpr.size() - alreadyUsed >= 1 && random.nextInt(2) == 0) {
            value2 = variableExpr.get(alreadyUsed);
            alreadyUsed++;
        }

        // build condition
        String condition = "(" + op + " " + value1 + " " + value2 + ")";

        return "(if " + condition + " " + createOperatorExpression(random) + " " + 
               createOperatorExpression(random) + ")";
    }

    /* Produces a correct expression of sorts <<num|var>|(op <num1|var1> <num2|var2>)> */
    private static String createOperatorExpression(Random random) {
        // return just a variable or number if random coin flip is 0
        if (random.nextInt(2) == 0) {
            if (variableExpr.size() - alreadyUsed >= 1 && random.nextInt(2) == 0) {
                alreadyUsed++;
                return variableExpr.get(alreadyUsed - 1);
            } else {
                return String.valueOf(randomLong(random));
            }
        }

        // get random operator
        String[] operators = {"+", "-", "*"};
        String op = operators[random.nextInt(3)];

        // get random longs
        String num1 = String.valueOf(random.nextInt());
        String num2 = String.valueOf(random.nextInt());

        // if there are spare variables and coin flip is 0, use variables for condition
        if (variableExpr.size() - alreadyUsed >= 1 && random.nextInt(2) == 0) {
            num1 = variableExpr.get(alreadyUsed);
            alreadyUsed++;
        }
        if (variableExpr.size() - alreadyUsed >= 1 && random.nextInt(2) == 0) {
            num2 = variableExpr.get(alreadyUsed);
            alreadyUsed++;
        }

        return "(" + op + " " + num1 + " " + num2 + ")";
    }
    
    /* Produces a correct expression that might contain a variable */
    private static String createVariableExpression(Random random) {
        // flip coin to decide if we use a variable or not
        if (random.nextInt(2) == 0) {
            return createCorrectExpression(random);
        }

        // num1 and num2 are either both variables, or only one of them is. One of them can also be an if statement
        String num1 = "";
        String num2 = "";
        switch (random.nextInt(4)) {
            // both variables
            case 0:
                num1 = variables.get(random.nextInt(variables.size()));
                num2 = variables.get(random.nextInt(variables.size()));
                break;
            // only num1 is variable
            case 1:
                num1 = variables.get(random.nextInt(variables.size()));
                num2 = String.valueOf(randomLong(random));
                break;
            // only num2 is variable
            case 2:
                num1 = String.valueOf(randomLong(random));
                num2 = variables.get(random.nextInt(variables.size()));
                break;
            // num1 or num2 is an if statement
            case 3:
                int coinFlip = random.nextInt(2);
                num1 = coinFlip == 0 ? createIfStatement(random) : String.valueOf(randomLong(random));
                num2 = coinFlip == 1 ? createIfStatement(random) : String.valueOf(randomLong(random));
                break;
            default:
                break;
        }

        // get random operator
        String[] operators = {"+", "-", "*"};
        String op = operators[random.nextInt(3)];

        return "(simplify (" + op + " " + num1 + " " + num2 + "))";
    }

    /* Produces a correct expression */
    private static String createCorrectExpression(Random random) {
        String[] operators = {"+", "-", "*"};
        String num1;
        String num2;
        String op = operators[random.nextInt(3)];
        switch (op) {
            case "*":
                num1 = String.valueOf(randomLong(random));
                num2 = String.valueOf(randomLong(random));
                break;
            case "+":
                num1 = randomBigInteger(random).toString();
                num2 = randomBigInteger(random).toString();
                break;
            case "-":
                num1 = randomBigInteger(random).toString();
                num2 = randomBigInteger(random).toString();
                break;
            default:
                num1 = "0";
                num2 = "0";
                break;
        }
        return "(simplify (" + op + " " + num1 + " " + num2 + "))";
    }

    /* Creates a String that is not a variable */
    private static String createNonVar(Random random) {
        // get undeclared variable
        String var = createVariable(random);
        while (allVariables.contains(var)) {
            var = createVariable(random);
        }
        allVariables.add(var);
        return var;
    }


    /* Creates variable String */
    private static String createVariable(Random random) {
        // characters that can be used in variable names
        String LETTERS = "ACDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder var;
        var = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            var.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        }
        return var.toString();
    }
}
