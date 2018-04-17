/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles 
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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp18.Scanner.Kind;
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

public class CodeGenerator implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	static final int Z = 255;

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int slotNumber = 1;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	final int defaultWidth;
	final int defaultHeight;

	Label start = new Label();
	Label end = new Label();

	// final boolean itf = false;
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
	 */
	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName, int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}

	public String getFieldType(Type type) {

		if (type == Type.INTEGER) {
			return "I";

		} else if (type == Type.BOOLEAN) {
			return "Z";

		} else if (type == Type.FLOAT) {
			return "F";

		} else if (type == Type.FILE) {
			return "Ljava/lang/String;";

		} else if (type == Type.IMAGE) {
			return RuntimeImageSupport.ImageDesc;

		} else {
			return "Ljava/lang/String;";
		}

	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		for (ASTNode node : block.decsOrStatements) {
			node.visit(this, null);
		}
		return null;
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		Type type = Types.getType(declaration.type);
		String fieldName = declaration.name;
		declaration.setSlot(slotNumber);
		slotNumber++;
		String fieldType = "";
		if (type == Type.INTEGER || type == Type.FLOAT || type == Type.BOOLEAN || type == Type.FILE) {
			if (type == Type.INTEGER) {
				fieldType = "I";

			} else if (type == Type.FLOAT) {
				fieldType = "F";

			} else if (type == Type.BOOLEAN) {
				fieldType = "Z";

			} else {
				fieldType = "Ljava/lang/String;";
			}

			mv.visitLocalVariable(fieldName, fieldType, null, start, end, declaration.getSlot());

		} else {
			if (declaration.width != null && declaration.height != null) {
				declaration.width.visit(this, arg);
				declaration.height.visit(this, arg);

			} else if (declaration.width == null || declaration.height == null) {
				mv.visitLdcInsn(defaultWidth);
				mv.visitLdcInsn(defaultHeight);

			} else {
				throw new UnsupportedOperationException();
			}

			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage",
					RuntimeImageSupport.makeImageSig, false);
			mv.visitVarInsn(ASTORE, declaration.getSlot());
		}

		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {

		Label startTrue = new Label();
		Label endTrue = new Label();

		if (expressionBinary.leftExpression.type == Type.INTEGER
				&& expressionBinary.rightExpression.type == Type.INTEGER) {
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			switch (expressionBinary.op) {
			case OP_PLUS:
				mv.visitInsn(IADD);
				break;
			case OP_MINUS:
				mv.visitInsn(ISUB);
				break;
			case OP_TIMES:
				mv.visitInsn(IMUL);
				break;
			case OP_DIV:
				mv.visitInsn(IDIV);
				break;
			case OP_AND:
				mv.visitInsn(IAND);
				break;
			case OP_OR:
				mv.visitInsn(IOR);
				break;
			case OP_MOD:
				mv.visitInsn(IREM);
				break;
			case OP_POWER:
				mv.visitInsn(POP);
				mv.visitInsn(POP);
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2I);
				break;
			case OP_EQ:
				mv.visitJumpInsn(IF_ICMPEQ, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_NEQ:
				mv.visitJumpInsn(IF_ICMPNE, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_GT:
				mv.visitJumpInsn(IF_ICMPGT, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_GE:
				mv.visitJumpInsn(IF_ICMPGE, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_LT:
				mv.visitJumpInsn(IF_ICMPLT, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_LE:
				mv.visitJumpInsn(IF_ICMPLE, startTrue);
				mv.visitLdcInsn(false);
				break;
			default:
				throw new UnsupportedOperationException();
			}
			mv.visitJumpInsn(GOTO, endTrue);
			mv.visitLabel(startTrue);
			mv.visitLdcInsn(true);
			mv.visitLabel(endTrue);

		} else if (expressionBinary.leftExpression.type == Type.FLOAT
				|| expressionBinary.rightExpression.type == Type.FLOAT) {
			expressionBinary.leftExpression.visit(this, arg);
			if (expressionBinary.leftExpression.type == Type.INTEGER) {
				mv.visitInsn(I2F);
			}
			expressionBinary.rightExpression.visit(this, arg);
			if (expressionBinary.rightExpression.type == Type.INTEGER) {
				mv.visitInsn(I2F);
			}
			switch (expressionBinary.op) {
			case OP_PLUS:
				mv.visitInsn(FADD);
				break;
			case OP_MINUS:
				mv.visitInsn(FSUB);
				break;
			case OP_TIMES:
				mv.visitInsn(FMUL);
				break;
			case OP_DIV:
				mv.visitInsn(FDIV);
				break;
			case OP_POWER:
				mv.visitInsn(POP);
				mv.visitInsn(POP);
				expressionBinary.leftExpression.visit(this, arg);
				if (expressionBinary.leftExpression.type == Type.INTEGER)
					mv.visitInsn(I2D);
				else if (expressionBinary.leftExpression.type == Type.FLOAT)
					mv.visitInsn(F2D);
				expressionBinary.rightExpression.visit(this, arg);
				if (expressionBinary.rightExpression.type == Type.INTEGER)
					mv.visitInsn(I2D);
				else if (expressionBinary.rightExpression.type == Type.FLOAT)
					mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
				break;
			case OP_EQ:
				mv.visitInsn(FCMPL);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(IF_ICMPEQ, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_NEQ:
				mv.visitInsn(FCMPL);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(IF_ICMPNE, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_GT:
				mv.visitInsn(FCMPL);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(IF_ICMPEQ, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_GE:
				mv.visitInsn(FCMPL);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(IF_ICMPGE, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_LT:
				mv.visitInsn(FCMPL);
				mv.visitInsn(ICONST_1);
				mv.visitInsn(INEG);
				mv.visitJumpInsn(IF_ICMPEQ, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_LE:
				mv.visitInsn(FCMPL);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(IF_ICMPLE, startTrue);
				mv.visitLdcInsn(false);
				break;
			default:
				throw new UnsupportedOperationException();
			}
			mv.visitJumpInsn(GOTO, endTrue);
			mv.visitLabel(startTrue);
			mv.visitLdcInsn(true);
			mv.visitLabel(endTrue);

		} else if (expressionBinary.leftExpression.type == Type.BOOLEAN
				&& expressionBinary.rightExpression.type == Type.BOOLEAN) {
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			switch (expressionBinary.op) {
			case OP_AND:
				mv.visitInsn(IAND);
				break;
			case OP_OR:
				mv.visitInsn(IOR);
				break;
			case OP_EQ:
				mv.visitJumpInsn(IF_ICMPEQ, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_NEQ:
				mv.visitJumpInsn(IF_ICMPNE, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_GT:
				mv.visitJumpInsn(IF_ICMPGT, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_GE:
				mv.visitJumpInsn(IF_ICMPGE, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_LT:
				mv.visitJumpInsn(IF_ICMPLT, startTrue);
				mv.visitLdcInsn(false);
				break;
			case OP_LE:
				mv.visitJumpInsn(IF_ICMPLE, startTrue);
				mv.visitLdcInsn(false);
				break;
			default:
				throw new UnsupportedOperationException();
			}
			mv.visitJumpInsn(GOTO, endTrue);
			mv.visitLabel(startTrue);
			mv.visitLdcInsn(true);
			mv.visitLabel(endTrue);
		}
		/*
		 * mv.visitJumpInsn(GOTO, endLabel); mv.visitLabel(startLabel);
		 * mv.visitLdcInsn(true); mv.visitLabel(endLabel);
		 */

		return null;

	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		Expression conditionexp = expressionConditional.guard;
		Expression trueexp = expressionConditional.trueExpression;
		Expression falseexp = expressionConditional.falseExpression;

		Label startLabel = new Label();
		Label endLabel = new Label();

		conditionexp.visit(this, arg);
		mv.visitLdcInsn(true);
		mv.visitJumpInsn(IF_ICMPEQ, startLabel);
		falseexp.visit(this, arg);
		mv.visitJumpInsn(GOTO, endLabel);
		mv.visitLabel(startLabel);
		trueexp.visit(this, arg);
		mv.visitLabel(endLabel);

		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {

		expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		Kind expKind = expressionFunctionAppWithExpressionArg.function;
		Type type = expressionFunctionAppWithExpressionArg.e.type;

		if (expKind == Kind.KW_abs) {

			if (type == Type.INTEGER) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(I)I", false);
			} else if (type == Type.FLOAT) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);
			} else {
				throw new UnsupportedOperationException();
			}

		} else if (expKind == Kind.KW_int && type == Type.FLOAT) {
			mv.visitInsn(F2I);

		} else if (expKind == Kind.KW_int && type == Type.INTEGER) {
			// do nothing

		} else if (expKind == Kind.KW_float && type == Type.INTEGER) {
			mv.visitInsn(I2F);

		} else if (expKind == Kind.KW_float && type == Type.FLOAT) {
			// do nothing

		} else if (expKind == Kind.KW_alpha) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.JVMClassName, "getAlpha", RuntimePixelOps.getAlphaSig,
					false);

		} else if (expKind == Kind.KW_red) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.JVMClassName, "getRed", RuntimePixelOps.getRedSig, false);

		} else if (expKind == Kind.KW_green) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.JVMClassName, "getGreen", RuntimePixelOps.getGreenSig,
					false);

		} else if (expKind == Kind.KW_blue) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.JVMClassName, "getBlue", RuntimePixelOps.getBlueSig,
					false);

		} else if (expKind == Kind.KW_width) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getWidth", RuntimeImageSupport.getWidthSig,
					false);

		} else if (expKind == Kind.KW_height) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getHeight",
					RuntimeImageSupport.getHeightSig, false);

		} else {
			if (type == Type.FLOAT)
				mv.visitInsn(F2D);

			if (expKind == Kind.KW_sin) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);

			} else if (expKind == Kind.KW_cos) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);

			} else if (expKind == Kind.KW_atan) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan", "(D)D", false);

			} else if (expKind == Kind.KW_log) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "log", "(D)D", false);

			} else {
				throw new UnsupportedOperationException();
			}
			mv.visitInsn(D2F);
		}
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {

		// No need to use Math.round as it is not mentioned in the specification. For
		// our unit test cases, rounding should not be an issue

		if (expressionFunctionAppWithPixel.name == Kind.KW_cart_x
				|| expressionFunctionAppWithPixel.name == Kind.KW_cart_y) {
			expressionFunctionAppWithPixel.e1.visit(this, arg);

			if (expressionFunctionAppWithPixel.e1.type == Type.FLOAT)
				mv.visitInsn(F2D);

			if (expressionFunctionAppWithPixel.name == Kind.KW_cart_x) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);

			} else {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			}

			expressionFunctionAppWithPixel.e0.visit(this, arg);

			if (expressionFunctionAppWithPixel.e0.type == Type.FLOAT)
				mv.visitInsn(F2D);

			mv.visitInsn(DMUL);
			mv.visitInsn(D2I);

		} else if (expressionFunctionAppWithPixel.name == Kind.KW_polar_a
				|| expressionFunctionAppWithPixel.name == Kind.KW_polar_r) {

			if (expressionFunctionAppWithPixel.name == Kind.KW_polar_a) {
				expressionFunctionAppWithPixel.e1.visit(this, arg);
				mv.visitInsn(I2D);
				expressionFunctionAppWithPixel.e0.visit(this, arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan2", "(DD)D", false);
				mv.visitInsn(D2F);
			} else {
				expressionFunctionAppWithPixel.e0.visit(this, arg);
				mv.visitInsn(I2D);
				expressionFunctionAppWithPixel.e1.visit(this, arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "hypot", "(DD)D", false);
				mv.visitInsn(D2F);
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {

		Type type = expressionIdent.type;
		if (type == Type.INTEGER || type == Type.BOOLEAN) {
			mv.visitVarInsn(ILOAD, expressionIdent.dec.getSlot());
		} else if (type == Type.FLOAT) {
			mv.visitVarInsn(FLOAD, expressionIdent.dec.getSlot());
		} else if (type == Type.FILE || type == Type.IMAGE) {
			mv.visitVarInsn(ALOAD, expressionIdent.dec.getSlot());
		} else {
		}
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		mv.visitVarInsn(ALOAD, expressionPixel.dec.getSlot());
		expressionPixel.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getPixel", RuntimeImageSupport.getPixelSig,
				false);
		return null;
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		expressionPixelConstructor.alpha.visit(this, arg);
		expressionPixelConstructor.red.visit(this, arg);
		expressionPixelConstructor.green.visit(this, arg);
		expressionPixelConstructor.blue.visit(this, arg);

		mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "makePixel", RuntimePixelOps.makePixelSig, false);
		return null;
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		Kind token = expressionPredefinedName.firstToken.kind;
		if (token == Kind.KW_default_width) {
			mv.visitLdcInsn(defaultWidth);
		} else if (token == Kind.KW_default_height) {
			mv.visitLdcInsn(defaultHeight);
		} else if (token == Kind.KW_Z) {
			mv.visitLdcInsn(Z);
		} else {
			throw new UnsupportedOperationException();
		}
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		Kind kind = expressionUnary.op;
		expressionUnary.expression.visit(this, arg);

		Label startLabel = new Label();
		Label endLabel = new Label();
		if (kind == Kind.OP_PLUS) {
			// do nothing
		} else if (kind == Kind.OP_MINUS) {
			if (expressionUnary.type == Type.INTEGER) {
				mv.visitInsn(INEG);
			} else if (expressionUnary.type == Type.FLOAT) {
				mv.visitInsn(FNEG);
			}

		} else if (kind == Kind.OP_EXCLAMATION) {
			if (expressionUnary.expression.type == Type.INTEGER) {
				mv.visitLdcInsn(-1);
				mv.visitInsn(IXOR);

			} else if (expressionUnary.expression.type == Type.BOOLEAN) {
				mv.visitJumpInsn(IFEQ, startLabel);
				mv.visitLdcInsn(new Integer(0));
			} else {
				throw new UnsupportedOperationException();
			}
		} else {
			throw new UnsupportedOperationException();
		}

		mv.visitJumpInsn(GOTO, endLabel);
		mv.visitLabel(startLabel);
		mv.visitLdcInsn(new Integer(1));
		mv.visitLabel(endLabel);
		return null;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {

		if (lhsIdent.type == Type.INTEGER || lhsIdent.type == Type.BOOLEAN || lhsIdent.type == Type.FLOAT
				|| lhsIdent.type == Type.FILE) {
			if (lhsIdent.type == Type.INTEGER || lhsIdent.type == Type.BOOLEAN) {
				mv.visitVarInsn(ISTORE, lhsIdent.dec.getSlot());
			} else if (lhsIdent.type == Type.FLOAT) {
				mv.visitVarInsn(FSTORE, lhsIdent.dec.getSlot());
			}

		} else if (lhsIdent.type == Type.IMAGE) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "deepCopy", RuntimeImageSupport.deepCopySig,
					false);
			mv.visitVarInsn(ASTORE, lhsIdent.dec.getSlot());

		} else {
			throw new UnsupportedOperationException();
		}
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {

		mv.visitVarInsn(ALOAD, lhsPixel.dec.getSlot());
		lhsPixel.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "setPixel", RuntimeImageSupport.setPixelSig,
				false);
		return null;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {

		mv.visitVarInsn(ALOAD, lhsSample.dec.getSlot());
		lhsSample.pixelSelector.visit(this, arg);
		if (lhsSample.color == Kind.KW_alpha)
			mv.visitInsn(ICONST_0);
		if (lhsSample.color == Kind.KW_red)
			mv.visitInsn(ICONST_1);
		if (lhsSample.color == Kind.KW_green)
			mv.visitInsn(ICONST_2);
		if (lhsSample.color == Kind.KW_blue)
			mv.visitInsn(ICONST_3);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "updatePixelColor",
				RuntimeImageSupport.updatePixelColorSig, false);
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		if (pixelSelector.ex.type == Type.INTEGER) {
			pixelSelector.ex.visit(this, arg);
			pixelSelector.ey.visit(this, arg);

		} else {
			// If the pixel values are float, they are polar coordinates. You can coerce
			// them to int using the cart_x and cart_y functions
			pixelSelector.ey.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			pixelSelector.ex.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitInsn(DMUL);
			mv.visitInsn(D2I);

			pixelSelector.ey.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			pixelSelector.ex.visit(this, arg);
			mv.visitInsn(F2D);
			mv.visitInsn(DMUL);
			mv.visitInsn(D2I);

		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
		// it is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You probably
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.
		className = program.progName;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);

		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

		// add label before first instruction
		Label mainStart = new Label();
		start = mainStart;
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		end = mainEnd;
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		statementAssign.e.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		Label insideBlock = new Label();
		Label startIf = new Label();
		Label endIf = new Label();
		Label endAfterIf = new Label();

		statementIf.guard.visit(this, arg);
		mv.visitJumpInsn(IFEQ, insideBlock);
		mv.visitLabel(startIf);
		statementIf.b.visit(this, arg);
		mv.visitLabel(endIf);
		mv.visitLabel(insideBlock);
		mv.visitLabel(endAfterIf);

		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		Kind kind = statementInput.dec.type;
		mv.visitVarInsn(ALOAD, 0);
		statementInput.e.visit(this, arg);
		mv.visitInsn(AALOAD);
		switch (kind) {
		case KW_int: {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitVarInsn(ISTORE, statementInput.dec.getSlot());
		}
			break;
		case KW_float: {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", false);
			mv.visitVarInsn(FSTORE, statementInput.dec.getSlot());
		}
			break;
		case KW_boolean: {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitVarInsn(ISTORE, statementInput.dec.getSlot());
		}
			break;
		case KW_filename: {
			mv.visitVarInsn(ASTORE, statementInput.dec.getSlot());
		}
			break;
		case KW_image: {
			if (statementInput.dec.width != null && statementInput.dec.height != null) {
				statementInput.dec.width.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				statementInput.dec.height.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);

			} else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "readImage",
					RuntimeImageSupport.readImageSig, false);
			mv.visitVarInsn(ASTORE, statementInput.dec.getSlot());
		}
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * For integers, booleans, and floats, generate code to print to console. For
		 * images, generate code to display in a frame.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.getType();
		switch (type) {
		case INTEGER: {
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
		}
			break;
		case BOOLEAN: {
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
		}
			break;
		case FLOAT: {
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(F)V", false);
		}
			break;
		case FILE: {
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String)V", false);
		}
			break;
		case IMAGE: {
			CodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeFrame",
					RuntimeImageSupport.makeFrameSig, false);
		}
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		statementSleep.duration.visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		Label whileCondition = new Label();
		Label whileBody = new Label();
		Label endWhileBody = new Label();
		Label endWhileCondition = new Label();

		// check condition
		mv.visitJumpInsn(GOTO, whileCondition);
		mv.visitLabel(whileBody);
		statementWhile.b.visit(this, arg);
		mv.visitLabel(endWhileBody);
		mv.visitLabel(whileCondition);
		statementWhile.guard.visit(this, arg);
		mv.visitLabel(endWhileCondition);
		mv.visitJumpInsn(IFNE, whileBody);

		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		mv.visitVarInsn(ALOAD, statementWrite.sourceDec.getSlot());
		mv.visitVarInsn(ALOAD, statementWrite.declDec.getSlot());
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "write", RuntimeImageSupport.writeSig, false);

		return null;
	}

}
