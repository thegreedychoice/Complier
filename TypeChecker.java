package cop5556sp18;

import cop5556sp18.Scanner.Token;
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
import cop5556sp18.Scanner.Kind;

public class TypeChecker implements ASTVisitor {

	SymbolTableManager symbolTb;
	TypeChecker() {
		symbolTb = new SymbolTableManager();
	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Inside block" + block);
		this.symbolTb.enterScope();
		for(ASTNode node: block.decsOrStatements) {
			node.visit(this, arg);
		}
		this.symbolTb.exitScope();
		return block;
		
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//Declaration.name ← IDENTIFIER.name
		System.out.println("Start declaration for " + declaration);
		//Declaration.name not present in SymbolTable.currentScope
		Declaration dec = symbolTb.lookupdec(declaration.name);
		if(dec != null) {
			System.out.println("Dec is not null");
			System.out.println(declaration.firstToken);
			Error(declaration.firstToken, "visitDeclaration(..)", "Identifier already exists!");
		}
		
		if(declaration.width == null && declaration.height == null) {
			// do nothing
			//valid
		}
		else {
			
			if((declaration.width != null && declaration.height == null) || (declaration.width == null && declaration.height != null)) {
				Error(declaration.firstToken, "visitDeclaration(..)", "Image width & Height Invalid types!");
			}
			
			declaration.width.visit(this, arg);
			declaration.height.visit(this, arg);
			
			
			if(!(declaration.width.getType() == Type.INTEGER && declaration.getType() == Type.IMAGE)) {
				//System.out.println("Culprit Width!");
				Error(declaration.firstToken, "visitDeclaration(..)", "Invalid Type!");
			}
			
			if(!(declaration.height.getType() == Type.INTEGER && declaration.getType() == Type.IMAGE)) {
				//System.out.println("Culprit Height!");
				Error(declaration.firstToken, "visitDeclaration(..)", "Invalid Type!");
			}
			
			
		}
		
		boolean isInserted = this.symbolTb.insert(declaration.name, declaration);
		System.out.println(isInserted);
		return declaration;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementWrite.sourceDec = this.symbolTb.lookup(statementWrite.sourceName);
		if(statementWrite.sourceDec == null)
			Error(statementWrite.firstToken, "visitStatementWrite(..)", "source Declaration is null!");
		
		statementWrite.destDec = this.symbolTb.lookup(statementWrite.destName);
		if(statementWrite.destDec == null)
			Error(statementWrite.firstToken, "visitStatementWrite(..)", "dest Declaration is null!");
		
		//visit these declarations
		//statementWrite.sourceDec.visit(this, arg);
		//statementWrite.destDec.visit(this, arg);
		
		
		
		if(Types.getType(statementWrite.sourceDec.type) != Type.IMAGE)
			Error(statementWrite.firstToken, "visitStatementWrite(..)", "source Declaration Type is not Image!");
		if(Types.getType(statementWrite.destDec.type) != Type.FILE)
			Error(statementWrite.firstToken, "visitStatementWrite(..)", "dest Declaration Type is not Filename!");
		
		return statementWrite;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		//System.out.println("Inside typechecker visitStatementInput");
		
		statementInput.e.visit(this, arg);
		statementInput.dec = this.symbolTb.lookup(statementInput.destName);
		if(statementInput.dec == null)
			Error(statementInput.firstToken, "visitStatementInput(..)", "Input identifier doesn't exists!");
		//visit this if not null
		//statementInput.dec.visit(this, arg);
		
		if(statementInput.e != null) {
			if(statementInput.e.getType() != Type.INTEGER) 
				Error(statementInput.firstToken, "visitStatementInput(..)", "Input expression type not Integer!");
		}
		//System.out.println(statementInput);
		return statementInput;
		//throw new UnsupportedOperationException();
	}

	
	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//visit nodes inside pixelSelector
		pixelSelector.ex.visit(this, arg);
		pixelSelector.ey.visit(this, arg);
		
		if(pixelSelector.ex.getType() != pixelSelector.ey.getType())
			Error(pixelSelector.firstToken, "visitPixelSelector(..)", "Expressions Type don't match!");
		
		if(pixelSelector.ex.getType() != Type.INTEGER && pixelSelector.ex.getType() != Type.FLOAT)
			Error(pixelSelector.firstToken, "visitPixelSelector(..)", "Invalid Type Expression ex!");
		
		return pixelSelector;
		
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//visit all the nodes inside
		expressionConditional.guard.visit(this, arg);
		expressionConditional.trueExpression.visit(this, arg);
		expressionConditional.falseExpression.visit(this, arg);
		
