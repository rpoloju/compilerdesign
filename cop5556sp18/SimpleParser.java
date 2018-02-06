package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles 
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


import cop5556sp18.Scanner.Token;
import cop5556sp18.Scanner.Kind;
import static cop5556sp18.Scanner.Kind.*;


public class SimpleParser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}



	Scanner scanner;
	Token t;

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public void parse() throws SyntaxException {
		program();
		matchEOF();
	}

	/*
	 * Program ::= Identifier Block
	 */
	public void program() throws SyntaxException {
		match(IDENTIFIER);
		block();
	}
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = {KW_input, KW_write, OP_ASSIGN, KW_while, KW_if, KW_show, KW_sleep};

	public void block() throws SyntaxException {
		match(LBRACE);
		while (isKind(firstDec)|isKind(firstStatement)) {
	     if (isKind(firstDec)) {
			declaration();
		} else if (isKind(firstStatement)) {
			statement();
		}
			match(SEMI);
		}
		match(RBRACE);

	}
	
	public void declaration() throws SyntaxException {
		//matching Type
		if (isKind(KW_int))
			match(KW_int);
		else if (isKind(KW_boolean))
			match(KW_boolean);
		else if (isKind(KW_image))
			match(KW_image);
		else if (isKind(KW_float))
			match(KW_float);
		else if (isKind(KW_filename))
			match(KW_filename);
		else
			throw new UnsupportedOperationException();
		
		if (isKind(IDENTIFIER)) {
			match(IDENTIFIER);
		} else if (isKind(KW_image)) {
			match(KW_image);
			match(IDENTIFIER);
			match(LSQUARE);
			expression();
			match(COMMA);
			expression();
			match(RSQUARE);
		} else
			throw new UnsupportedOperationException();
	}
	
	public void statement() throws SyntaxException {
		if (isKind(KW_input))
			statementInput();
		else if (isKind(KW_write))
			statementWrite();
		else if (isKind(OP_ASSIGN))
			statementAssignment();
		else if (isKind(KW_while))
			statementWhile();
		else if (isKind(KW_if))
			statementIf();
		else if (isKind(KW_show))
			statementShow();
		else if (isKind(KW_sleep))
			statementSleep();
		else 
			throw new UnsupportedOperationException();
	}
	
	public void statementInput() throws SyntaxException {
		match(KW_input);
		match(IDENTIFIER);
		match(KW_from);
		match(OP_AT);
		expression();
	}
	
	public void statementWrite() throws SyntaxException {
		match(KW_write);
		match(IDENTIFIER);
		match(KW_to);
		match(IDENTIFIER);
	}
	
	public void statementAssignment() throws SyntaxException {
		LHS();
		match(OP_ASSIGN);
		expression();
	}
	
	public void statementWhile() throws SyntaxException {
		
	}
	
	public void statementIf() throws SyntaxException {
		
	}
	
	public void statementShow() throws SyntaxException {
		
	}
	
	public void statementSleep() throws SyntaxException {
		
	}
	
	public void expression() {
		
	}
	
	public void LHS() throws SyntaxException {
		match(IDENTIFIER);
		
	}

	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		String message =  "Expected EOL at " + t.line() + ":" + t.posInLine();
		throw new SyntaxException(t,message); //TODO  give a better error message!
	}


	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind( EOF)) {
			throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!  
			//Note that EOF should be matched by the matchEOF method which is called only in parse().  
			//Anywhere else is an error. */
		}
		t = scanner.nextToken();
		return tmp;
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!
	}
	

}

