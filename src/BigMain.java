import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigMain {
    private static final Logger log = LoggerFactory.getLogger(BigMain.class);
    /* Reads string and returns content as BigNum */
    public static BigNum stringToBigNum(String numberString) throws Exception {
        boolean minusFlag = false;
        BigNum number = new BigNum();
        // make sure - is at first position
        int minusCount = 0;
        for (char character: numberString.toCharArray()) {
            // detected a minus at first position
            if (character == '-' && !minusFlag && minusCount == 0) {
                minusFlag = true;
                minusCount = 1;
            } 
            
            // input is a digit
            else if (Character.isDigit(character)) {
                int charValue = Character.getNumericValue(character);
                BigNum tmp1 = Operations.bigNumShiftLeft(3, number);
                BigNum tmp2 = Operations.bigNumShiftLeft(1, number);
                number = Operations.bignumAdd(tmp1, tmp2);
                BigNum inputNum = new BigNum();
                inputNum.assignToNum(charValue);
                number = Operations.bignumAdd(inputNum, number);

                // number we read is too big if it has a 1 at MSB
                if (((number.values[3] >> 31) & 1) == 1) {
                    throw new Exception("nummber is too big for signed 128-bit word");
                }
                minusCount = 1;
            }

            // else undesired input
            else {
                throw new Exception("string can't be converted to BigNum");
            }
        }
        // invert number if it is negative
        if (minusFlag) {
            number.invertNum();
        }
        return (number);
    }
}

