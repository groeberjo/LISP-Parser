import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BigNum {
    // Initialize logger for this class
    private static final Logger log = LoggerFactory.getLogger(BigNum.class);
    public int[] values = new int[4];

    /* initializes a BigNum with all values as 0 */
    public BigNum() {
        final int[] zeros = {0, 0, 0, 0};
        values = zeros;
    }

    /* sets all values to 0 */
    public void setZero() {
        final int[] zeros = {0, 0, 0, 0};
        values = zeros;
    }
    
    /* adds toAdd as least significant word */
    public void assignToNum(int toAdd) {
        values[0] = toAdd;
    }

    /* checks if all values are zero */
    public boolean checkZeros() {
        for (int value: values) {
            if (value != 0) {
                return (false);
            }
        }
        return (true);
    }

    /* inverts bignum by the rules of twos complement */
    public void invertNum() throws Exception{
        for (int i = 0; i < values.length; i++) {
            values[i] = ~values[i];
        }
        BigNum one = new BigNum();
        one.assignToNum(1);
        BigNum overWrite = Operations.bignumAdd(this, one);
        for (int i = 0; i < values.length; i++) {
            values[i] = overWrite.values[i];
        }
    }

    /* returns BigNum as decimal string */
    @Override
    public String toString() {
        // string to return
        String toReturn = "";

        // creating a copy of bignum x
        BigNum copy = new BigNum();
        for (int i = 0; i < copy.values.length; i++) {
            copy.values[i] = values[i];
        }

        // if the number is negative, print "-", invert number and print number
        if (((copy.values[copy.values.length - 1] >> 31) & 1) == 1) {
            toReturn = "-" + toReturn;
            try {
                copy.invertNum();
            } catch (Exception e) {
                log.error(e.getMessage());
                System.exit(1);
            }
        }

        // use Stack structure to store digits
        Stack<Character> digits = new Stack<Character>();

        // while BigNum is not zero, divide by 10 and store remainder
        while(!copy.checkZeros()) {
            Pair<BigNum, Integer> divResult = Operations.bignumDivideByInt32(copy, 10);
            digits.push((char) (divResult.getSecond() + '0'));
            copy = divResult.getFirst();
        }

        // print digits in reverse order, if no digits were found print '0'
        if (digits.isEmpty()) {
            toReturn = "0";
        } else {
            while (!digits.isEmpty()) {
                toReturn = toReturn + digits.pop().toString();
            }
        }
        return toReturn;
    }
}