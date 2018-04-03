package cop5556sp18;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;
import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHS;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;

public class TypeChecker implements ASTVisitor {
	SymbolTable symtable = new SymbolTable();

	TypeChecker() {
	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// program.visit(this,null);
		program.block.visit(this, arg);
		return program;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symtable.enterScope();

		for (ASTNode eachNode : block.decsOrStatements) {
			eachNode.visit(this, arg);
		}
		symtable.leaveScope();
		return block;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {

		String decName = declaration.name;

		// If the declaration is already present in the symbol table, throw Semantic
		// Exception
		if (symtable.getDeclarationinCurrentScope(decName) != null) {
			String message = decName.toString() + " is already in the current scope";
			throw new SemanticException(null, message);
		}

		Expression ewidth = declaration.width;
		Expression eheight = declaration.height;

		if (!(ewidth == null) && !(eheight == null)) {
			ewidth.visit(this, arg);
			eheight.visit(this, arg);
		}

		if ((ewidth == null && eheight != null) || (ewidth != null && eheight == null)) {
			throw new SemanticException(declaration.firstToken, "Semantic Exception");
		}

		if (!((ewidth == null)
				|| (ewidth.type.equals(Type.INTEGER) && declaration.firstToken.kind.equals(Kind.KW_image)))) {
			throw new SemanticException(ewidth.firstToken, "Semantic Exception");
		}

		if (!((eheight == null)
				|| (eheight.type.equals(Type.INTEGER) && declaration.firstToken.kind.equals(Kind.KW_image)))) {
			throw new SemanticException(eheight.firstToken, "Semantic Exception");
		}

		Boolean addDeclaration = symtable.addToMap(decName, declaration);

		if (!addDeclaration) {
			String message = decName.toString() + " is already in the current scope";
			throw new SemanticException(declaration.firstToken, message);
		}

		return declaration;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {

		Declaration writeSource = symtable.getDeclaration(statementWrite.sourceName);
		if (writeSource == null) {
			String message = statementWrite.sourceName.toString() + " is not declared in the current scope";
			throw new SemanticException(null, message);
		}

		Declaration writeDest = symtable.getDeclaration(statementWrite.destName);
		if (writeDest == null) {
			String message = statementWrite.destName.toString() + " is not declared in the current scope";
			throw new SemanticException(null, message);
		}

		statementWrite.sourceDec = writeSource;
		statementWrite.declDec = writeDest;
		if (!(writeSource.firstToken.kind.equals(Kind.KW_image)
				&& (writeDest.firstToken.kind.equals(Kind.KW_filename)))) {
			String message = "Type Mismatch in StatementWrite";
			throw new SemanticException(null, message);
		}

		return statementWrite;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		statementInput.e.visit(this, arg);

		Declaration declaration = symtable.getDeclaration(statementInput.destName);
		statementInput.dec = declaration;
		if (declaration == null) {
			String message = statementInput.destName.toString() + " is not declared in the current scope";
			throw new SemanticException(null, message);
		}

		// The value of the expression indicates the index of the input in the array of
		// command line parameters, so it needs to be an integer.
		if (!statementInput.e.type.equals(Type.INTEGER)) {
			String message = "Type Mismatch in StatementInput";
			throw new SemanticException(null, message);
		}

		return statementInput;

	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {

		pixelSelector.ex.visit(this, arg);
		pixelSelector.ey.visit(this, arg);

		Type xType = pixelSelector.ex.type;
		Type yType = pixelSelector.ey.type;

		if (!(xType.equals(yType))) {
			String message = "Type Mismatch in PixelSelector";
			throw new SemanticException(null, message);
		} else if (xType.equals(yType)) {
			if (!(xType.equals(Type.INTEGER) || xType.equals(Type.FLOAT))) {
				String message = "Type Mismatch in PixelSelector";
				throw new SemanticException(null, message);
			}
		}

		return pixelSelector;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		expressionConditional.guard.visit(this, arg);
		expressionConditional.trueExpression.visit(this, arg);
		expressionConditional.falseExpression.visit(this, arg);

		if (!expressionConditional.guard.type.equals(Type.BOOLEAN)) {
			String message = "The first token is not a boolean literal";
			throw new SemanticException(expressionConditional.guard.firstToken, message);
		}

		if (!(expressionConditional.trueExpression.type.equals(expressionConditional.falseExpression.type))) {
			String message = "Type mismatch in ExpressionConditional";
			throw new SemanticException(null, message);
		}
		expressionConditional.type = expressionConditional.trueExpression.type;
		return expressionConditional;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {

		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);
		expressionBinary.type = inferredType(expressionBinary.leftExpression.type,
				expressionBinary.rightExpression.type, expressionBinary.op);
		return expressionBinary;
	}

	public Types.Type inferredType(Type left, Type right, Kind op) throws SemanticException {
		switch (op) {
		case OP_PLUS:
		case OP_MINUS:
		case OP_TIMES:
		case OP_DIV:
		case OP_POWER: {
			if (left.equals(Type.INTEGER) && right.equals(Type.INTEGER)) {
				return Type.INTEGER;
			} else if (left.equals(Type.FLOAT) && right.equals(Type.FLOAT)) {
				return Type.FLOAT;
			} else if (left.equals(Type.FLOAT) && right.equals(Type.INTEGER)) {
				return Type.FLOAT;
			} else if (left.equals(Type.INTEGER) && right.equals(Type.FLOAT)) {
				return Type.FLOAT;
			} else {
				throw new SemanticException(null, "Unsupported Operation");
			}
		}

		case OP_MOD: {
			if (left.equals(Type.INTEGER) && right.equals(Type.INTEGER)) {
				return Type.INTEGER;
			} else {
				throw new SemanticException(null, "Unsupported Operation");
			}
		}
		case OP_AND:
		case OP_OR: {
			if (left.equals(Type.INTEGER) && right.equals(Type.INTEGER)) {
				return Type.INTEGER;
			} else if (left.equals(Type.BOOLEAN) && right.equals(Type.BOOLEAN)) {
				return Type.BOOLEAN;
			} else {
				throw new SemanticException(null, "Unsupported Operation");
			}
		}

		case OP_EQ:
		case OP_NEQ:
		case OP_GT:
		case OP_GE:
		case OP_LT:
		case OP_LE: {
			if (left.equals(Type.INTEGER) && right.equals(Type.INTEGER)) {
				return Type.BOOLEAN;
			} else if (left.equals(Type.FLOAT) && right.equals(Type.FLOAT)) {
				return Type.BOOLEAN;
			} else if (left.equals(Type.BOOLEAN) && right.equals(Type.BOOLEAN)) {
				return Type.BOOLEAN;
			} else {
				throw new SemanticException(null, "Unsupported Operation");
			}
		}
		default:
			throw new SemanticException(null, "Unsupported Operation");

		}
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		expressionUnary.expression.visit(this, arg);
		expressionUnary.type = expressionUnary.expression.type;
		return expressionUnary;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {

		expressionIntegerLiteral.type = Types.getType(Kind.KW_int);
		return expressionIntegerLiteral;
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		expressionBooleanLiteral.type = Types.getType(Kind.KW_boolean);
		return expressionBooleanLiteral;
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		expressionPredefinedName.type = Types.getType(Kind.KW_int);
		return expressionPredefinedName;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		expressionFloatLiteral.type = Types.getType(Kind.KW_float);
		return expressionFloatLiteral;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		expressionFunctionAppWithExpressionArg.e.visit(this, arg);

		expressionFunctionAppWithExpressionArg.type = inferredTypeFunctionApp(
				expressionFunctionAppWithExpressionArg.function, expressionFunctionAppWithExpressionArg.e.type);
		return expressionFunctionAppWithExpressionArg;
	}

	public Types.Type inferredTypeFunctionApp(Kind function, Types.Type type) throws SemanticException {

		if ((function.equals(Kind.KW_abs) || function.equals(Kind.KW_red) || function.equals(Kind.KW_green)
				|| function.equals(Kind.KW_blue) || function.equals(Kind.KW_alpha)) && type.equals(Type.INTEGER)) {
			return Type.INTEGER;

		} else if ((function.equals(Kind.KW_abs) || function.equals(Kind.KW_sin) || function.equals(Kind.KW_cos)
				|| function.equals(Kind.KW_atan) || function.equals(Kind.KW_log)) && type.equals(Type.FLOAT)) {
			return Type.FLOAT;

		} else if ((function.equals(Kind.KW_width) || function.equals(Kind.KW_height)) && type.equals(Type.IMAGE)) {
			return Type.INTEGER;

		} else if (function.equals(Kind.KW_float) && type.equals(Type.INTEGER)) {
			return Type.FLOAT;

		} else if (function.equals(Kind.KW_float) && type.equals(Type.FLOAT)) {
			return Type.FLOAT;

		} else if (function.equals(Kind.KW_int) && type.equals(Type.FLOAT)) {
			return Type.INTEGER;

		} else if (function.equals(Kind.KW_int) && type.equals(Type.INTEGER)) {
			return Type.INTEGER;

		} else {
			String message = "Semantic Error";
			throw new SemanticException(null, message);
		}
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		expressionFunctionAppWithPixel.e0.visit(this, arg);
		expressionFunctionAppWithPixel.e1.visit(this, arg);

		if (expressionFunctionAppWithPixel.name.equals(Kind.KW_cart_x)
				|| expressionFunctionAppWithPixel.name.equals(Kind.KW_cart_y)) {
			if (!(expressionFunctionAppWithPixel.e0.type.equals(Type.FLOAT)
					&& expressionFunctionAppWithPixel.e1.type.equals(Type.FLOAT))) {
				String message = "Incompatible types in ExpressionFunctionAppWithPixel";
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken, message);
			} else {
				expressionFunctionAppWithPixel.type = Type.INTEGER;
			}
		}

		if (expressionFunctionAppWithPixel.name.equals(Kind.KW_polar_a)
				|| expressionFunctionAppWithPixel.name.equals(Kind.KW_polar_r)) {
			if (!(expressionFunctionAppWithPixel.e0.type.equals(Type.INTEGER)
					&& expressionFunctionAppWithPixel.e1.type.equals(Type.INTEGER))) {
				String message = "Incompatible types in ExpressionFunctionAppWithPixel";
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken, message);
			} else {
				expressionFunctionAppWithPixel.type = Type.FLOAT;
			}
		}

		return expressionFunctionAppWithPixel;

	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		expressionPixelConstructor.alpha.visit(this, arg);
		expressionPixelConstructor.blue.visit(this, arg);
		expressionPixelConstructor.green.visit(this, arg);
		expressionPixelConstructor.red.visit(this, arg);

		if (!(expressionPixelConstructor.alpha.type.equals(Type.INTEGER)
				&& expressionPixelConstructor.blue.type.equals(Type.INTEGER)
				&& expressionPixelConstructor.green.type.equals(Type.INTEGER)
				&& expressionPixelConstructor.red.type.equals(Type.INTEGER))) {
			String message = "Incompatible types in ExpressionPixelConstructor";
			throw new SemanticException(expressionPixelConstructor.firstToken, message);
		}
		expressionPixelConstructor.type = Type.INTEGER;
		return expressionPixelConstructor;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		statementAssign.lhs.visit(this, arg);
		statementAssign.e.visit(this, arg);

		if (!(statementAssign.lhs.type.equals(statementAssign.e.type))) {
			String message = "Incompatible types in StatementAssign";
			throw new SemanticException(null, message);
		}

		return statementAssign;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		statementShow.e.visit(this, arg);

		if (!(statementShow.e.type.equals(Type.INTEGER) || statementShow.e.type.equals(Type.BOOLEAN)
				|| statementShow.e.type.equals(Type.FLOAT) || statementShow.e.type.equals(Type.IMAGE))) {
			String message = "Incompatible types in StatementShow";
			throw new SemanticException(null, message);
		}
		return statementShow;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		expressionPixel.pixelSelector.visit(this, arg);
		Declaration pixelDeclaration = symtable.getDeclaration(expressionPixel.name);

		if (pixelDeclaration == null) {
			String message = expressionPixel.name.toString() + " is not declared in the current scope";
			throw new SemanticException(null, message);
		}

		if (!pixelDeclaration.type.equals(Kind.KW_image)) {
			String message = "Incompatible types in ExpressionPixel";
			throw new SemanticException(pixelDeclaration.firstToken, message);
		}
		expressionPixel.type = Type.INTEGER;
		expressionPixel.dec = pixelDeclaration;
		return expressionPixel;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {

		Declaration identDeclaration = symtable.getDeclaration(expressionIdent.name);

		if (identDeclaration == null) {
			String message = expressionIdent.name.toString() + " is not declared in the current scope";
			throw new SemanticException(expressionIdent.firstToken, message);
		}
		expressionIdent.type = Types.getType(identDeclaration.firstToken.kind);
		expressionIdent.dec = identDeclaration;
		return expressionIdent;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		lhsSample.pixelSelector.visit(this, arg);
		Declaration declaration = symtable.getDeclaration(lhsSample.name);

		if (declaration == null) {
			String message = lhsSample.name.toString() + " is not declared in the current scope";
			throw new SemanticException(null, message);
		}

		if (!declaration.firstToken.kind.equals(Kind.KW_image)) {
			String message = "Incompatible types in LHSSample";
			throw new SemanticException(declaration.firstToken, message);
		}
		lhsSample.type = Type.INTEGER;
		lhsSample.dec = declaration;
		return lhsSample;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		lhsPixel.pixelSelector.visit(this, arg);
		Declaration declaration = symtable.getDeclaration(lhsPixel.name);

		if (declaration == null) {
			String message = lhsPixel.name.toString() + " is not declared in the current scope";
			throw new SemanticException(null, message);
		}

		if (!declaration.firstToken.kind.equals(Kind.KW_image)) {
			String message = "Incompatible types in LHSPixel";
			throw new SemanticException(declaration.firstToken, message);
		}

		lhsPixel.type = Type.INTEGER;
		lhsPixel.dec = declaration;
		return lhsPixel;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		Declaration declaration = symtable.getDeclaration(lhsIdent.name);

		if (declaration == null) {
			String message = lhsIdent.name.toString() + " is not declared in the current scope";
			throw new SemanticException(null, message);
		} else {
			lhsIdent.type = Types.getType(declaration.type);
			lhsIdent.dec = declaration;
		}

		return lhsIdent;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		statementIf.guard.visit(this, arg);
		statementIf.b.visit(this, arg);

		if (!statementIf.guard.type.equals(Type.BOOLEAN)) {
			String message = "Incompatible types in StatementIf";
			throw new SemanticException(null, message);
		}

		return statementIf;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		statementWhile.guard.visit(this, arg);
		statementWhile.b.visit(this, arg);

		if (!statementWhile.guard.type.equals(Type.BOOLEAN)) {
			String message = "Incompatible types in StatementWhile";
			throw new SemanticException(null, message);
		}

		return statementWhile;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		statementSleep.duration.visit(this, arg);

		if (!statementSleep.duration.type.equals(Type.INTEGER)) {
			String message = "Incompatible types in StatementSleep";
			throw new SemanticException(null, message);
		}

		return statementSleep;
	}
	
	public void ravi() {
		int x = 10;
		
	}

}