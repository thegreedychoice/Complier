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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.FieldVisitor;

import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
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

import cop5556sp18.CodeGenUtils;

//new imports

import cop5556sp18.Scanner.Kind;


public class CodeGenerator implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	static final int Z = 255;
	
	static int slot_no = 1;

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	final int defaultWidth;
	final int defaultHeight;
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
	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
			int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
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
	public Object visitBooleanLiteral(
			ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return expressionBooleanLiteral;
		//return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		//add a local variable with declaration's name and type to the class
		String dec_name = declaration.name + declaration.getSlotNo();
		//String dec_name = declaration.name;
		Type dec_type = declaration.getType();
		String dec_desc = getASMType(dec_type);
		
		
		FieldVisitor fv = null;
		
		//set slot no for the declaration object
		//declaration.setSlotNo(slot_no);
		//slot_no++;
		
		//How do I add a local variable?
		if(dec_type == Type.INTEGER) {
			fv = cw.visitField(ACC_STATIC, dec_name, "I", null, new Integer(0));
			fv.visitEnd();
		}
		else if(dec_type == Type.BOOLEAN) {
			fv = cw.visitField(ACC_STATIC, dec_name, "Z", null, new Boolean(false));
			fv.visitEnd();
		}
		else if(dec_type == Type.FLOAT) {
			fv = cw.visitField(ACC_STATIC, dec_name, "F", null, new Float(0));
			fv.visitEnd();
		}
		else if(dec_type == Type.FILE) {
			fv = cw.visitField(ACC_STATIC, dec_name, "Ljava/lang/String;", null, null);
			fv.visitEnd();
		}
		if(dec_type == Type.IMAGE) {
						
			if(declaration.width != null && declaration.height != null) {
				
				//visit and evaluate the expressions
				declaration.width.visit(this, arg);
				declaration.height.visit(this, arg);
				
				//put their value on the stack
				//int width_val = declaration.width.firstToken.intVal();
				//int height_val = declaration.height.firstToken.intVal();
				
				//mv.visitIntInsn(Opcodes.BIPUSH, width_val);
				//mv.visitIntInsn(Opcodes.BIPUSH, height_val);
				
				
			}
			
			//if E0 and E1 are null
			if(declaration.width == null && declaration.height == null) {
				mv.visitLdcInsn(defaultWidth);
				mv.visitLdcInsn(defaultHeight);			
			}
			
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage", "(II)" + RuntimeImageSupport.ImageDesc, false);
			mv.visitVarInsn(ASTORE, declaration.getSlotNo());
		}
		
		//create start label
		//Label dec_start = new Label();
		//mv.visitLabel(dec_start);

		return declaration;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		Kind operator = expressionBinary.op;
		
		switch(operator) {
		case OP_POWER : {
			
			//System.out.println("inside Power");
			expressionBinary.leftExpression.visit(this, arg);
			Type left_type = expressionBinary.leftExpression.getType();
			if(left_type == Type.INTEGER)
				mv.visitInsn(I2D);
			else if(left_type == Type.FLOAT)
				mv.visitInsn(F2D);
			
			expressionBinary.rightExpression.visit(this, arg);
			Type right_type = expressionBinary.rightExpression.getType();
			if(right_type == Type.INTEGER)
				mv.visitInsn(I2D);
			else if(right_type == Type.FLOAT)
				mv.visitInsn(F2D);
			
			//evaluate the expression
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
			//System.out.println("After method invocation");
			
			if(left_type == Type.FLOAT || right_type == Type.FLOAT)
				mv.visitInsn(D2F);
			else
				mv.visitInsn(D2I);
		}
			break;
		case OP_PLUS : {
			expressionBinary.leftExpression.visit(this, arg);
			Type left_type = expressionBinary.leftExpression.getType();
			if(left_type == Type.INTEGER)
				mv.visitInsn(I2F);
			
			expressionBinary.rightExpression.visit(this, arg);
			Type right_type = expressionBinary.rightExpression.getType();
			if(right_type == Type.INTEGER)
				mv.visitInsn(I2F);
			
			mv.visitInsn(FADD);
			if(left_type == Type.INTEGER && right_type == Type.INTEGER)
				mv.visitInsn(F2I);
		}
			break;
		case OP_MINUS : {
			expressionBinary.leftExpression.visit(this, arg);
			Type left_type = expressionBinary.leftExpression.getType();
			if(left_type == Type.INTEGER)
				mv.visitInsn(I2F);
			
			expressionBinary.rightExpression.visit(this, arg);
			Type right_type = expressionBinary.rightExpression.getType();
			if(right_type == Type.INTEGER)
				mv.visitInsn(I2F);
			
			mv.visitInsn(FSUB);
			if(left_type == Type.INTEGER && right_type == Type.INTEGER)
				mv.visitInsn(F2I);
		}
			break;
		case OP_TIMES : {
			expressionBinary.leftExpression.visit(this, arg);
			Type left_type = expressionBinary.leftExpression.getType();
			if(left_type == Type.INTEGER)
				mv.visitInsn(I2F);
			
			expressionBinary.rightExpression.visit(this, arg);
			Type right_type = expressionBinary.rightExpression.getType();
			if(right_type == Type.INTEGER)
				mv.visitInsn(I2F);
			
			mv.visitInsn(FMUL);
			if(left_type == Type.INTEGER && right_type == Type.INTEGER)
				mv.visitInsn(F2I);
		}
			
			break;
		case OP_DIV : {
			expressionBinary.leftExpression.visit(this, arg);
			Type left_type = expressionBinary.leftExpression.getType();
			if(left_type == Type.INTEGER)
				mv.visitInsn(I2F);
			
			expressionBinary.rightExpression.visit(this, arg);
			Type right_type = expressionBinary.rightExpression.getType();
			if(right_type == Type.INTEGER)
				mv.visitInsn(I2F);
			
			mv.visitInsn(FDIV);
			if(left_type == Type.INTEGER && right_type == Type.INTEGER)
				mv.visitInsn(F2I);
		}	
			break;
		case OP_MOD : {
			expressionBinary.leftExpression.visit(this, arg);
			Type left_type = expressionBinary.leftExpression.getType();
			if(left_type == Type.INTEGER) {
				System.out.println("Inside Mod Left!");
				mv.visitInsn(I2F);
			}
			expressionBinary.rightExpression.visit(this, arg);
			Type right_type = expressionBinary.rightExpression.getType();
			if(right_type == Type.INTEGER) {
				System.out.println("Inside Mod Right!");
				mv.visitInsn(I2F);
			}
			
			mv.visitInsn(FREM);
			if(left_type == Type.INTEGER && right_type == Type.INTEGER)
				mv.visitInsn(F2I);
		}			
			break;
		case OP_AND : {
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			mv.visitInsn(IAND);
		}		
			break;
		case OP_OR : {
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			mv.visitInsn(IOR);
		}
			
			break;
		case OP_LE : {
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			Type left_type = expressionBinary.leftExpression.getType();
			
			Label labelStart = new Label();
			Label labelEnd = new Label();
			
			if(left_type == Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFEQ, labelStart);
			}
			else {
				mv.visitJumpInsn(IF_ICMPLE, labelStart);
			}
			
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(labelEnd);
		}
			
			break;
		case OP_LT : {
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			
			Type left_type = expressionBinary.leftExpression.getType();
			
			Label labelStart = new Label();
			Label labelEnd = new Label();
			
			if(left_type == Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFEQ, labelStart);
			}
			else {
				mv.visitJumpInsn(IF_ICMPLT, labelStart);
			}
			
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(labelEnd);
		}
			
			break;
		case OP_EQ : {
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			
			Type left_type = expressionBinary.leftExpression.getType();
			
			Label labelStart = new Label();
			Label labelEnd = new Label();
			
			if(left_type == Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFEQ, labelStart);
			}
			else {
				mv.visitJumpInsn(IF_ICMPEQ, labelStart);
			}
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(labelEnd);
		}
			
			break;
		case OP_GE : {
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			
			Type left_type = expressionBinary.leftExpression.getType();
			
			Label labelStart = new Label();
			Label labelEnd = new Label();
			
			if(left_type == Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFEQ, labelStart);
			}
			else {
				mv.visitJumpInsn(IF_ICMPGE, labelStart);
			}
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(labelEnd);
		}
			
			break;
		case OP_GT : {
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			
			Type left_type = expressionBinary.leftExpression.getType();
			
			Label labelStart = new Label();
			Label labelEnd = new Label();
			
			if(left_type == Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFEQ, labelStart);
			}
			else {
				mv.visitJumpInsn(IF_ICMPGT, labelStart);
			}
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(labelEnd);
		}
			
			break;
		case OP_NEQ : {
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			
			Type left_type = expressionBinary.leftExpression.getType();
			
			Label labelStart = new Label();
			Label labelEnd = new Label();
			
			if(left_type == Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFEQ, labelStart);
			}
			else {
				mv.visitJumpInsn(IF_ICMPNE, labelStart);
			}
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, labelEnd);
			mv.visitLabel(labelStart);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(labelEnd);
		}
			
			break;
		}
		
		
		return expressionBinary;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionConditional(
			ExpressionConditional expressionConditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionConditional.guard.visit(this, arg);
		Label labelStart = new Label();
		Label labelEnd = new Label();
		
		mv.visitJumpInsn(IFEQ, labelStart);
		expressionConditional.trueExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, labelEnd);
		mv.visitLabel(labelStart);
		expressionConditional.falseExpression.visit(this, arg);
		mv.visitLabel(labelEnd);
		
		return expressionConditional;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFloatLiteral(
			ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return expressionFloatLiteral;
	}

	/*
	 * (non-Javadoc)
	 * @see cop5556sp18.AST.ASTVisitor#visitExpressionFunctionAppWithExpressionArg(cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg, java.lang.Object)
	 * ExpressionFunctionAppWithExpressionArg ::=  FunctionName Expression
	 */
	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		//For sin, cos, atan, log, abs:  use functions in java.lang.Math.

		//Some of these functions expect a double argument and return a double value, 
		//so you will need to cast to and from float.

		//You may find it easier to write a wrapper routine in Java to do this and 
		//invoke your function instead of invoking the java.lang.Math function directly.
		//function int converts a float to an int (JVM instruction F2I) or does nothing if the type is already int.
		//function float converts an int to float(JVM instruction I2F) or does nothing if the type is already float.
		
		//System.out.println("inside visitExpressionFunctionAppWithExpressionArg");
		
		expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		Type exp_type = expressionFunctionAppWithExpressionArg.e.getType();
		Kind fun_type = expressionFunctionAppWithExpressionArg.function;
		
		//System.out.println("expressionFunctionAppWithExpressionArg.e");
		//System.out.println(expressionFunctionAppWithExpressionArg.e);
		
		//System.out.println("expressionFunctionAppWithExpressionArg.e.type()");
		//System.out.println(exp_type);
		
		//System.out.println("expressionFunctionAppWithExpressionArg.function");
		//System.out.println(expressionFunctionAppWithExpressionArg.function);
		
		switch(fun_type) 
		{
			case KW_abs: {
				if(exp_type == Type.INTEGER) 
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(I)I", false);
				else if(exp_type == Type.FLOAT) 
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);
				else 
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(D)D", false);
			}
			break;
			case KW_log: {
				invokeMathDoubleFunction("log");
			}
			break;
			case KW_sin: {
				invokeMathDoubleFunction("sin");
			}
			break;
			case KW_cos: {
				invokeMathDoubleFunction("cos");
			}
			break;
			case KW_atan: {
				invokeMathDoubleFunction("atan");
			}
			break;
			case KW_int: {
				if(exp_type == Type.FLOAT)
					mv.visitInsn(F2I);
			}
			break;
			case KW_float: {
				if(exp_type == Type.INTEGER)
					mv.visitInsn(I2F);
			}
			break;
			case KW_width: {
				mv.visitMethodInsn(INVOKESTATIC,RuntimeImageSupport.className,"getWidth",RuntimeImageSupport.getWidthSig,false);
			}
			break;
			case KW_height: {
				mv.visitMethodInsn(INVOKESTATIC,RuntimeImageSupport.className,"getHeight",RuntimeImageSupport.getHeightSig,false);
			}
			break;
			case KW_red: {
				mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getRed", "(I)I", false);
			}
			break;
			case KW_green: {
				mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getGreen", "(I)I", false);
			}
			break;
			case KW_blue: {
				mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getBlue", "(I)I", false);			
				}
			break;
			case KW_alpha: {
				System.out.println("Inside KW_alpha");
				mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getAlpha", "(I)I", false);			
				System.out.println("Outside KW_alpha");
			}
			break;
		}
		
		return expressionFunctionAppWithExpressionArg;
		//throw new UnsupportedOperationException();
	}
	
	public void invokeMathDoubleFunction(String fnName) {
		mv.visitInsn(F2D);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", fnName, "(D)D", false);
		mv.visitInsn(D2F);
	}

	/*
	 * (non-Javadoc)
	 * @see cop5556sp18.AST.ASTVisitor#visitExpressionFunctionAppWithPixel(cop5556sp18.AST.ExpressionFunctionAppWithPixel, java.lang.Object)
	 * ExpressionFunctionAppWithPixel ::= FunctionName Expression0 Expression1
	 */
	@Override
	public Object visitExpressionFunctionAppWithPixel(
			ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Kind fn_name = expressionFunctionAppWithPixel.name;
		
		if(fn_name == Kind.KW_cart_x || fn_name == Kind.KW_cart_y) {
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			
			if(fn_name == Kind.KW_cart_x) {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
				mv.visitInsn(D2F);
				mv.visitInsn(FMUL);
				mv.visitInsn(F2I);
			}
			else {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
				mv.visitInsn(D2F);
				mv.visitInsn(FMUL);
				mv.visitInsn(F2I);
			}
		}
		else if(fn_name == Kind.KW_polar_r || fn_name == Kind.KW_polar_a) {
			
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitInsn(I2D);
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitInsn(I2D);
			
			if(fn_name == Kind.KW_polar_a) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan2", "(DD)D", false);
				mv.visitInsn(D2F);
			}
			else {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "hypot", "(DD)D", false);
				mv.visitInsn(D2F);
			}
		}
		
		return expressionFunctionAppWithPixel;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Declaration dec = expressionIdent.dec;
		Type exp_type = expressionIdent.getType();
		
		//load the expression value on the stack
		if(exp_type == Type.INTEGER) {
			mv.visitVarInsn(ILOAD, dec.getSlotNo());
		}
		else if(exp_type == Type.FLOAT) {
			mv.visitVarInsn(FLOAD, dec.getSlotNo());
		}
		else if(exp_type == Type.BOOLEAN){
			mv.visitVarInsn(ILOAD, dec.getSlotNo());
		}
		else if(exp_type == Type.FILE){
			mv.visitVarInsn(ALOAD, dec.getSlotNo());
		}
		else if(exp_type == Type.IMAGE){
			mv.visitVarInsn(ALOAD, dec.getSlotNo());
		}
		
		return expressionIdent;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionIntegerLiteral(
			ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// This one is all done!
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return expressionIntegerLiteral;
	}
	
	/*
	 * (non-Javadoc)
	 * @see cop5556sp18.AST.ASTVisitor#visitExpressionPixel(cop5556sp18.AST.ExpressionPixel, java.lang.Object)
	 * ExpressionPixel ::= IDENTIFIER PixelSelector
	 */
	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Declaration dec = expressionPixel.dec;
		mv.visitVarInsn(ALOAD, dec.getSlotNo());
		expressionPixel.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getPixel", RuntimeImageSupport.getPixelSig, false);
		return expressionPixel;
		
		//throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see cop5556sp18.AST.ASTVisitor#visitExpressionPixelConstructor(cop5556sp18.AST.ExpressionPixelConstructor, java.lang.Object)
	 * ExpressionPixelConstructor ::= Expressionalpha Expressionred Expressiongreen Expressionblue
	 */
	@Override
	public Object visitExpressionPixelConstructor(
			ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionPixelConstructor.alpha.visit(this, arg);
		expressionPixelConstructor.red.visit(this, arg);
		expressionPixelConstructor.green.visit(this, arg);
		expressionPixelConstructor.blue.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "makePixel", RuntimePixelOps.makePixelSig, false);
		return expressionPixelConstructor;
		
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionPredefinedName(
			ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//Z = 255.
		//default_width and default_height get their values from parameters 
		//passed to the CodeGenerator constructor.
		if(expressionPredefinedName.name == Kind.KW_Z) {
			mv.visitLdcInsn(Z);
		}
		else if(expressionPredefinedName.name == Kind.KW_default_width) {
			mv.visitLdcInsn(defaultWidth);
		}
		else if(expressionPredefinedName.name == Kind.KW_default_height) {
			mv.visitLdcInsn(defaultHeight);
		}
		
		return expressionPredefinedName;
		//throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see cop5556sp18.AST.ASTVisitor#visitExpressionUnary(cop5556sp18.AST.ExpressionUnary, java.lang.Object)
	 * ExpressionUnary ::= Op Expression
	 */
	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		//Implement all of the unary operators in our language.
		//Operator ! applied to an integer, should flip all the bits, including the sign bit.
		
		expressionUnary.expression.visit(this, arg);
		Kind operator = expressionUnary.op;
		
		if(operator == Kind.OP_MINUS) {
			if(expressionUnary.getType() == Type.INTEGER) 
				mv.visitInsn(INEG);
			else if(expressionUnary.getType() == Type.FLOAT) 
				mv.visitInsn(FNEG);
			
		}
		else if (operator == Kind.OP_EXCLAMATION) {
			if(expressionUnary.getType() == Type.INTEGER) 
				{
					mv.visitInsn(ICONST_M1);
					mv.visitInsn(IXOR);
				}
			else if(expressionUnary.getType() == Type.BOOLEAN) {
				Label labelStart = new Label();
				Label labelEnd = new Label();
				
				mv.visitJumpInsn(IFEQ, labelStart);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, labelEnd);
				mv.visitLabel(labelStart);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(labelEnd);
			}
		}
		
		return expressionUnary;
		//throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see cop5556sp18.AST.ASTVisitor#visitLHSIdent(cop5556sp18.AST.LHSIdent, java.lang.Object)
	 * LHSIdent ::= IDENTIFIER
	 */
	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		Declaration dec = lhsIdent.dec;
		Type lhs_type = lhsIdent.getType();
		
		//Generate code to store the value already on top of the stack in the corresponding variable
		if(lhs_type == Type.INTEGER) {
			mv.visitVarInsn(ISTORE, dec.getSlotNo());
		}
		else if(lhs_type == Type.BOOLEAN) {
			mv.visitVarInsn(ISTORE, dec.getSlotNo());
		}
		else if(lhs_type == Type.FLOAT) {
			mv.visitVarInsn(FSTORE, dec.getSlotNo());
		}
		else if(lhs_type == Type.FILE) {			
			mv.visitVarInsn(ASTORE, dec.getSlotNo());
		}
		else if(lhs_type == Type.IMAGE) {

			// If the type is image, the value on top of the stack is actually a reference.  
			//Instead of copying the reference, a copy of the image should be created and 
			//the reference to the copy stored.  Use RuntimeImageSupport.deepCopy to copy the image.
			
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "deepCopy",RuntimeImageSupport.deepCopySig, false);
			mv.visitVarInsn(ASTORE, dec.getSlotNo());
		}


		return lhsIdent;
		//throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see cop5556sp18.AST.ASTVisitor#visitLHSPixel(cop5556sp18.AST.LHSPixel, java.lang.Object)
	 * LHSPixel ::= IDENTIFIER PixelSelector
	 */
	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("Inside visitLHSPixel");
		Declaration dec = lhsPixel.dec;
		//mv.visitVarInsn(ASTORE, dec.getSlotNo());
		mv.visitVarInsn(ALOAD, dec.getSlotNo());
		lhsPixel.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "setPixel", RuntimeImageSupport.setPixelSig, false);
		//System.out.println("Outside visitLHSPixel");
		return lhsPixel;
		
		//throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see cop5556sp18.AST.ASTVisitor#visitLHSSample(cop5556sp18.AST.LHSSample, java.lang.Object)
	 * LHSSample ::= IDENTIFIER PixelSelector Color
	 */
	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Declaration dec = lhsSample.dec;
		//mv.visitVarInsn(ISTORE, dec.getSlotNo());
		mv.visitVarInsn(ALOAD, dec.getSlotNo());
		
		Kind color = lhsSample.color;
		lhsSample.pixelSelector.visit(this, arg);
		
		switch(color) 
		{
			case KW_alpha: {
				mv.visitLdcInsn(RuntimePixelOps.ALPHA);
			}
			break;
			case KW_red: {
				mv.visitLdcInsn(RuntimePixelOps.RED);
			}
			break;
			case KW_green: {
				mv.visitLdcInsn(RuntimePixelOps.GREEN);
			}
			break;
			case KW_blue: {
				mv.visitLdcInsn(RuntimePixelOps.BLUE);
			}
			break;
		}
		
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "updatePixelColor", RuntimeImageSupport.updatePixelColorSig, false);
		return lhsSample;
		
		//throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * @see cop5556sp18.AST.ASTVisitor#visitPixelSelector(cop5556sp18.AST.PixelSelector, java.lang.Object)
	 * PixelSelector ::= Expression0 Expression1
	 */
	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		pixelSelector.ex.visit(this, arg);
		pixelSelector.ey.visit(this, arg);
		return pixelSelector;
		
		//throw new UnsupportedOperationException();
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
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,
				"java/lang/Object", null);
		cw.visitSource(sourceFileName, null);

		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart,
				mainEnd, 0);
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

	/*
	 * (non-Javadoc)
	 * @see cop5556sp18.AST.ASTVisitor#visitStatementAssign(cop5556sp18.AST.StatementAssign, java.lang.Object)
	 * 
	 * StatementAssign ::= LHS Expression
	 */
	@Override
	public Object visitStatementAssign(StatementAssign statementAssign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		System.out.println();
		System.out.println("Inside Statement Assign!");
		
		//Visit the Expression to generate code to leave the expressions value on top of the stack.
		statementAssign.e.visit(this, arg);
		
		System.out.println("statementAssign.e");
		System.out.println(statementAssign.e);
		System.out.println();
		//Visit LHS to generate code to store the top of the stack into the indicated variable.
		statementAssign.lhs.visit(this, arg);
		
		System.out.println("statementAssign.lhs");
		System.out.println(statementAssign.lhs);
		
		return statementAssign;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Label labelStart = new Label();
		Label labelEnd = new Label();
		
		mv.visitLabel(labelStart);
		statementIf.guard.visit(this, arg);
		mv.visitJumpInsn(IFEQ, labelEnd);
		statementIf.b.visit(this, arg);
		mv.visitLabel(labelEnd);
		
		return statementIf;
		//throw new UnsupportedOperationException();
	}
	/*
	 * (non-Javadoc)
	 * @see cop5556sp18.AST.ASTVisitor#visitStatementInput(cop5556sp18.AST.StatementInput, java.lang.Object)
	 * StatementInput ::= IDENTIFIER Expression
	 */
	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		//This expression’s value is the index into the main method’s String[] args parameter 
		//that contains command line arguments.

		//Generate code to load the appropriate parameter from args.
		
		//System.out.println("Inside visitStatementInput");
		
		Declaration dec = statementInput.dec;
		
		mv.visitVarInsn(ALOAD, 0); //loads args
		statementInput.e.visit(this, arg); //loads @paramter from input statement
		mv.visitInsn(AALOAD); //loads args[@parameter]
		

		
		Type dec_type = Types.getType(statementInput.dec.type);
		
		//Convert from String to the appropriate type and store in the variable. 
		
		if(dec_type == Type.INTEGER) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitVarInsn(ISTORE, dec.getSlotNo());
		}
		else if(dec_type == Type.BOOLEAN) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitVarInsn(ISTORE, dec.getSlotNo());
		}
		else if(dec_type == Type.FLOAT) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", false);
			mv.visitVarInsn(FSTORE, dec.getSlotNo());
		}
		else if(dec_type == Type.FILE) {
			mv.visitVarInsn(ASTORE, dec.getSlotNo());
		}
		else if(dec_type == Type.IMAGE) {
			//If the the type is  image, the parameter is a url or file, and 
			//the image should be read from its location (invoke RuntimeImageSupport.readImage).  
			//If a size was specified when the image variable was declared, 
			//the image should be resized to this value.  Otherwise, the image retains its original size.
			
			
			if(statementInput.dec.width != null && statementInput.dec.height != null) {
				
				//get the value of url
				mv.visitVarInsn(ALOAD, dec.getSlotNo());
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				
				
				mv.visitVarInsn(ALOAD, dec.getSlotNo());
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);							
			}
			else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "readImage", RuntimeImageSupport.readImageSig, false);
			mv.visitVarInsn(ASTORE, dec.getSlotNo());
		}
		
		return statementInput;
		//throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see cop5556sp18.AST.ASTVisitor#visitStatementShow(cop5556sp18.AST.StatementShow, java.lang.Object)
	 * StatementShow ::= Expression
	 */
	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg)
			throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * For integers, booleans, and floats, generate code to print to
		 * console. For images, generate code to display in a frame.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.getType();
		switch (type) {
			case INTEGER : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(I)V", false);
			}
				break;
			case BOOLEAN : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
				
				//throw new UnsupportedOperationException();
			}
			 break; 
			 
			case FLOAT : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(F)V", false);
				//throw new UnsupportedOperationException();
			}
			break;
			case FILE : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
				//throw new UnsupportedOperationException();
			}
			break;
			case IMAGE : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				//System.out.println("Before Image Invocation");
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeFrame", RuntimeImageSupport.makeFrameSig, false);
				//System.out.println("After Image Invocation");
				//System.out.println("deteced Image!");
				//throw new UnsupportedOperationException();
			}
			break;

		}
		return statementShow;
	}

	/*
	 * (non-Javadoc)
	 * @see cop5556sp18.AST.ASTVisitor#visitStatementSleep(cop5556sp18.AST.StatementSleep, java.lang.Object)
	 * StatementSleep ::= Expression
	 */
	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		//The value of the expression is the number of msecs 
		//that the program should sleep.  Use java.lang.Thread.sleep.
		statementSleep.duration.visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return statementSleep;
		
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Label labelStart = new Label();
		Label labelEnd = new Label();
		
		mv.visitLabel(labelStart);
		statementWhile.guard.visit(this, arg);
		mv.visitJumpInsn(IFEQ, labelEnd);
		statementWhile.b.visit(this, arg);
		mv.visitJumpInsn(GOTO, labelStart);
		mv.visitLabel(labelEnd);
		
		return statementWhile;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		
		Declaration source = statementWrite.sourceDec;
		Declaration destination = statementWrite.destDec;
		mv.visitVarInsn(ALOAD, source.getSlotNo());
		mv.visitVarInsn(ALOAD, destination.getSlotNo());
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "write", RuntimeImageSupport.writeSig, false);
		//throw new UnsupportedOperationException();
		return statementWrite;
	}
	
	
	public String getASMType(Types.Type t) {
		String asmType;
		switch(t) {
			case INTEGER: 
				asmType = "I";
				break;
			case FLOAT: 
				asmType = "F";
				break;
			case BOOLEAN: 
				asmType = "Z";
				break;
			case IMAGE: 
				asmType = "Ljava/awt/image/BufferedImage;";
				break;
			case FILE: 
				asmType = "Ljava/lang/String;";
				break;
			default:
				asmType = "";
				break;
		}
		return asmType;
	}

}
