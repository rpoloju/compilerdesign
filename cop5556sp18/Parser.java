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
import cop5556sp18.AST.*;
import cop5556sp18.Scanner.Kind;
import static cop5556sp18.Scanner.Kind.*;

import java.util.List;
import java.util.ArrayList;

public class Parser {

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

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	/*
	 * Program ::= Identifier Block
	 */
	public Program program() throws SyntaxException {
		Program prog = null;
		Token firstToken = t;
		Token identifier = null;

		if (t.kind == Kind.IDENTIFIER) {
			identifier = t;
			consume();
		}

		Block b = block();
		prog = new Program(firstToken, identifier, b);
		return prog;
	}

	/*
	 * Block ::= { ( (Declaration | Statement) ; )* }
	 */

	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = { KW_input, KW_write, IDENTIFIER, KW_red, KW_blue, KW_green, KW_alpha, KW_while, KW_if,
			KW_show, KW_sleep };

	public Block block() throws SyntaxException {
		Block b = null;
		Declaration d = null;
		Statement s = null;
		List<ASTNode> decsOrStatements = new ArrayList<>();
		Token firstToken = t;
		match(LBRACE);
		while (isKind(firstDec) | isKind(firstStatement)) {
			if (isKind(firstDec)) {
				d = declaration();
				decsOrStatements.add(d);
			} else if (isKind(firstStatement)) {
				s = statement();
				decsOrStatements.add(s);
			}
			match(SEMI);
		}
		match(RBRACE);
		b = new Block(firstToken, decsOrStatements);
		return b;
	}

	/*
	 * Declaration ::= Type IDENTIFIER | image IDENTIFIER [ Expression , Expression
	 * ]
	 */

	public Declaration declaration() throws SyntaxException {
		// matching Type
		Declaration d = null;
		Expression width = null, height = null;
		Token firstToken = t;

		if (isKind(KW_int) || isKind(KW_boolean) || isKind(KW_image) || isKind(KW_float) || isKind(KW_filename))
			consume();

		// match(IDENTIFIER);
		Token ident = t;
		if (ident.kind == Kind.IDENTIFIER) {
			consume();
		}

		if (isKind(LSQUARE)) {
			match(LSQUARE);
			width = expression();
			match(COMMA);
			width = expression();
			match(RSQUARE);
		}
		d = new Declaration(firstToken, firstToken, ident, width, height);
		return d;
	}

	/*
	 * Statement ::= StatementInput | StatementWrite | StatementAssignment |
	 * StatementWhile | StatementIf | StatementShow | StatementSleep
	 */

	public Statement statement() throws SyntaxException {

		Statement stmt = null;

		if (isKind(KW_input))
			stmt = statementInput();

		else if (isKind(KW_write))
			stmt = statementWrite();

		else if (isKind(IDENTIFIER) || isKind(KW_red) || isKind(KW_green) || isKind(KW_blue) || isKind(KW_alpha))
			stmt = statementAssignment();

		else if (isKind(KW_while))
			stmt = statementWhile();

		else if (isKind(KW_if))
			stmt = statementIf();

		else if (isKind(KW_show))
			stmt = statementShow();

		else if (isKind(KW_sleep))
			stmt = statementSleep();

		else
			// this code is never reached since we are already checking for possible
			// scenarios in block() method
			throw new UnsupportedOperationException();
		return stmt;
	}

	/* StatementInput ::= input IDENTIFIER from @ Expression */

	public StatementInput statementInput() throws SyntaxException {
		Token firstToken = t;
		Token identifier;
		StatementInput stmtInput = null;
		Expression exp = null;

		match(KW_input);

		identifier = t;
		match(IDENTIFIER);

		match(KW_from);
		match(OP_AT);

		exp = expression();

		stmtInput = new StatementInput(firstToken, identifier, exp);
		return stmtInput;
	}

	/* StatementWrite ::= write IDENTIFIER to IDENTIFIER */

	public StatementWrite statementWrite() throws SyntaxException {
		Token firstToken = t;
		StatementWrite stmtWrite = null;
		Token identifier1, identifier2;

		match(KW_write);

		identifier1 = t;
		match(IDENTIFIER);

		match(KW_to);

		identifier2 = t;
		match(IDENTIFIER);

		stmtWrite = new StatementWrite(firstToken, identifier1, identifier2);
		return stmtWrite;
	}

	/* StatementAssignment ::= LHS := Expression */

