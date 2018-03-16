/**
* JUunit tests for the Parser for the class project in COP5556 Programming Language Principles 
* at the University of Florida, Spring 2018.
* 
* This software is solely for the educational benefit of students 
* enrolled in the course during the Spring 2018 semester.  
* 
* This software, and any software derived from it,  may not be shared with others or posted to public web sites,
* either during the course or afterwards.
* 
*  @Beverly A. Sanders, 2018
*/

package cop5556sp18;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.AST.*;
import cop5556sp18.Parser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Scanner.Token;
import static cop5556sp18.Scanner.Kind.*;

public class ParserTest {

	// set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	// creates and returns a parser for the given input.
	private Parser makeParser(String input) throws LexicalException {
		// show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and initialize it
		// show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner);
		return parser;
	}

	/**
	 * Simple test case with an empty program. This throws an exception because it
	 * lacks an identifier and a block
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = ""; // The input is the empty string.
		thrown.expect(SyntaxException.class);
		Parser parser = makeParser(input);
		@SuppressWarnings("unused")
		Program p = parser.parse();
	}

	/**
	 * Smallest legal program.
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testSmallest() throws LexicalException, SyntaxException {
		String input = "b{}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		// show(p);
		assertEquals("b", p.progName);
		assertEquals(0, p.block.decsOrStatements.size());
	}

	/**
	 * Checks that an element in a block is a declaration with the given type and
	 * name. The element to check is indicated by the value of index.
	 * 
	 * @param block
	 * @param index
	 * @param type
	 * @param name
	 * @return
	 */
	Declaration checkDec(Block block, int index, Kind type, String name) {
		ASTNode node = block.decOrStatement(index);
		assertEquals(Declaration.class, node.getClass());
		Declaration dec = (Declaration) node;
		assertEquals(type, dec.type);
		assertEquals(name, dec.name);
		return dec;
	}

	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int c; image j;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		// show(p);
		checkDec(p.block, 0, Kind.KW_int, "c");
		checkDec(p.block, 1, Kind.KW_image, "j");
	}

	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "b{int c; image j; float abc; filename usr; boolean b;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		// show(p);
		checkDec(p.block, 0, Kind.KW_int, "c");
		checkDec(p.block, 1, Kind.KW_image, "j");
		checkDec(p.block, 2, Kind.KW_float, "abc");
		checkDec(p.block, 3, Kind.KW_filename, "usr");
		checkDec(p.block, 4, Kind.KW_boolean, "b");
	}

	/**
	 * This test illustrates how you can test specific grammar elements by
	 * themselves by calling the corresponding parser method directly, instead of
	 * calling parse. This requires that the methods are visible (not private).
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */

	@Test
	public void testExpression() throws LexicalException, SyntaxException {
		String input = "x + 2";
		Parser parser = makeParser(input);
		Expression e = parser.expression(); // call expression here instead of parse
		// show(e);
		assertEquals(ExpressionBinary.class, e.getClass());
		ExpressionBinary b = (ExpressionBinary) e;
		assertEquals(ExpressionIdent.class, b.leftExpression.getClass());
		ExpressionIdent left = (ExpressionIdent) b.leftExpression;
		assertEquals("x", left.name);
		assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
		ExpressionIntegerLiteral right = (ExpressionIntegerLiteral) b.rightExpression;
		assertEquals(2, right.value);
		assertEquals(OP_PLUS, b.op);
	}

	@Test
	public void testExpression1() throws LexicalException, SyntaxException {
		String input = "20 | 30 & 40 ** 50";
		Parser parser = makeParser(input);
		Expression e = parser.expression(); // call expression here instead of parse
		// show(e);
		assertEquals(ExpressionBinary.class, e.getClass());

	}

	@Test
	public void test01() throws LexicalException, SyntaxException {
		String input = "abc{int ab;ab := (20 | 30 & 40 ** 50);}";
		Parser parser = makeParser(input);
		Program p = parser.program(); // call expression here instead of parse
		// show(p);
		assertEquals(Program.class, p.getClass());
		assertEquals(Block.class, p.block.getClass());
		Block blk = (Block) p.block;
		assertEquals(Declaration.class, blk.decsOrStatements.get(0).getClass());

	}

	@Test
	public void testDemo3() throws LexicalException, SyntaxException {
		String input = "x**+2";
		Parser parser = makeParser(input);
		Expression p = parser.expression();
		show(p);
		assertEquals(p.toString(),
				"ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_POWER, rightExpression=ExpressionUnary [op=OP_PLUS, expression=ExpressionIntegerLiteral [value=2]]]");
	}

	@Test
	public void test001() throws LexicalException, SyntaxException {
		String input = "sin x";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			ExpressionFunctionAppWithExpressionArg f = (ExpressionFunctionAppWithExpressionArg) parser
					.functionApplication();
			show(f);
		} catch (Exception e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test0011() throws LexicalException, SyntaxException {
		String input = "sin(x)";
		Parser parser = makeParser(input);
		ExpressionFunctionAppWithExpressionArg f = (ExpressionFunctionAppWithExpressionArg) parser
				.functionApplication();
		show(f);
	}

	@Test
	public void test0012() throws LexicalException, SyntaxException {
		String input = "Za";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			ExpressionPredefinedName f = (ExpressionPredefinedName) parser.predefinedName();
			show(f);
		} catch (Exception e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test0013() throws LexicalException, SyntaxException {
		String input = "Z";
		Parser parser = makeParser(input);
		ExpressionPredefinedName f = (ExpressionPredefinedName) parser.predefinedName();
		show(f);
	}

}
