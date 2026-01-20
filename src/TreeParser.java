
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TreeParser is able to evaluate an AST that was built by Lexer.
 * Call parseTree() to get the result for the input AST.
 */
public class TreeParser {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    
    public TreeParser(){
        log.debug("Instantiated TreeParser.");
    }

    // Calls recursive evaluator
    public String evaluateTree(TreeNode<String> tree) throws Exception {
        log.debug("Start recursive evaluation of TreeNode.");
        final TreeNode<String> resultTree = recParseTree(tree);
        log.debug("Recursive evaluation of TreeNode complete.");
        if (resultTree.data.matches("^\\d+$")) {
            return resultTree.data;
        } else if (resultTree.data.matches("^-\\d+$")) {
            return ("(" + "- " + resultTree.data.replaceAll("-", "") + ")");
        } else {
            throw new Exception("Something went wrong with parsing tree");
        }
    }

    // Recursively works in a DFS manner to evaluate the input tree
    private TreeNode<String> recParseTree(TreeNode<String> tree) throws Exception {
        // Base case no1, tree has no children
        if (tree.children.isEmpty()) {
            return tree;
        }

        // Base case no2, display negative integers as one node
        // NOTE: this case doesn't exist currently
        /* else if ("-".equals(tree.data) && tree.children.size() == 1 &&
                 tree.children.getFirst().data.matches("^\\d+$")) {
                    if (tree.parent == null) {
                        return new TreeNode<String>("-" + tree.children.getFirst().data);
                    } else {
                        TreeNode<String> negetiveInt = new TreeNode<String>("-" + tree.children.getFirst().data);
                        tree.parent.children.set(tree.parent.children.indexOf(tree), negetiveInt);
                        return recParseTree(tree.parent);
                    }
        } */

        // Go right if right operand is another operator
        else if (!tree.children.getLast().data.matches("^-?\\d+$")) {
            return recParseTree(tree.children.getLast());
        }

        // Go left if right operand is just a number
        else if (!tree.children.getFirst().data.matches("^-?\\d+$")) {
            return recParseTree(tree.children.getFirst());
        }

        // Case we have both operands as numbers 
        else if (tree.data.matches("^[+\\-\\*]$")) {
            String result = evaluate(tree.data, tree.children.getFirst().data, tree.children.getLast().data);
            TreeNode<String> resultTree = new TreeNode<String>(result);
            if (tree.parent == null) {
                tree = resultTree;
                return tree;
            } else {
                tree.parent.children.set(tree.parent.children.indexOf(tree), resultTree);
                return recParseTree(tree.parent);
            }
            
        }

        // Case not desired
        else {
            throw new Exception("Tree has wrong form.");
        }

    }

    // apply string operator on integer operands
    private String evaluate(String op, String left, String right) throws Exception {
        BigNum leftNum = BigMain.stringToBigNum(left);
        BigNum rightNum = BigMain.stringToBigNum(right);
        BigNum res;
        switch(op) {
            case "+":
                res = Operations.bignumAdd(leftNum, rightNum);
                return (res.toString());
            case "*":
                res = Operations.bignumMultiply(leftNum, rightNum);
                return (res.toString());
            case "-":
                res = Operations.bignumSubtract(leftNum, rightNum);
                return (res.toString());
            default:
                return "0";
        }
    }
}