	public StatementAssign statementAssignment() throws SyntaxException {
		Token firstToken = t;
		StatementAssign stmtAssignment = null;
		Expression exp = null;
		LHS lhs = null;

		if (isKind(IDENTIFIER) || isKind(KW_red) || isKind(KW_green) || isKind(KW_blue) || isKind(KW_alpha))
			lhs = LHS();

		match(OP_ASSIGN);

		exp = expression();

		stmtAssignment = new StatementAssign(firstToken, lhs, exp);
		return stmtAssignment;
	}

	/* StatementWhile ::= while (Expression ) Block */

	public StatementWhile statementWhile() throws SyntaxException {
		Token firstToken = t;
		StatementWhile stmtWhile = null;
		Expression exp = null;
		Block blk = null;

		match(KW_while);
		match(LPAREN);

		exp = expression();

		match(RPAREN);

		blk = block();

		stmtWhile = new StatementWhile(firstToken, exp, blk);
		return stmtWhile;
	}

	/* StatementIf ::= if ( Expression ) Block */

	public StatementIf statementIf() throws SyntaxException {
		Token firstToken = t;
		StatementIf stmtIf = null;
		Expression exp = null;
		Block blk = null;

		match(KW_if);
		match(LPAREN);

		exp = expression();

		match(RPAREN);

		blk = block();

		stmtIf = new StatementIf(firstToken, exp, blk);
		return stmtIf;
	}

	/* StatementShow ::= show Expression */

	public StatementShow statementShow() throws SyntaxException {
		Token firstToken = t;
		StatementShow stmtShow = null;
		Expression exp = null;

		match(KW_show);

		exp = expression();

		stmtShow = new StatementShow(firstToken, exp);
		return stmtShow;

	}

	/* StatementSleep ::= sleep Expression */

	public StatementSleep statementSleep() throws SyntaxException {
		Token firstToken = t;
		StatementSleep stmtSleep = null;
		Expression exp = null;

		match(KW_sleep);
		exp = expression();

		stmtSleep = new StatementSleep(firstToken, exp);
		return stmtSleep;
	}

	Kind[] firstColor = { KW_red, KW_green, KW_blue, KW_alpha };

	/* Color ::= red | green | blue | alpha */

	public void color() throws SyntaxException {
		if (isKind(firstColor)) {
			consume();
		}
	}

	/*
	 * LHS ::= IDENTIFIER | IDENTIFIER PixelSelector | Color ( IDENTIFIER
	 * PixelSelector )
	 */

	public LHS LHS() throws SyntaxException {
		LHS lhs = null;
		Token firstToken = t;
		PixelSelector ps = null;

		if (isKind(IDENTIFIER) || isKind(firstColor)) {
			if (isKind(IDENTIFIER)) {
				Token identifier = t;
				consume();
				if (isKind(LSQUARE)) {
					ps = pixelSelector();
					return new LHSPixel(firstToken, identifier, ps);
				} else {
					return new LHSIdent(firstToken, identifier);
				}

			} else if (isKind(firstColor)) {
				Token color = t;
				consume();
				match(LPAREN);
				Token identifier = t;
				match(IDENTIFIER);
				ps = pixelSelector();
				match(RPAREN);
				return new LHSSample(firstToken, identifier, ps, color);
			}
		}

		return lhs;

	}

	/* PixelSelector ::= [ Expression , Expression ] */

	public PixelSelector pixelSelector() throws SyntaxException {
		Token firstToken = t;
		PixelSelector ps = null;
		Expression e1 = null, e2 = null;
		match(Kind.LSQUARE);
		e1 = expression();
		match(Kind.COMMA);
		e2 = expression();
		match(Kind.RSQUARE);

		ps = new PixelSelector(firstToken, e1, e2);
		return ps;

	}

	/*
	 * PixelExpression ::= IDENTIFIER PixelSelector we do not need this method
	 * because pixelExpression() is possibly called only from the primary() method.
	 * The identifier is already consumed before checking if the identifier belongs
	 * to pixelExpression(). So once the identifier is consumed, it directly calls
	 * the pixelSelector() instead of pixelExpression()
	 */

	public void pixelExpression() throws SyntaxException {
		match(IDENTIFIER);
		pixelSelector();
	}

	/*
	 * PixelConstructor ::= << Expression , Expression , Expression , Expression >>
	 */

	public ExpressionPixelConstructor pixelConstructor() throws SyntaxException {
		Token firstToken = t;
		ExpressionPixelConstructor epc = null;
		Expression e1 = null, e2 = null, e3 = null, e4 = null;
		match(LPIXEL);
		e1 = expression();
		match(COMMA);
		e2 = expression();
		match(COMMA);
		e3 = expression();
		match(COMMA);
		e4 = expression();
		match(RPIXEL);

		epc = new ExpressionPixelConstructor(firstToken, e1, e2, e3, e4);
		return epc;
	}

