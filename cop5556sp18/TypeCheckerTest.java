package cop5556sp18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Parser;
import cop5556sp18.Parser.SyntaxException;
import cop5556sp18.Scanner;
import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Program;
import cop5556sp18.TypeChecker.SemanticException;

public class TypeCheckerTest {

	/*
	 * set Junit to be able to catch exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scans, parses, and type checks the input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		Scanner scanner = new Scanner(input).scan();
		// show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new Parser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		ASTVisitor v = new TypeChecker();
		ast.visit(v, null);
	}

	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */

	@Test
	public void emptyProg() throws Exception {
		String input = "emptyProg{}";
		typeCheck(input);
	}

	@Test
	public void expression1() throws Exception {
		String input = "prog {show 3+4;}";
		typeCheck(input);
	}

	@Test
	public void expression2_fail() throws Exception {
		String input = "prog { show true+4; }"; // error, incompatible types in binary expression
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}

	@Test
	public void sample1() throws Exception {
		String input = "abc {image xy[23 , 44]; int x; x := 10;}";
		typeCheck(input);
	}

	@Test
	public void sample11() throws Exception {
		String input = "prog{image image1; write image1 to image1;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}

	@Test
	public void sample111() throws Exception {
		String input = "prog {int var1; float var2; image var3;var1 := width(var3); var1 := height(var3); var2 := float(1); var1 := int(1.0);}";
		typeCheck(input);
	}

	@Test
	public void sample2() throws Exception {
		String input = "xyz{int a;a:=10;float b;b:=10.5;while (b > a) { boolean x; x:=true; b:=b-1.0;};}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}

	@Test
	public void sample3() throws Exception {
		String input = "xyz{int a;a:=10;float a;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}

	@Test
	public void scope5() throws Exception {
		String input = "abc{int x;x:=10;int y;int a;y:=20;while (y > x) {int a;a:=1;};while (x>y) {a:=2;};}";
		typeCheck(input);
	}

}
