 /**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
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

import static cop5556sp18.Scanner.Kind.BOOLEAN_LITERAL;
import static cop5556sp18.Scanner.Kind.DOT;
import static cop5556sp18.Scanner.Kind.FLOAT_LITERAL;
import static cop5556sp18.Scanner.Kind.IDENTIFIER;
import static cop5556sp18.Scanner.Kind.INTEGER_LITERAL;
import static cop5556sp18.Scanner.Kind.KW_boolean;
import static cop5556sp18.Scanner.Kind.KW_float;
import static cop5556sp18.Scanner.Kind.KW_int;
import static cop5556sp18.Scanner.Kind.KW_sin;
import static cop5556sp18.Scanner.Kind.LPAREN;
import static cop5556sp18.Scanner.Kind.OP_ASSIGN;
import static cop5556sp18.Scanner.Kind.OP_AT;
import static cop5556sp18.Scanner.Kind.OP_COLON;
import static cop5556sp18.Scanner.Kind.OP_GE;
import static cop5556sp18.Scanner.Kind.OP_QUESTION;
import static cop5556sp18.Scanner.Kind.OP_TIMES;
import static cop5556sp18.Scanner.Kind.RPAREN;
import static cop5556sp18.Scanner.Kind.SEMI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Scanner.Token;

public class ScannerTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(pos, t.pos);
		assertEquals(length, t.length);
		assertEquals(line, t.line());
		assertEquals(pos_in_line, t.posInLine());
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}
	


	/**
	 * Simple test case with an empty program.  The only Token will be the EOF Token.
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}
	
	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, the end of line 
	 * character would be inserted by the text editor.
	 * Showing the input will let you check your input is 
	 * what you think it is.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws LexicalException {
		String input = ";;\n;;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	

	
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failIllegalChar1() throws LexicalException {
		String input = ";;~";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(2,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
	@Test
	public void failIllegalChar2() throws LexicalException {
		String input = "x = 4";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(3,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
	@Test
	public void testOperators1() throws LexicalException {
		String input = "float x = 3 >= 4 ? 10.5 : 20.5";
		Scanner scanner;
		show(input);
		thrown.expect(LexicalException.class);
		try {
			scanner = new Scanner(input).scan();
			show(scanner);
			checkNext(scanner, KW_float, 0, 5, 1, 1);
			checkNext(scanner, IDENTIFIER, 6, 1, 1, 7);
			checkNext(scanner, INTEGER_LITERAL, 10, 1, 1, 11);
			checkNext(scanner, OP_GE, 12, 2, 1, 13);
			checkNext(scanner, INTEGER_LITERAL, 15, 1, 1, 16);
			checkNext(scanner, OP_QUESTION, 17, 1, 1, 18);
			checkNext(scanner, FLOAT_LITERAL, 19, 4, 1, 20);
			checkNext(scanner, OP_COLON, 24, 1, 1, 25);
			checkNext(scanner, FLOAT_LITERAL, 26, 4, 1, 27);
			checkNextIsEOF(scanner);
		} catch (LexicalException e) {
			show(e);
			assertEquals(9,e.getPos());
			throw e;
		}
	}
	
	@Test
	public void testEqual() throws LexicalException {
		String input = "float x :=3 >= 4 ? 10.5 : 20.5";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_float, 0, 5, 1, 1);
		checkNext(scanner, IDENTIFIER, 6, 1, 1, 7);
		checkNext(scanner, OP_ASSIGN, 8, 2, 1, 9);
		checkNext(scanner, INTEGER_LITERAL, 10, 1, 1, 11);
		checkNext(scanner, OP_GE, 12, 2, 1, 13);
		checkNext(scanner, INTEGER_LITERAL, 15, 1, 1, 16);
		checkNext(scanner, OP_QUESTION, 17, 1, 1, 18);
		checkNext(scanner, FLOAT_LITERAL, 19, 4, 1, 20);
		checkNext(scanner, OP_COLON, 24, 1, 1, 25);
		checkNext(scanner, FLOAT_LITERAL, 26, 4, 1, 27);
		checkNextIsEOF(scanner);
	}




	@Test
	public void testParens() throws LexicalException {
		String input = "()";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LPAREN, 0, 1, 1, 1);
		checkNext(scanner, RPAREN, 1, 1, 1, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testInteger() throws LexicalException {
		String input = "123";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 3, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testIntegerStattingZero() throws LexicalException {
		String input = "0123";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 1, 3, 1, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testFloat1() throws LexicalException {
		String input = "0.01";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 4, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testFloat2() throws LexicalException {
		String input = "0.00";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 4, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testFloat3() throws LexicalException {
		String input = "0.01\n1";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 4, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 5, 1, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testFloat4() throws LexicalException {
		String input = "0.01.3";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 4, 1, 1);
		checkNext(scanner, FLOAT_LITERAL, 4, 2, 1, 5);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testFloat5() throws LexicalException {
		String input = "0.00.000.00.0.0.00";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 4, 1, 1);
		checkNext(scanner, FLOAT_LITERAL, 4, 4, 1, 5);
		checkNext(scanner, FLOAT_LITERAL, 8, 3, 1, 9);
		checkNext(scanner, FLOAT_LITERAL, 11, 2, 1, 12);
		checkNext(scanner, FLOAT_LITERAL, 13, 2, 1, 14);
		checkNext(scanner, FLOAT_LITERAL, 15, 3, 1, 16);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testComments() throws LexicalException {
		String input = "456/*This is shit*/123";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 3, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 19, 3, 1, 20);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testNewLine() throws LexicalException {
		String input = "4\n5";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 2, 1, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testAssign() throws LexicalException {
		String input = "/*commented*/boolean b := true";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_boolean, 13, 7, 1, 14);
		checkNext(scanner, IDENTIFIER, 21, 1, 1, 22);
		checkNext(scanner, OP_ASSIGN, 23, 2, 1, 24);
		checkNext(scanner, BOOLEAN_LITERAL, 26, 4, 1, 27);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testMultilineComments() throws LexicalException {
		String input = "123/*shit\nshitagain*/boolean b := true";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 3, 1, 1);
		checkNext(scanner, KW_boolean, 21, 7, 2, 12);
		checkNext(scanner, IDENTIFIER, 29, 1, 2, 20);
		checkNext(scanner, OP_ASSIGN, 31, 2, 2, 22);
		checkNext(scanner, BOOLEAN_LITERAL, 34, 4, 2, 25);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testKeyWords1() throws LexicalException {
		String input = "int x := sin(90);";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_int, 0, 3, 1, 1);
		checkNext(scanner, IDENTIFIER, 4, 1, 1, 5);
		checkNext(scanner, OP_ASSIGN, 6, 2, 1, 7);
		checkNext(scanner, KW_sin, 9, 3, 1, 10);
		checkNext(scanner, LPAREN, 12, 1, 1, 13);
		checkNext(scanner, INTEGER_LITERAL, 13, 2, 1, 14);
		checkNext(scanner, RPAREN, 15, 1, 1, 16);
		checkNext(scanner, SEMI, 16, 1, 1, 17);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testKeyWords2() throws LexicalException {
		String input = "flo\nat";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 3, 1, 1);
		checkNext(scanner, IDENTIFIER, 4, 2, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void testZero() throws LexicalException {
		String input = "000";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 1, 1, 1, 2);
		checkNext(scanner, INTEGER_LITERAL, 2, 1, 1, 3);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testDot1() throws LexicalException {
		String input = "0.1.a";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 3, 1, 1);
		checkNext(scanner, DOT, 3, 1, 1, 4);
		checkNext(scanner, IDENTIFIER, 4, 1, 1, 5);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testDot2() throws LexicalException {
		String input = "0..1";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 2, 1, 1);
		checkNext(scanner, FLOAT_LITERAL, 2, 2, 1, 3);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testDot3() throws LexicalException {
		String input = "0.sin";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 2, 1, 1);
		checkNext(scanner, KW_sin, 2, 3, 1, 3);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testKeyWords3() throws LexicalException {
		String input = "sincosatanlogtrueabs";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 20, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testNewLine1() throws LexicalException {
		String input = "\n\n\n12\n\n34\n.\n0\n>=";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 3, 2, 4, 1);
		checkNext(scanner, INTEGER_LITERAL, 7, 2, 6, 1);
		checkNext(scanner, DOT, 10, 1, 7, 1);
		checkNext(scanner, INTEGER_LITERAL, 12, 1, 8, 1);
		checkNext(scanner, OP_GE, 14, 2, 9, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testDots() throws LexicalException {
		String input = "..0.3..20....";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, DOT, 0, 1, 1, 1);
		checkNext(scanner, FLOAT_LITERAL, 1, 2, 1, 2);
		checkNext(scanner, FLOAT_LITERAL, 3, 2, 1, 4);
		checkNext(scanner, DOT, 5, 1, 1, 6);
		checkNext(scanner, FLOAT_LITERAL, 6, 3, 1, 7);
		checkNext(scanner, DOT, 9, 1, 1, 10);
		checkNext(scanner, DOT, 10, 1, 1, 11);
		checkNext(scanner, DOT, 11, 1, 1, 12);
		checkNext(scanner, DOT, 12, 1, 1, 13);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testIdentifier2() throws LexicalException {
		String input = "num1.*num2";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 4, 1, 1);
		checkNext(scanner, DOT, 4, 1, 1, 5);
		checkNext(scanner, OP_TIMES, 5, 1, 1, 6);
		checkNext(scanner, IDENTIFIER, 6, 4, 1, 7);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testIdentifier1() throws LexicalException {
		String input = "num1$$1$..@num__1*1";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 8, 1, 1);
		checkNext(scanner, DOT, 8, 1, 1, 9);
		checkNext(scanner, DOT,9, 1, 1, 10);
		checkNext(scanner, OP_AT, 10, 1, 1, 11);
		checkNext(scanner, IDENTIFIER, 11, 6, 1, 12);
		checkNext(scanner, OP_TIMES, 17, 1, 1, 18);
		checkNext(scanner, INTEGER_LITERAL, 18, 1, 1, 19);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void failForEquals() throws LexicalException {
		String input = "=====q";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(5,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
	@Test
	public void failForCommentNotClosed() throws LexicalException {
		String input = "/*test";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(6,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
	@Test
	public void testDot() throws LexicalException {
		String input = "1.2.3";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 3, 1, 1);
		checkNext(scanner, FLOAT_LITERAL, 3, 2, 1, 4);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void failForIntOverflow() throws LexicalException {
		String input = "2147483648";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(10,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
	@Test
	public void failForUnclosedComment() throws LexicalException {
		String input = "/**";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(3,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
	@Test
	public void testDot4() throws LexicalException {
		String input = "..1..";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, DOT, 0, 1, 1, 1);
		checkNext(scanner, FLOAT_LITERAL, 1, 2, 1, 2);
		checkNext(scanner, DOT, 3, 1, 1, 4);
		checkNext(scanner, DOT, 4, 1, 1, 5);
		checkNextIsEOF(scanner);
	}
	
	
}
	

