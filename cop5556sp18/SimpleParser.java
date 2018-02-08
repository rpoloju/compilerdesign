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
	 * Block ::= { ( (Declaration | Statement) ; )* }
	 */

	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = { KW_input, KW_write, IDENTIFIER, KW_red, KW_blue, KW_green, KW_alpha, KW_while, KW_if,
			KW_show, KW_sleep };

	public void block() throws SyntaxException {
		match(LBRACE);
		while (isKind(firstDec) | isKind(firstStatement)) {
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
		// matching Type
		if (isKind(KW_int) || isKind(KW_boolean) || isKind(KW_image) || isKind(KW_float) || isKind(KW_filename))
			consume();
		match(IDENTIFIER);
		if (isKind(LSQUARE)) {
			match(LSQUARE);
			expression();
			match(COMMA);
			expression();
			match(RSQUARE);
		}
	}

	public void statement() throws SyntaxException {
		if (isKind(KW_input))
			statementInput();
		else if (isKind(KW_write))
			statementWrite();
		else if (isKind(IDENTIFIER) || isKind(KW_red) || isKind(KW_green) || isKind(KW_blue) || isKind(KW_alpha))
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
		if (isKind(IDENTIFIER) || isKind(KW_red) || isKind(KW_green) || isKind(KW_blue) || isKind(KW_alpha))
			LHS();
		match(OP_ASSIGN);
		expression();
	}

	public void statementWhile() throws SyntaxException {
		match(KW_while);
		match(LPAREN);
		expression();
		match(RPAREN);
		block();
	}

	public void statementIf() throws SyntaxException {
		match(KW_if);
		match(LPAREN);
		expression();
		match(RPAREN);
		block();
	}

	public void statementShow() throws SyntaxException {
		match(KW_show);
		expression();
	}

	public void statementSleep() throws SyntaxException {
		match(KW_sleep);
		expression();
	}

	Kind[] firstColor = { KW_red, KW_green, KW_blue, KW_alpha };

	public void color() throws SyntaxException {
		if (isKind(firstColor)) {
			consume();
		}
	}

	public void LHS() throws SyntaxException {
		if (isKind(IDENTIFIER) || isKind(firstColor)) {
			if (isKind(IDENTIFIER)) {
				consume();
				if (isKind(LSQUARE)) {
					pixelSelector();
				}
			} else if (isKind(firstColor)) {
				consume();
				match(LPAREN);
				match(IDENTIFIER);
				pixelSelector();
				match(RPAREN);
			}
		}

	}

	public void pixelSelector() throws SyntaxException {
		match(Kind.LSQUARE);
		expression();
		match(Kind.COMMA);
		expression();
		match(Kind.RSQUARE);
	}

	public void pixelExpression() throws SyntaxException {
		match(IDENTIFIER);
		pixelSelector();
	}

	public void pixelConstructor() throws SyntaxException {
		match(LPIXEL);
		expression();
		match(COMMA);
		expression();
		match(COMMA);
		expression();
		match(COMMA);
		expression();
		match(RPIXEL);
	}

	Kind[] funcName = { KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r, KW_int,
			KW_float, KW_width, KW_height, KW_red, KW_green, KW_blue, KW_alpha };

	public void functionName() throws SyntaxException {
		if (isKind(funcName)) {
			consume();
		}
	}

	public void functionApplication() throws SyntaxException {
		functionName();

		if (isKind(LPAREN)) {
			match(LPAREN);
			expression();
			match(RPAREN);
		} else if (isKind(LSQUARE)) {
			match(LSQUARE);
			expression();
			match(COMMA);
			expression();
			match(RSQUARE);
		}

	}

	Kind[] firstPredefinedName = { KW_Z, KW_default_height, KW_default_width };

	public void predefinedName() throws SyntaxException {
		if (isKind(KW_Z) || isKind(KW_default_height) || isKind(KW_default_width)) {
			consume();
		}
	}

	Kind[] firstPrimary = { INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL, LPAREN, IDENTIFIER, LPIXEL };

	public void primary() throws SyntaxException {
		if (isKind(firstPrimary) || isKind(funcName) || isKind(firstPredefinedName)) {
			if (isKind(INTEGER_LITERAL)) {
				consume();
			} else if (isKind(Kind.BOOLEAN_LITERAL)) {
				consume();
			} else if (isKind(FLOAT_LITERAL)) {
				consume();
			} else if (isKind(LPAREN)) {
				match(LPAREN);
				expression();
				match(RPAREN);
			} else if (isKind(funcName)) {
				functionApplication();
			} else if (isKind(IDENTIFIER)) {
				consume();
				if (isKind(LSQUARE)) {
					pixelExpression();
				}
			} else if (isKind(firstPredefinedName)) {
				consume();
			} else if (isKind(LPIXEL)) {
				pixelConstructor();
			}
		}
	}

	public void unaryExpressionNotPlusMinus() throws SyntaxException {
		if (isKind(OP_EXCLAMATION) || isKind(firstPrimary) || isKind(funcName) || isKind(firstPredefinedName)) {
			if (isKind(OP_EXCLAMATION)) {
				consume();
				unaryExpression();
			} else {
				primary();
			}
		}
	}

	public void unaryExpression() throws SyntaxException {
		if (isKind(OP_PLUS) || isKind(OP_MINUS) || isKind(OP_EXCLAMATION) || isKind(firstPrimary) || isKind(funcName)
				|| isKind(firstPredefinedName)) {
			if (isKind(OP_PLUS)) {
				unaryExpression();
			} else if (isKind(OP_MINUS)) {
				unaryExpression();
			} else {
				unaryExpressionNotPlusMinus();
			}
		}
	}

	public void powerExpression() throws SyntaxException {
		unaryExpression();
		if (isKind(OP_POWER)) {
			consume();
			powerExpression();
		}
	}

	public void multExpression() throws SyntaxException {
		powerExpression();
		while (isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_MOD)) {
			consume();
			powerExpression();
		}
	}

	public void addExpression() throws SyntaxException {
		multExpression();
		while (isKind(OP_PLUS) || isKind(OP_MINUS)) {
			consume();
			multExpression();
		}
	}

	public void relExpression() throws SyntaxException {
		addExpression();
		while (isKind(OP_LT) || isKind(OP_GT) || isKind(OP_LE) || isKind(OP_GE)) {
			consume();
			addExpression();
		}
	}

	public void eqExpression() throws SyntaxException {
		relExpression();
		while (isKind(OP_EQ) || isKind(OP_NEQ)) {
			consume();
			relExpression();
		}
	}

	public void andExpression() throws SyntaxException {
		eqExpression();
		while (isKind(OP_AND)) {
			consume();
			eqExpression();
		}
	}

	public void orExpression() throws SyntaxException {
		andExpression();
		while (isKind(OP_OR)) {
			consume();
			andExpression();
		}
	}

	public void expression() throws SyntaxException {
		orExpression();
		if (isKind(OP_QUESTION)) {
			consume();
			expression();
			match(OP_COLON);
			expression();
		}
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
		String message = "Expected EOL at " + t.line() + ":" + t.posInLine();
		throw new SyntaxException(t, message); // TODO give a better error message!
	}

	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind(EOF)) {
			throw new SyntaxException(t, "Syntax Error"); // TODO give a better error message!
			// Note that EOF should be matched by the matchEOF method which is called only
			// in parse().
			// Anywhere else is an error. */
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
		throw new SyntaxException(t, "Syntax Error"); // TODO give a better error message!
	}

}
