import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads file in args and prints the output for each line in the file
 */
public class Main {
    // Initialize logger for this class
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    // Hashmap to store variable names and their values
    private static HashMap<String, String> variables = new HashMap<String, String>();
    public static void main(String[] args) {
        Lexer l = new Lexer();
        TreeParser p = new TreeParser();
        try {
        // if args is not empty, try to read file in args
        if (args.length != 0) {
            // Read file in filepath line by line, parse and evaluate it and then print the output
            String filepath = args[0];
            log.debug("Reading file " + filepath + " and try to parse input.");
            log.info("We are reading a new file.");
            Scanner scanner = new Scanner(new File(filepath), "UTF-8");
            while (scanner.hasNextLine()) {
                try {
                    // Lex and parse line, print result
                    String nxt = scanner.nextLine();

                    // continue on empty lines
                    if (nxt.isEmpty()) {
                        continue;
                    }

                    // handle next line
                    handleNextLine(nxt, l, p);

                } catch (Exception e) {
                    log.error(e.getMessage());
                    System.exit(1);
                }
            }
            scanner.close();
        }
        } catch (Exception e) {
            log.error(e.getMessage());
            System.exit(1);
            // continue with the rest of the input file
        }
    }

    /* Handles "simplify" and "declare-const" statements */
    private static void handleNextLine(String nxt, Lexer l, TreeParser p) throws Exception {
        // case for declare-const
        if (nxt.startsWith("(declare-const")) {
            Pair<String, String> nameValuePair = l.lexDeclareConst(nxt, variables);
            variables.put(nameValuePair.getFirst(), nameValuePair.getSecond());
            // System.out.println(nameValuePair.getSecond());
            return;
        }

        // case for simplify
        else if (nxt.startsWith("(simplify")) {
            // Lex and parse line, print result
            TreeNode<String> intermResult = l.lexSimplify(nxt, variables);
            String result = p.evaluateTree(intermResult);
            System.out.println(result);
            return;
        }

        //  no other cases desired
        else {
            throw new Exception("Input has to begin with simplify or declare-const statement.");
        }
    }
}