		if(expressionConditional.guard.getType() != Type.BOOLEAN)
			Error(expressionConditional.firstToken, "visitExpressionConditional(..)", "Expression Guard not Boolean!");
		if(expressionConditional.trueExpression.getType() != expressionConditional.falseExpression.getType())
			Error(expressionConditional.firstToken, "visitExpressionConditional(..)", "Expression Types don't match!");
		
		expressionConditional.setType(expressionConditional.trueExpression.getType());
		
		return expressionConditional;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//visit nodes
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);
		Type t = inferredType(expressionBinary.leftExpression.getType(), 
				expressionBinary.rightExpression.getType(), expressionBinary.op);
		if(t == null)
			Error(expressionBinary.firstToken, "visitExpressionBinary(..)", "Types not compatible!");
		
		expressionBinary.setType(t);
		
		return expressionBinary;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//visit nodes inside
		expressionUnary.expression.visit(this, arg);
		Type t = expressionUnary.expression.getType();
		if(t == Type.NONE)
			Error(expressionUnary.firstToken, "visitExpressionUnary(..)", "Types is None!");

		expressionUnary.setType(t);
		
		return expressionUnary;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionIntegerLiteral.setType(Type.INTEGER);
		return expressionIntegerLiteral;
		
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionBooleanLiteral.setType(Type.BOOLEAN);
		return expressionBooleanLiteral;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionPredefinedName.setType(Type.INTEGER);
		return expressionPredefinedName;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionFloatLiteral.setType(Type.FLOAT);
		return expressionFloatLiteral;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("Inside visitExpressionFunctionAppWithExpressionArg");
		expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		
		Type t = inferredTypeFunctionApp(expressionFunctionAppWithExpressionArg.function, expressionFunctionAppWithExpressionArg.e.getType());
		
		if(t == Type.NONE)
			Error(expressionFunctionAppWithExpressionArg.firstToken, "expressionFunctionAppWithExpressionArg(..)", "Types is None!");

		expressionFunctionAppWithExpressionArg.setType(t);
		

		return expressionFunctionAppWithExpressionArg;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		//visit the nodes
		expressionFunctionAppWithPixel.e0.visit(this, arg);
		expressionFunctionAppWithPixel.e1.visit(this, arg);
		
		if(expressionFunctionAppWithPixel.name == Kind.KW_cart_x || expressionFunctionAppWithPixel.name == Kind.KW_cart_y) {
			if(expressionFunctionAppWithPixel.e0.getType() != Type.FLOAT)
				Error(expressionFunctionAppWithPixel.firstToken, "visitExpressionFunctionAppWithPixel(..)", "Expression e0 Type Not Float!");
			if(expressionFunctionAppWithPixel.e1.getType() != Type.FLOAT)
				Error(expressionFunctionAppWithPixel.firstToken, "visitExpressionFunctionAppWithPixel(..)", "Expression e1 Type Not Float!");
			expressionFunctionAppWithPixel.setType(Type.INTEGER);
		}
		
		if(expressionFunctionAppWithPixel.name == Kind.KW_polar_a || expressionFunctionAppWithPixel.name == Kind.KW_polar_r) {
			if(expressionFunctionAppWithPixel.e0.getType() != Type.INTEGER)
				Error(expressionFunctionAppWithPixel.firstToken, "visitExpressionFunctionAppWithPixel(..)", "Expression e0 Type Not Integer!");
			if(expressionFunctionAppWithPixel.e1.getType() != Type.INTEGER)
				Error(expressionFunctionAppWithPixel.firstToken, "visitExpressionFunctionAppWithPixel(..)", "Expression e1 Type Not Integer!");
			expressionFunctionAppWithPixel.setType(Type.FLOAT);
		}
		
		return expressionFunctionAppWithPixel;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//visit all nodes inside
		expressionPixelConstructor.alpha.visit(this, arg);
		expressionPixelConstructor.red.visit(this, arg);
		expressionPixelConstructor.green.visit(this, arg);
		expressionPixelConstructor.blue.visit(this, arg);
		
		/*
		expressionPixelConstructor.alpha.setType(Type.INTEGER);
		expressionPixelConstructor.red.setType(Type.INTEGER);
		expressionPixelConstructor.green.setType(Type.INTEGER);
		expressionPixelConstructor.blue.setType(Type.INTEGER);
		*/
		
		Type t_alpha = expressionPixelConstructor.alpha.getType();
		Type t_red = expressionPixelConstructor.red.getType();
		Type t_green = expressionPixelConstructor.green.getType();
		Type t_blue = expressionPixelConstructor.blue.getType();
		if(!(t_alpha ==Type.INTEGER && t_red ==Type.INTEGER && t_green==Type.INTEGER && t_blue==Type.INTEGER))
			Error(expressionPixelConstructor.firstToken,"visitExpressionPixelConstructor(..)", "Not all types are Integer");
		
		
		expressionPixelConstructor.setType(Type.INTEGER);
		
		return expressionPixelConstructor;
		
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//visit the nodes in statementAssign
		//System.out.println("Inside Statement assign");
		
		statementAssign.lhs.visit(this, arg);
		statementAssign.e.visit(this, arg);
		
		if(statementAssign.lhs.getType() != statementAssign.e.getType())
			Error(statementAssign.firstToken, "visitStatementAssign(..)", "Types don't match!");
			
		return statementAssign;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//visit nodes inside
		//System.out.println(statementShow);
		//System.out.println(statementShow.e.getType());
		
		statementShow.e.visit(this, arg);
		
		//System.out.println("After Expression visit in Statement Show");
		//System.out.println(statementShow.e.getType());
		//System.out.println();
		
		if(statementShow.e.getType() != Type.INTEGER && statementShow.e.getType() != Type.BOOLEAN &&
				statementShow.e.getType() != Type.FLOAT && statementShow.e.getType() != Type.IMAGE) {
			Error(statementShow.firstToken, "visitStatementShow(..)", "Invalid Type for expression e!");
		}
		
		return statementShow;
			
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionPixel.pixelSelector.visit(this, arg);
		expressionPixel.dec = this.symbolTb.lookup(expressionPixel.name);
		if(expressionPixel == null)
			Error(expressionPixel.firstToken, "visitExpressionPixel(..)", "Identifier doesn't exists!");
		//expressionPixel.dec.visit(this, arg);
		
		if(Types.getType(expressionPixel.dec.type) != Type.IMAGE)
			Error(expressionPixel.firstToken, "visitExpressionPixel(..)", "Type isn't Image!");
		expressionPixel.setType(Type.INTEGER);
		return expressionPixel;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("Inside visitExpressionIdent");
		//System.out.println(expressionIdent);
		//System.out.println(expressionIdent.dec);
		
		
		//System.out.println("visitExpressionIdent Name");
		//System.out.println(expressionIdent.name);
		
		//expressionIdent.dec = this.symbolTb.peek(expressionIdent.name);
		expressionIdent.dec = this.symbolTb.lookup(expressionIdent.name);
		
		//System.out.println("After Symbol table lookup");
		//System.out.println(expressionIdent.dec);
		//System.out.println();
		
		if(expressionIdent.dec == null)
			Error(expressionIdent.firstToken, "visitExpressionIdent(..)", "Identifier doesn't exists!");
		
		//expressionIdent.dec.visit(this, arg);
		
		expressionIdent.setType(Types.getType(expressionIdent.dec.type));
		
		
		//System.out.println("expressionIdent returned!");
		return expressionIdent;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		// TODO Auto-generated method stub
		lhsSample.pixelSelector.visit(this, arg);
		Declaration dec = this.symbolTb.lookup(lhsSample.name);
		
		if(dec == null)
			Error(lhsSample.firstToken, "visitLHSSample(..)", "Identifier doesn't exist!");
		
		//visit nodes inside
		//lhsSample.dec.visit(this, arg);
		
		if(Types.getType(dec.type) != Type.IMAGE)
			Error(lhsSample.firstToken, "visitLHSSample(..)", "Type not image!");
		
		lhsSample.setType(Type.INTEGER);
		lhsSample.dec = dec;
		
		return lhsSample;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		lhsPixel.pixelSelector.visit(this, arg);
		Declaration dec = this.symbolTb.lookup(lhsPixel.name);
		
		if(dec == null)
			Error(lhsPixel.firstToken, "visitLHSPixel(..)", "Identifier doesn't exist!");
		
		//visit nodes inside
		//lhsPixel.dec.visit(this, arg);
		
		if(Types.getType(dec.type) != Type.IMAGE)
			Error(lhsPixel.firstToken, "visitLHSPixel(..)", "Type not image!");
		
		lhsPixel.setType(Type.INTEGER);
		lhsPixel.dec = dec;
		
		return lhsPixel;
			
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		System.out.println();
		System.out.println(lhsIdent.name);
		Declaration dec = this.symbolTb.lookup(lhsIdent.name);
		if(dec != null)
			lhsIdent.setType(Types.getType(dec.type));
		else {
			System.out.println(lhsIdent.firstToken);
			Error(lhsIdent.firstToken, "visitLHSIdent(..)", "Identifier doesn't exists!");
			}
		//if all good, visit nodes inside
		//lhsIdent.dec.visit(this,arg);
		
		if(lhsIdent.getType() == Type.NONE)
			Error(lhsIdent.firstToken, "visitLHSIdent(..)", "LHS Ident type can't be None!");
		
		lhsIdent.dec = dec;
		return lhsIdent;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//visit nodes in statementWhile
		//System.out.println();
		//System.out.println("Start of visitStatementIf");
		
		statementIf.guard.visit(this, arg);
		statementIf.b.visit(this, arg);
		
		//System.out.println(statementIf.guard);
		//System.out.println(statementIf.b);
		
		if(statementIf.guard.getType() != Type.BOOLEAN)
			Error(statementIf.firstToken, "visitStatementIf(..)", "Types don't match!");
		
		
		//System.out.println("End of visitStatementIf");
		//System.out.println();
		
		return statementIf;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//visit nodes in statementWhile
		statementWhile.b.visit(this, arg);
		statementWhile.guard.visit(this, arg);
		
		
		if(statementWhile.guard.getType() != Type.BOOLEAN)
			Error(statementWhile.firstToken, "visitStatementWhile(..)", "Types don't match!");
			
		return statementWhile;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//visit nodes inside
		statementSleep.duration.visit(this, arg);
		
		if(statementSleep.duration.getType() != Type.INTEGER)
			Error(statementSleep.firstToken, "visitStatementSleep(..)", "Type not Integer!");
		
		return statementSleep;
		//throw new UnsupportedOperationException();
	}

	public void Error(Token t, String fname, String msg) throws SemanticException {
		throw new SemanticException(t, new String("Semantic Error -> Function Name : " + fname + "Issue : " + msg));
	}
	
	public Type inferredType(Type e0, Type e1, Kind op) {
		if((op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || op == Kind.OP_DIV || op == Kind.OP_MOD
				|| op == Kind.OP_POWER || op == Kind.OP_AND || op == Kind.OP_OR) 
				&& e0 == Type.INTEGER && e1 == Type.INTEGER) {
			return Type.INTEGER;
		}
		else if((op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_TIMES || op == Kind.OP_DIV || op == Kind.OP_POWER) 
				&& ((e0 == Type.FLOAT && e1 == Type.FLOAT) || (e0 == Type.FLOAT && e1 == Type.INTEGER) 
						|| (e0 == Type.INTEGER && e1 == Type.FLOAT))) {
			return Type.FLOAT;
		}
		else if(((op == Kind.OP_AND || op == Kind.OP_OR) && (e0 == Type.BOOLEAN && e1 == Type.BOOLEAN)) || 
				((op == Kind.OP_EQ || op == Kind.OP_NEQ || op == Kind.OP_GT || op == Kind.OP_GE || op == Kind.OP_LT || op == Kind.OP_LE) &&
						((e0 == Type.INTEGER && e1 == Type.INTEGER) || (e0 == Type.FLOAT && e1 == Type.FLOAT) || (e0 == Type.BOOLEAN && e1 == Type.BOOLEAN)))
				) {
			return Type.BOOLEAN;
		}
		else
			return Type.NONE;
	}
	
	public Type inferredTypeFunctionApp(Kind functionName, Type e) {
		if((functionName == Kind.KW_abs || functionName == Kind.KW_red || functionName == Kind.KW_green
				|| functionName == Kind.KW_blue || functionName == Kind.KW_alpha) && e == Type.INTEGER) {
			return Type.INTEGER;
		}
		else if((functionName == Kind.KW_abs || functionName == Kind.KW_sin || functionName == Kind.KW_cos
				|| functionName == Kind.KW_atan || functionName == Kind.KW_log) && e == Type.FLOAT) {
			return Type.FLOAT;
		}
		else if((functionName == Kind.KW_width || functionName == Kind.KW_height) && e == Type.IMAGE) {
			return Type.INTEGER;
		}
		else if(functionName == Kind.KW_float && e == Type.INTEGER) {
			return Type.FLOAT;
		}
		else if(functionName == Kind.KW_float && e == Type.FLOAT) {
			return Type.FLOAT;
		}
		else if(functionName == Kind.KW_int && e == Type.FLOAT) {
			return Type.INTEGER;
		}
		else if(functionName == Kind.KW_int && e == Type.INTEGER) {
			return Type.INTEGER;
		}
		else 
			return Type.NONE;
	}
}
