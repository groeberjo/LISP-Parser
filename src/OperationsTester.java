import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class OperationsTester {

    /********* Test BigNumShiftLeft *********/
    @Test
    public void testBigNumShiftLeft1() throws Exception {
        BigNum b = new BigNum();
        b = Operations.bigNumShiftLeft(0, b);
        assertEquals("0", b.toString());
    }

    /********* Test Printing Hex *********/
    @Test
    public void testHex1() throws Exception{
        // Save the original System.out
        PrintStream originalOut = System.out;

        // Create a ByteArrayOutputStream to capture the output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        try {
            BigNum b = BigMain.stringToBigNum("489573489759384792304");
            Operations.bignumPrintHex(b);

            // Get the captured output and trim it
            String output = outputStream.toString().trim();

            // Assert that the output matches the expected string
            assertEquals("1A8A326F108C786CF0", output);

        } finally {
            // Restore the original System.out
            System.setOut(originalOut);
        }
    }
    @Test
    public void testHex2() throws Exception{
        // Save the original System.out
        PrintStream originalOut = System.out;

        // Create a ByteArrayOutputStream to capture the output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        try {
            BigNum b = new BigNum();
            Operations.bignumPrintHex(b);

            // Get the captured output and trim it
            String output = outputStream.toString().trim();

            // Assert that the output matches the expected string
            assertEquals("0.", output);

        } finally {
            // Restore the original System.out
            System.setOut(originalOut);
        }
    }

    /********* Test Printing Decimal *********/
    @Test
    public void testDec1() throws Exception{
        // Save the original System.out
        PrintStream originalOut = System.out;

        // Create a ByteArrayOutputStream to capture the output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        try {
            BigNum b = BigMain.stringToBigNum("14");
            Operations.bigNumPrintDecimal(b);

            // Get the captured output and trim it
            String output = outputStream.toString().trim();

            // Assert that the output matches the expected string
            assertEquals("14", output);

        } finally {
            // Restore the original System.out
            System.setOut(originalOut);
        }
    }
    @Test
    public void testDec2() throws Exception{
        // Save the original System.out
        PrintStream originalOut = System.out;

        // Create a ByteArrayOutputStream to capture the output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        try {
            BigNum b = BigMain.stringToBigNum("-14");
            Operations.bigNumPrintDecimal(b);

            // Get the captured output and trim it
            String output = outputStream.toString().trim();

            // Assert that the output matches the expected string
            assertEquals("-14", output);

        } finally {
            // Restore the original System.out
            System.setOut(originalOut);
        }
    }
    @Test
    public void testDec3() throws Exception{
        // Save the original System.out
        PrintStream originalOut = System.out;

        // Create a ByteArrayOutputStream to capture the output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        try {
            BigNum b = new BigNum();
            Operations.bigNumPrintDecimal(b);

            // Get the captured output and trim it
            String output = outputStream.toString().trim();

            // Assert that the output matches the expected string
            assertEquals("0", output);

        } finally {
            // Restore the original System.out
            System.setOut(originalOut);
        }
    }

    /********* Test BigNumShiftLeft for errors *********/
    @Test
    public void errorShiftLeft1() throws Exception {
        BigNum b = new BigNum();
        assertThrows(Exception.class, () -> {
            Operations.bigNumShiftLeft(-1, b);
        });
    }

    @Test
    public void errorShiftLeft2() throws Exception {
        assertThrows(Exception.class, () -> {
            Operations.bigNumShiftLeft(1, null);
        });
    }

    @Test
    public void errorShiftLeft3() throws Exception {
        BigNum b = new BigNum();
        assertThrows(Exception.class, () -> {
            Operations.bigNumShiftLeft(129, b);
        });
    }

    @Test
    public void errorShiftLeft4() throws Exception {
        BigNum b = new BigNum();
        b.values[3] = 1;
        assertThrows(Exception.class, () -> {
            Operations.bigNumShiftLeft(32, b);
        });
    }

    @Test
    public void errorShiftLeft5() throws Exception {
        BigNum b = new BigNum();
        b.values[3] = -1;
        assertThrows(Exception.class, () -> {
            Operations.bigNumShiftLeft(5, b);
        });
    }
}