	Kind[] funcName = { KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r, KW_int,
			KW_float, KW_width, KW_height, KW_red, KW_green, KW_blue, KW_alpha };

	/*
	 * FunctionName ::= sin | cos | atan | abs | log | cart_x | cart_y | polar_a |
	 * polar_r int | float | width | height | Color
	 */

	public Token functionName() throws SyntaxException {
		Token token = t;
		if (isKind(funcName)) {
			consume();
		}
		return token;
	}

	/*
	 * FunctionApplication ::= FunctionName ( Expression ) | FunctionName [
	 * Expression , Expression ]
	 */

	public Expression functionApplication() throws SyntaxException {
		Token firstToken = t;
		Token funcName = t;
		funcName = functionName();

		if (isKind(LPAREN)) {
			match(LPAREN);
			Expression exp = expression();
			match(RPAREN);
			return new ExpressionFunctionAppWithExpressionArg(firstToken, funcName, exp);

		} else if (isKind(LSQUARE)) {
			match(LSQUARE);
			Expression exp1 = expression();
			match(COMMA);
			Expression exp2 = expression();
			match(RSQUARE);
			return new ExpressionFunctionAppWithPixel(firstToken, funcName, exp1, exp2);
		}

		return null;

	}

	Kind[] firstPredefinedName = { KW_Z, KW_default_height, KW_default_width };

	/* PredefinedName ::= Z | default_height | default_width */

	public ExpressionPredefinedName predefinedName() throws SyntaxException {
		Token firstToken = t;
		if (isKind(KW_Z) || isKind(KW_default_height) || isKind(KW_default_width)) {
			Token predefinedName = t;
			consume();
			return new ExpressionPredefinedName(firstToken, predefinedName);
		}
		return null;
	}

	Kind[] firstPrimary = { INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL, LPAREN, IDENTIFIER, LPIXEL };

	/*
	 * Primary ::= INTEGER_LITERAL | BOOLEAN_LITERAL | FLOAT_LITERAL | ( Expression
	 * ) | FunctionApplication | IDENTIFIER | PixelExpression | PredefinedName |
	 * PixelConstructor
	 */

	public Expression primary() throws SyntaxException {
		Token firstToken = t;
		if (isKind(firstPrimary) || isKind(funcName) || isKind(firstPredefinedName)) {
			if (isKind(INTEGER_LITERAL)) {
				Token intVal = t;
				consume();
				return new ExpressionIntegerLiteral(firstToken, intVal);

			} else if (isKind(BOOLEAN_LITERAL)) {
				Token boolVal = t;
				consume();
				return new ExpressionBooleanLiteral(firstToken, boolVal);

			} else if (isKind(FLOAT_LITERAL)) {
				Token floatVal = t;
				consume();
				return new ExpressionFloatLiteral(firstToken, floatVal);

			} else if (isKind(LPAREN)) {
				match(LPAREN);
				Expression e0 = expression();
				match(RPAREN);
				return e0;

			} else if (isKind(funcName)) {
				Expression e0 = functionApplication();
				return e0;

			} else if (isKind(IDENTIFIER)) {
				Token identifier = t;
				consume();

				if (isKind(LSQUARE)) {
					PixelSelector ps = pixelSelector();
					return new ExpressionPixel(firstToken, identifier, ps);
				} else {
					return new ExpressionIdent(firstToken, identifier);
				}

			} else if (isKind(firstPredefinedName)) {
				Token pname = t;
				consume();
				return new ExpressionPredefinedName(firstToken, pname);

			} else if (isKind(LPIXEL)) {
				ExpressionPixelConstructor epc = pixelConstructor();
				return epc;
			}
		} else {
			throw new SyntaxException(t, "Syntax Error");
		}
		return null;
	}

	/* UnaryExpressionNotPlusMinus ::= ! UnaryExpression | Primary */

	public Expression unaryExpressionNotPlusMinus() throws SyntaxException {
		Token firstToken = t;
		if (isKind(OP_EXCLAMATION) || isKind(firstPrimary) || isKind(funcName) || isKind(firstPredefinedName)) {
			if (isKind(OP_EXCLAMATION)) {
				Token op = t;
				consume();
				Expression eu = unaryExpression();
				return new ExpressionUnary(firstToken, op, eu);

			} else {
				Expression prim = primary();
				return prim;
			}
		}
		return null;
	}

