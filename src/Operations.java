import java.io.FileWriter;
import java.util.Locale;
import java.util.Stack;

public class Operations {
    /* left shifts bignum by the amount of 'shift' */
    public static BigNum bigNumShiftLeft(int shift, BigNum number) throws Exception {
        if (shift > 128) {
            throw new Exception("too many left shifts");
        } else if (shift == 0) {
            return (number);
        }

        // initialize BigNum to return with values of number
        BigNum toReturn = new BigNum();
        for (int i = 0; i < toReturn.values.length; i++) {
            toReturn.values[i] = number.values[i];
        }

        // number of words and bits to shift
        int wordShift = shift >> 5;
        int bitShift = shift & 31;

        // check for overflow when shifting words
        for (int i = toReturn.values.length - 1; i >= toReturn.values.length - wordShift; i--) {
            if (toReturn.values[i] != 0) {
                throw new Exception("Overflow for wordshift.");
            }
        }

        // shift words
        for (int i = toReturn.values.length - 1; i > wordShift - 1; i--) {
            toReturn.values[i] = toReturn.values[i - wordShift];
        }
        for (int i = 0; i < wordShift; i++) {
            toReturn.values[i] = 0;
        }

        // shift within words, respecting the carry bits (only if bitshift > 0 otherwise we get strange behavior)
        if (bitShift > 0) {
            int carry = 0;
            for (int i = wordShift; i < toReturn.values.length; i++) {
                int interm = toReturn.values[i] >>> (32 - bitShift);
                toReturn.values[i] = toReturn.values[i] << bitShift;
                toReturn.values[i] = toReturn.values[i] | carry;
                carry = interm;
            }

            // check for overflow when shifting bits within word
            if (carry != 0) {
                throw new Exception("Overflow due to bit shifting");
            }
        }

        return (toReturn);
    }

    /* adding two 32-bit numbers */
    public static int[] add32WithCarry(int a, int b, int carryIn) {
        // initialize carries and result
        int carryIntoMSB = 0;
        int carry = carryIn;
        int result = 0;

        // iterate over each bit position
        for (int i = 0; i < 32; i++) {
            int bitA = (a >> i) & 1;
            int bitB = (b >> i) & 1;
            int sumBit = (bitA ^ bitB) ^ carry;
            
            // compute carry and result
            carry = (bitA & bitB) | (bitA & carry) | (bitB & carry);
            result = result | (sumBit << i);
            
            // store carry into msb
            if (i == 30) {
                carryIntoMSB = carry;
            }
        }

        // return result and carry as int array {result, carry}
        int[] toReturn = {result, carry, carryIntoMSB};
        return (toReturn);
    }

    /* adds two bignums by calling add32WithCarry for each word */
    public static BigNum bignumAdd (BigNum a, BigNum b) throws Exception {
        // initialize carries with 0 and result BigNum
        int carry = 0;
        int carryIntoMSB = 0;
        BigNum result = new BigNum();

        // add words of a and b with carry and store in result BigNum
        for (int i = 0; i < a.values.length; i++) {
            int[] resCar = add32WithCarry(a.values[i], b.values[i], carry);
            result.values[i] = resCar[0];
            carry = resCar[1];
            carryIntoMSB = resCar[2];
        }

        // check that carry into msb and carry out are the same
        if (carry != carryIntoMSB) {
            throw new Exception("overflow while adding big nums");
        }

        // check for carry in the end
        /* if (carry != 0) {
            throw new Error("carry-out is not 0");
        } */

        return (result);
    }

    /* subtracts b from a by inverting b and calling bignumAdd */
    public static BigNum bignumSubtract(BigNum a, BigNum b) throws Exception {
        b.invertNum();
        BigNum toReturn = bignumAdd(a, b);
        b.invertNum();
        return (toReturn);
    }

    public static BigNum bignumMultiply(BigNum a, BigNum b) throws Exception {
        // check for sign of a and b
        boolean aSign = ((a.values[a.values.length - 1] >> 31) & 1) == 1;
        boolean bSign = ((b.values[b.values.length - 1] >> 31) & 1) == 1;
        
        // if a is negative, invert a
        if (aSign) {
            a.invertNum();
        }

        // if b is negative, invert b
        if (bSign) {
            b.invertNum();
        }

        // initialize result BigNum to zero
        BigNum result = new BigNum();

        // create copy of multiplicand a
        BigNum multiplicand = new BigNum();
        for (int i = 0; i < multiplicand.values.length; i++) {
            multiplicand.values[i] = a.values[i];
        }

        // iterate over bits in b
        for (int i = 0; i < b.values.length; i++) {
            for (int j = 0; j < 32; j++) {
                int currentBit = (b.values[i] >> j) & 1;
                if (currentBit == 1) {
                    // shift multiplicand to the left by i*32+j bits
                    BigNum tmp = bigNumShiftLeft(i * 32 + j, multiplicand);

                    // check for overflow
                    if (((tmp.values[tmp.values.length - 1] >> 31) & 1) == 1) {
                        throw new Exception("overflow while multiplying bignums");
                    }

                    // add shifted multiplicand to result
                    result = bignumAdd(result, tmp);
                }
            }
        }

        // if a or b is negative, invert result, not if both are negative
        if ((aSign || bSign) && !(aSign && bSign)) {
            result.invertNum();
        }

        // invert a and b back
        if (aSign) {
            a.invertNum();
        }
        if (bSign) {
            b.invertNum();
        }

        return (result);
    }

