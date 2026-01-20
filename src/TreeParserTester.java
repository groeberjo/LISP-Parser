import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

public class TreeParserTester {
    TreeParser t = new TreeParser();
    Lexer l = new Lexer();

    /*************Test different correct input on parser*************/
    @Test
    public void parser1() throws Exception {
        assertEquals("1467", t.evaluateTree(l.lexSimplify("(simplify (+ 480 987))", new HashMap<String, String>())));
    }
    @Test
    public void parser2() throws Exception {
        assertEquals("295", t.evaluateTree(l.lexSimplify("(simplify (- 902 607))", new HashMap<String, String>())));
    }
    @Test
    public void parser3() throws Exception {
        assertEquals("814", t.evaluateTree(l.lexSimplify("(simplify (+ 780 34))", new HashMap<String, String>())));
    }
    @Test
    public void parser4() throws Exception {
        assertEquals("7", t.evaluateTree(l.lexSimplify("(simplify (+ (- 1 2) (+ 3 5)))", new HashMap<String, String>())));
    }
    @Test
    public void parser5() throws Exception {
        assertEquals("11", t.evaluateTree(l.lexSimplify("(simplify (+ (- 1 -2) (+ 3 5)))", new HashMap<String, String>())));
    }
    @Test
    public void parser6() throws Exception {
        assertEquals("(- 20)", t.evaluateTree(l.lexSimplify("(simplify (+ (+ 2 5) (- (+ 23 (- 4 50)) 4)))", new HashMap<String, String>())));
    }
    @Test
    public void parser7() throws Exception {
        assertEquals("0", t.evaluateTree(l.lexSimplify("(simplify (- 1 1))", new HashMap<String, String>())));
    }
    @Test
    public void parser8() throws Exception {
        assertEquals("(- 4)", t.evaluateTree(l.lexSimplify("(simplify (+ 1 -5))", new HashMap<String, String>())));
    }

    /*************Tests from sheet 4 as input (including bignums)*************/
    @Test
    public void parser9() throws Exception {
        assertEquals("864197532", t.evaluateTree(l.lexSimplify("(simplify (+ -123456789 987654321))", new HashMap<String, String>())));
    }
    @Test
    public void parser10() throws Exception {
        assertEquals("(- 7500000000)", t.evaluateTree(l.lexSimplify("(simplify (- -5000000000 2500000000))", new HashMap<String, String>())));
    }
    @Test
    public void parser11() throws Exception {
        assertEquals("(- 30)", t.evaluateTree(l.lexSimplify("(simplify (+ 15 -45))", new HashMap<String, String>())));
    }

    /*************Test input for multiplication*************/
    @Test
    public void multi1() throws Exception{
        assertEquals("0", t.evaluateTree(l.lexSimplify("(simplify (* 0 0))", new HashMap<String, String>())));
    }
    @Test
    public void multi2() throws Exception{
        assertEquals("0", t.evaluateTree(l.lexSimplify("(simplify (* 0 1))", new HashMap<String, String>())));
    }
    @Test
    public void multi3() throws Exception{
        assertEquals("0", t.evaluateTree(l.lexSimplify("(simplify (* 1 0))", new HashMap<String, String>())));
    }
    @Test
    public void multi4() throws Exception{
        assertEquals("1329227995784915872903807060280344576",
                     t.evaluateTree(l.lexSimplify("(simplify (* 1152921504606846976 1152921504606846976))", new HashMap<String, String>())));
    }
    @Test
    public void multi5() throws Exception{
        assertEquals("(- 1329227995784915872903807060280344576)",
                     t.evaluateTree(l.lexSimplify("(simplify (* -1152921504606846976 1152921504606846976))", new HashMap<String, String>())));
    }
    @Test
    public void multi6() throws Exception{
        assertEquals("100", t.evaluateTree(l.lexSimplify("(simplify (* -20 -5))", new HashMap<String, String>())));
    }
    @Test
    public void multi7() throws Exception{
        assertEquals("(- 121932631112635269)", t.evaluateTree(l.lexSimplify("(simplify (* -123456789 987654321))", new HashMap<String, String>())));
    }
    @Test
    public void multi8() throws Exception{
        assertEquals("(- 675)", t.evaluateTree(l.lexSimplify("(simplify (* 15 -45))", new HashMap<String, String>())));
    }

    /*************Tests for incorrect input*************/
    @Test
    public void incorrect1() throws Exception{
        TreeNode<String> ast = l.lexSimplify("(simplify (+ 170141183460469231731687303715884105727 1))", new HashMap<String, String>());
        assertThrows(Exception.class, () -> {
            t.evaluateTree(ast);
        });
    }
    @Test
    public void incorrect2() throws Exception{
        TreeNode<String> ast = new TreeNode<String>("test");
        assertThrows(Exception.class, () -> {
            t.evaluateTree(ast);
        });
    }
    @Test
    public void incorrect3() throws Exception{
        TreeNode<String> ast = l.lexSimplify("(simplify (+ -340282366920938463463374607431768211455 1))", new HashMap<String, String>());
        assertThrows(Exception.class, () -> {
            t.evaluateTree(ast);
        });
    }
    @Test
    public void incorrect4() throws Exception{
        TreeNode<String> ast = l.lexSimplify("(simplify (+ 340282366920938463463374607431768211455 1))", new HashMap<String, String>());
        assertThrows(Exception.class, () -> {
            t.evaluateTree(ast);
        });
    }
    @Test
    public void incorrect5() throws Exception{
        TreeNode<String> ast = l.lexSimplify("(simplify (+ 3402823669209384634633746074317682114551010 1))", new HashMap<String, String>());
        assertThrows(Exception.class, () -> {
            t.evaluateTree(ast);
        });
    }
    @Test
    public void incorrect6() throws Exception{
        TreeNode<String> ast = l.lexSimplify("(simplify (* 170141183460469231731687303715884105727 2))", new HashMap<String, String>());
        assertThrows(Exception.class, () -> {
            t.evaluateTree(ast);
        });
    }
    @Test
    public void incorrect7() throws Exception{
        TreeNode<String> ast = l.lexSimplify("(simplify (* 298347289347289374333 829347289347289374))", new HashMap<String, String>());
        assertThrows(Exception.class, () -> {
            t.evaluateTree(ast);
        });
    }
    @Test
    public void incorrect8() throws Exception{
        TreeNode<String> ast = new TreeNode<String>("&");
        ast.addChild("1234");
        assertThrows(Exception.class, () -> {
            t.evaluateTree(ast);
        });
    }
}