	/*
	 * UnaryExpression ::= + UnaryExpression | - UnaryExpression |
	 * UnaryExpressionNotPlusMinus
	 */

	public Expression unaryExpression() throws SyntaxException {
		Token firstToken = t;
		if (isKind(OP_PLUS) || isKind(OP_MINUS) || isKind(OP_EXCLAMATION) || isKind(firstPrimary) || isKind(funcName)
				|| isKind(firstPredefinedName)) {
			Token op = t;

			if (isKind(OP_PLUS)) {
				consume();
				Expression eu = unaryExpression();
				return new ExpressionUnary(firstToken, op, eu);

			} else if (isKind(OP_MINUS)) {
				consume();
				Expression eu = unaryExpression();
				return new ExpressionUnary(firstToken, op, eu);

			} else {
				Expression eunpm = unaryExpressionNotPlusMinus();
				return eunpm;
			}
		} else {
			String message = "Expected expression at line " + t.line() + ": position " + t.posInLine() + " found "
					+ t.kind.toString();
			throw new SyntaxException(t, message);
		}
	}

	/* PowerExpression := UnaryExpression (** PowerExpression | ε) */

	public Expression powerExpression() throws SyntaxException {
		Token firstToken = t;
		Token op = null;
		Expression e0 = null;
		Expression e1 = null;

		e0 = unaryExpression();

		if (isKind(OP_POWER)) {
			op = t;
			consume();
			e1 = powerExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}

	/* MultExpression := PowerExpression ( ( * | / | % ) PowerExpression ) **/

	public Expression multExpression() throws SyntaxException {
		Token firstToken = t;
		Token op = null;
		Expression e0 = null;
		Expression e1 = null;

		e0 = powerExpression();

		while (isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_MOD)) {
			op = t;
			consume();
			e1 = powerExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}

	/* AddExpression ::= MultExpression ( ( + | - ) MultExpression ) **/

	public Expression addExpression() throws SyntaxException {
		Token firstToken = t;
		Token op = null;
		Expression e0 = null;
		Expression e1 = null;

		e0 = multExpression();

		while (isKind(OP_PLUS) || isKind(OP_MINUS)) {
			op = t;
			consume();
			e1 = multExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}

	/* RelExpression ::= AddExpression ( (< | > | <= | >= ) AddExpression) **/

	public Expression relExpression() throws SyntaxException {
		Token firstToken = t;
		Token op = null;
		Expression e0 = null;
		Expression e1 = null;

		e0 = addExpression();

		while (isKind(OP_LT) || isKind(OP_GT) || isKind(OP_LE) || isKind(OP_GE)) {
			op = t;
			consume();
			e1 = addExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}

	/* EqExpression ::= RelExpression ( (== | != ) RelExpression ) **/

	public Expression eqExpression() throws SyntaxException {
		Token firstToken = t;
		Token op = null;
		Expression e0 = null;
		Expression e1 = null;

		e0 = relExpression();

		while (isKind(OP_EQ) || isKind(OP_NEQ)) {
			op = t;
			consume();
			e1 = relExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}

	/* AndExpression ::= EqExpression ( & EqExpression ) **/

	public Expression andExpression() throws SyntaxException {
		Token firstToken = t;
		Token op = null;
		Expression e0 = null;
		Expression e1 = null;

		e0 = eqExpression();

		while (isKind(OP_AND)) {
			op = t;
			consume();
			e1 = eqExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}

	/* OrExpression ::= AndExpression ( | AndExpression ) **/

	public Expression orExpression() throws SyntaxException {
		Token firstToken = t;
		Token op = null;
		Expression e0 = null;
		Expression e1 = null;

		e0 = andExpression();

		while (isKind(OP_OR)) {
			op = t;
			consume();
			e1 = andExpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}

	/*
	 * Expression ::= OrExpression ? Expression : Expression | OrExpression
	 */

	public Expression expression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = null, e1 = null, e2 = null;
		e0 = orExpression();

		if (isKind(OP_QUESTION)) {
			consume();
			e1 = expression();
			match(OP_COLON);
			e2 = expression();
			return new ExpressionConditional(firstToken, e0, e1, e2);
		}
		return e0;
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
		String message = "Expected " + kind.toString() + " at line " + t.line() + ": position " + t.posInLine()
				+ " found " + t.kind.toString();
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
		String message = "Expected EOL at " + t.line() + ":" + t.posInLine();
		throw new SyntaxException(t, message); // TODO give a better error message!
	}

}