    /* prints bignum as hex decimal number */
    public static void bignumPrintHex(BigNum x) {
        // flag to track if we started printing
        boolean printFlag = false;

        // iterate over words of bignum in reverse
        for (int i = x.values.length - 1; i >= 0; i--) {
            // only print if we already started printing or if word is non-zero
            if (printFlag || x.values[i] != 0) {
                if (!printFlag) {
                    System.out.print(Integer.toHexString(x.values[i]).toUpperCase(Locale.ROOT));
                    printFlag = true;
                } else {
                    printFlag = true;
                    System.out.print((String.format("%08x", x.values[i])).toUpperCase(Locale.ROOT));
                }
            }
        }

        // if no non-zero words are found, print "0."
        if (!printFlag) {
            System.out.print("0.");
        }
    }

    /* subtracts two 32-bit integers with borrow */
    public static int[] subtract32WithBorrow(int a, int b, int borrowIn) {
        // initialize result and borrow
        int result = 0;
        int borrow = borrowIn;

        // iterate over each bit position
        for (int i = 0; i < 32; i++) {
            int bitA = (a >> i) & 1;
            int bitB = (b >> i) & 1;

            // calculate result bit
            int res = bitA ^ bitB ^ borrow;
            
            // calculate borrow according to truth table
            borrow = ((~bitA & ~bitB & borrow) | (~bitA & bitB & ~borrow) |
                      (~bitA & bitB & borrow) | (bitA & bitB & borrow)) & 1;

            // update result
            result = result | (res << i);
        }
        int[] toReturn = {result, borrow};
        return toReturn;
    }

    /* compare two 32 bit integers a and b, return true if a >= b*/
    public static boolean compareInt32(int a, int b) {
        int[] resBorrow = subtract32WithBorrow(a, b, 0);
        // If borrow is 0, we know a >= b
        if (resBorrow[1] == 0) {
            return (true);
        } else {
            return (false);
        }
    }

    /* dividing a bignum by a 32 bit integer */
    public static Pair<BigNum, Integer> bignumDivideByInt32(BigNum numerator, int divisor) {
        // initialize remainder and quotient to 0
        int remainder = 0;
        BigNum quotient = new BigNum();
        
        // loop over bits from numerator (from most significant to least significant)
        for (int i = numerator.values.length - 1; i >= 0; i--) {
            for (int j = 0; j < 32; j++) {
                int currentBit = (numerator.values[i] >>> (31 - j)) & 1;

                // shift remainder left by one bit and add next Bit from numerator
                remainder = (remainder << 1) | currentBit;
                if (compareInt32(remainder, divisor)) {
                    int[] subtractRes = subtract32WithBorrow(remainder, divisor, 0);
                    quotient.values[i] = quotient.values[i] | (1 << (31 - j));
                    remainder = subtractRes[0];
                }
            }
        }
        Pair<BigNum, Integer> toReturn = new Pair<BigNum, Integer>(quotient, remainder);
        return toReturn;
    }

    /* prints bignum as decimal number */
    public static void bigNumPrintDecimal(BigNum x) throws Exception {
        // creating a copy of bignum x
        BigNum copy = new BigNum();
        for (int i = 0; i < copy.values.length; i++) {
            copy.values[i] = x.values[i];
        }

        // if the number is negative, print "-", invert number and print number
        if (((copy.values[copy.values.length - 1] >> 31) & 1) == 1) {
            System.out.print('-');
            copy.invertNum();
        }

        // use Stack structure to store digits
        Stack<Character> digits = new Stack<Character>();

        // while BigNum is not zero, divide by 10 and store remainder
        while(!copy.checkZeros()) {
            Pair<BigNum, Integer> divResult = bignumDivideByInt32(copy, 10);
            digits.push((char) (divResult.getSecond() + '0'));
            copy = divResult.getFirst();
        }

        // print digits in reverse order, if no digits were found print '0'
        if (digits.isEmpty()) {
            System.out.println('0');
        } else {
            while (!digits.isEmpty()) {
                System.out.print(digits.pop());
            }
        }
    }
}
