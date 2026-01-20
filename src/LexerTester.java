import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

public class LexerTester {
    Lexer l = new Lexer();
    /*************Test different correct input on lexer*************/
    @Test
    public void lexer1() throws Exception{
        TreeNode<String> testNode = l.lexSimplify("(simplify (+ 345 789))", new HashMap<String, String>());
        assertEquals("+", testNode.data);
        assertEquals("345", testNode.children.getFirst().data);
        assertEquals("789", testNode.children.getLast().data);
    }
    @Test
    public void lexer2() throws Exception{
        TreeNode<String> testNode = l.lexSimplify("(simplify (* 123 45))", new HashMap<String, String>());
        assertEquals("*", testNode.data);
        assertEquals("123", testNode.children.getFirst().data);
        assertEquals("45", testNode.children.getLast().data);
    }
    @Test
    public void lexer3() throws Exception{
        TreeNode<String> testNode = l.lexSimplify("(simplify (- 1123 2500))", new HashMap<String, String>());
        assertEquals("-", testNode.data);
        assertEquals("1123", testNode.children.getFirst().data);
        assertEquals("2500", testNode.children.getLast().data);
    }
    @Test
    public void lexer4() throws Exception{
        TreeNode<String> testNode = l.lexSimplify("(simplify (- -1123 2500))", new HashMap<String, String>());
        assertEquals("-", testNode.data);
        assertEquals("-1123", testNode.children.getFirst().data);
        assertEquals("2500", testNode.children.getLast().data);
    }
    @Test
    public void lexer5() throws Exception{
        TreeNode<String> testNode = l.lexSimplify("(simplify (- -1123 -2500))", new HashMap<String, String>());
        assertEquals("-", testNode.data);
        assertEquals("-1123", testNode.children.getFirst().data);
        assertEquals("-2500", testNode.children.getLast().data);
    }
    @Test
    public void lexer6() throws Exception{
        TreeNode<String> testNode = l.lexSimplify("(simplify (+ (- 1 2) (* 3 5)))", new HashMap<String, String>());
        assertEquals("+", testNode.data);
        assertEquals("-", testNode.children.getFirst().data);
        assertEquals("1", testNode.children.getFirst().children.getFirst().data);
        assertEquals("2", testNode.children.getFirst().children.getLast().data);
        assertEquals("*", testNode.children.getLast().data);
        assertEquals("3", testNode.children.getLast().children.getFirst().data);
        assertEquals("5", testNode.children.getLast().children.getLast().data);
    }
    @Test
    public void lexer7() throws Exception{
        TreeNode<String> testNode = l.lexSimplify("(simplify (+ (- 1 -2) (* 3 5)))", new HashMap<String, String>());
        assertEquals("+", testNode.data);
        assertEquals("-", testNode.children.getFirst().data);
        assertEquals("*", testNode.children.getLast().data);
    }
    @Test
    public void lexer8() throws Exception{
        TreeNode<String> testNode = l.lexSimplify("(simplify (+ (* 2 5) (- (+ 23 (- 4 50)) 4)))", new HashMap<String, String>());
        assertEquals("+", testNode.data);
        assertEquals("*", testNode.children.getFirst().data);
        assertEquals("2", testNode.children.getFirst().children.getFirst().data);
        assertEquals("5", testNode.children.getFirst().children.getLast().data);
        assertEquals("-", testNode.children.getLast().data);
        assertEquals("+", testNode.children.getLast().children.getFirst().data);
        assertEquals("4", testNode.children.getLast().children.getLast().data);
    }

    /*************Test different incorrect input on lexer*************/
    @Test
    public void incorrect1() throws Exception{
        assertThrows(Exception.class, () -> {
            l.lexSimplify("(simplify (- 1123 2500)", new HashMap<String, String>());
        });
    }
    @Test
    public void incorrect2() throws Exception{
        assertThrows(Exception.class, () -> {
            l.lexSimplify("(simplify ( 1123 2500))", new HashMap<String, String>());
        });
    }
    @Test
    public void incorrect3() throws Exception{
        assertThrows(Exception.class, () -> {
            l.lexSimplify("(simplify (+ 1123 234 2500))", new HashMap<String, String>());
        });
    }
    @Test
    public void incorrect4() throws Exception{
        assertThrows(Exception.class, () -> {
            l.lexSimplify("(- 1123 2500)", new HashMap<String, String>());
        });
    }
    @Test
    public void incorrect5() throws Exception{
        assertThrows(Exception.class, () -> {
            l.lexSimplify("(simplify (- 1123 2500)))", new HashMap<String, String>());
        });
    }
    @Test
    public void incorrect6() throws Exception{
        assertThrows(Exception.class, () -> {
            l.lexSimplify("(simplify (- 1123) 2500))", new HashMap<String, String>());
        });
    }
    @Test
    public void incorrect7() throws Exception{
        assertThrows(Exception.class, () -> {
            l.lexSimplify("( (- 1123 2500)", new HashMap<String, String>());
        });
    }
    @Test
    public void incorrect8() throws Exception{
        assertThrows(Exception.class, () -> {
            l.lexSimplify("(simplify (- && 2500))", new HashMap<String, String>());
        });
    }
}
