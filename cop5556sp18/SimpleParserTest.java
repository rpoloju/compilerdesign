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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Parser.SyntaxException;

public class SimpleParserTest {

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
	 * lacks an identifier and a block. The test case passes because it expects an
	 * exception
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testEmpty1() throws LexicalException, SyntaxException {
		String input = ""; // The input is the empty string.
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}

	/**
	 * Smallest legal program.
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testSmallest1() throws LexicalException, SyntaxException {
		String input = "b{}";
		Parser parser = makeParser(input);
		parser.parse();
	}

	// This test should pass in your complete parser. It will fail in the starter
	// code.
	// Of course, you would want a better error message.
	@Test
	public void testDec011() throws LexicalException, SyntaxException {
		String input = "b{int c;}";
		Parser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void test11() throws LexicalException, SyntaxException {
		String input = "b*"; // The input is the empty string.
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}

	@Test
	public void orTest() throws LexicalException, SyntaxException {
		String input = "abc {image xy[23 , 44]; int x; x := 10;}";
		Parser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void testError1() throws LexicalException, SyntaxException {
		String input = "block1{while{}}"; // The input is the empty string.
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}

	}

	@Test
	public void test01() throws LexicalException, SyntaxException {
		String input = "xyz{image xy[sin(80),cos(atan(abs(cart_x(width(20)))))];}";
		Parser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void test02() throws LexicalException, SyntaxException {
		String input = "abc{int ab;ab := (20 | 30 & 40 ** 50;}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test03() throws LexicalException, SyntaxException {
		String input = "abc{int ab;ab := (20 | 30 & 40 ** 50);}";
		Parser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void test04() throws LexicalException, SyntaxException {
		String input = "prog{if(a & ){};}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test05() throws LexicalException, SyntaxException {
		String input = "prog{if(a!=){};}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test06() throws LexicalException, SyntaxException {
		String input = "prog{image var [,]; }";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test07() throws LexicalException, SyntaxException {
		String input = "prog{show ;}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test08() throws LexicalException, SyntaxException {
		String input = "prog{if(a | b |){};}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test09() throws LexicalException, SyntaxException {
		String input = "prog{input var from @; }";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test10() throws LexicalException, SyntaxException {
		String input = "prog{sleep ;}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test12() throws LexicalException, SyntaxException {
		String input = "prog{var := ;}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test13() throws LexicalException, SyntaxException {
		String input = "prog{while (){};}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test14() throws LexicalException, SyntaxException {
		String input = "prog{int a;a := (2+3)==(3+2)?1:;}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test15() throws LexicalException, SyntaxException {
		String input = "prog{int a;a := (2+3)==(3+2)?:5;}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test16() throws LexicalException, SyntaxException {
		String input = "prog{ var [,] := 25;}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test17() throws LexicalException, SyntaxException {
		String input = "prog{if(a | b || c){};}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test18() throws LexicalException, SyntaxException {
		String input = "prog{if(a==){};}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test19() throws LexicalException, SyntaxException {
		String input = "prog{if (){};}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test20() throws LexicalException, SyntaxException {
		String input = "prog{if(a && b){};}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}

	@Test
	public void test21() throws LexicalException, SyntaxException {
		String input = "prog{show int();}";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();
		} catch (SyntaxException e) {
			show(e);
			throw (e);
		}
	}
	
	@Test
	public void test22() throws LexicalException, SyntaxException {
		String input = "sin x";
		show(input);
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}

}
