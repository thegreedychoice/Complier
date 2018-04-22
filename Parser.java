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

import java.util.ArrayList;
import java.util.List;

import cop5556sp18.AST.*;


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
		Program p = null;
		p = program();
		matchEOF();
		return p;
	}

	/*
	 * Program ::= Identifier Block
	 */
	public Program program() throws SyntaxException {
		Program p = null;
		
		Token first = t;
		Block block = null;
		
		Token progName = t;		
		
		match(IDENTIFIER);	
		block = block();		
		return new Program(first, progName, block);
	}
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 * 
	 * AS => Block ::= ( Declaration | Statement )*
	 */
	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = {KW_input, KW_write, IDENTIFIER, KW_red, KW_green, KW_blue, KW_alpha,
							KW_while, KW_if, KW_show, KW_sleep};
	Kind[] firstStatementAssignment = {IDENTIFIER, KW_red, KW_green, KW_blue, KW_alpha};

	public Block block() throws SyntaxException {
		Token first = t;
		List<ASTNode> decsOrStatements = new ArrayList<ASTNode>();
		
		match(LBRACE);
		while (isKind(firstDec)|isKind(firstStatement)) {
		     if (isKind(firstDec)) {
		    	 	Declaration d = declaration();
		    	 	decsOrStatements.add(d); 		
			} else if (isKind(firstStatement)) {
				Statement s = statement();
				decsOrStatements.add(s); 
			}
			match(SEMI);
		}
		match(RBRACE);
		
		return new Block(t, decsOrStatements);
	}
	
	/*
	 * Statement ::= StatementInput | StatementWrite | StatementAssignment 
		| StatementWhile | StatementIf | StatementShow | StatementSleep
	 */
	public Statement statement() throws SyntaxException{
		//System.out.println("Statement");
		Token first;
		
		if(isKind(KW_input)) {
			return statementInput();
		}
		else if(isKind(KW_write)) {
			return statementWrite();
		}
		else if(isKind(firstStatementAssignment)) {
			return statementAssignment();
		}
		else if(isKind(KW_while)) {
			return statementWhile();
		}
		else if(isKind(KW_if)) {
			//System.out.println("If");
			return statementIf();
		}
		else if(isKind(KW_show)) {
			return statementShow();
		}
		else if(isKind(KW_sleep)) {
			return statementSleep();
		}
		else {
			throw new SyntaxException(t, "Illegal Statement");
		}
		
		//throw new UnsupportedOperationException();
	}
	
	/*
	 * StatementInput ::= input IDENTIFIER from @ Expression
	 */
	public Statement statementInput() throws SyntaxException{
		Token first = t;
		Token destName = null;
		Expression e = null;
		
		match(KW_input);
		destName = t;
		match(IDENTIFIER);
		match(KW_from);
		match(OP_AT);
		e = expression();
		
		return new StatementInput(first, destName, e);
	}
	
	/*
	 * StatementWrite ::= write IDENTIFIER to IDENTIFIER
	 */
	public Statement statementWrite() throws SyntaxException{
		Token first = t;
		Token sourceName = null;
		Token destName = null;
		
		match(KW_write);
		sourceName = t;
		match(IDENTIFIER);
		match(KW_to);
		destName = t;
		match(IDENTIFIER);	
		
		return new StatementWrite(first, sourceName, destName);
	}
	
	/*
	 * StatementAssignment ::=  LHS := Expression
	 */
	public Statement statementAssignment() throws SyntaxException{
		//System.out.println("Statement Assignment");
		Token first = t;
		LHS lhs = null;
		Expression e = null;
		
		lhs = LHS();
		match(OP_ASSIGN);
		e = expression();
		
		return new StatementAssign(first, lhs, e);
	}
	
	/*
	 * LHS ::=  IDENTIFIER | IDENTIFIER PixelSelector | Color ( IDENTIFIER PixelSelector )
	 * 
	 * LHS ::=  IDENTIFIER (E | PixelSelector ) | Color ( IDENTIFIER PixelSelector )
	 */
	public LHS LHS() throws SyntaxException{
		//System.out.println("LHS");
		Token first = t;
		Token name = null;
		PixelSelector ps = null;
		
		if(isKind(IDENTIFIER)) {
			//System.out.println("LHS -> Identifier");
			name = t;
			match(IDENTIFIER);
			
			/*
			if(isKind(EOF)) return new LHSIdent(first, name);
			*/
			
			if(isKind(LSQUARE)) {
				//pixel selector
				ps = PixelSelector();
				return new LHSPixel(first, name, ps);
			}
			else {
				return new LHSIdent(first, name);
			}
		}
		else if(isKind(KW_red) || isKind(KW_green) || isKind(KW_blue) || isKind(KW_alpha)) {
			//System.out.println("Inside Color!");
			Token color = t;
			consume();
			match(LPAREN);
			name = t;
			match(IDENTIFIER);
			if(isKind(LSQUARE)) {
				//System.out.println("Get Pixel Selector inside Color!");
				//pixel selector
				ps = PixelSelector();
			}
			else {
				throw new SyntaxException(t, "Illegal LHS! No pixel Selector after Color");
			}
			match(RPAREN);
			return new LHSSample(first, name, ps, color);
		}
		else {
			throw new SyntaxException(t, "Illegal LHS");
		}
	}
	
	/*
	 * StatementWhile ::=  while (Expression ) Block
	 */
	public Statement statementWhile() throws SyntaxException{
		Token first = t;
		Expression guard = null;
		Block b = null;
		
		match(KW_while);
		match(LPAREN);
		guard = expression();
		match(RPAREN);
		b = block();
		
		return new StatementWhile(first, guard, b);
	}
	
	/*
	 * StatementIf ::=  if ( Expression ) Block 
	 */
	public Statement statementIf() throws SyntaxException{
		Token first = t;
		Expression guard = null;
		Block b = null;
		
		match(KW_if);
		match(LPAREN);
		guard = expression();
		match(RPAREN);
		b = block();
		
		return new StatementIf(first, guard, b);
	}
	
	/*
	 * StatementShow ::=  show Expression
	 */
	public Statement statementShow() throws SyntaxException{
		//System.out.println("Statement Show!");
		Token first = t;
		Expression e = null;
		
		match(KW_show);
		e = expression();
		return new StatementShow(first, e);
	}
	
	/*
	 * StatementSleep ::=  sleep Expression
	 */
	public Statement statementSleep() throws SyntaxException{
		Token first = t;
		Expression duration = null;
		
		match(KW_sleep);
		duration = expression();
		
		return new StatementSleep(first, duration);
	}
	
	/*
	 * Declaration ::= Type IDENTIFIER | image IDENTIFIER [ Expression , Expression ]
	 * 
	 * AS => Declaration ::= Type IDENTIFIER ( ε| Expression Expression )
	 */
	public Declaration declaration() throws SyntaxException {
		//System.out.println("Declaration");
		//TODO
		Token first = t;
		Token type = null;
		Expression width = null;
		Expression height = null;
		
		Token name;
		
		if(isKind(firstDec)) {
			type = t;
			
			//if kind is image
			if(isKind(KW_image)) {
							
				consume();
				name = t;
				
				match(IDENTIFIER);
				if(!isKind(LSQUARE)) {
					if(isKind(SEMI) || isKind(EOF)) {
						//return new Declaration(first, type, name, width, height);
						//do nothing
					}
					else {
						throw new SyntaxException(t, "Illegal image declaration!");
					}
				}
				else {
					
					match(LSQUARE);
					width = expression();
					match(COMMA);
					height = expression();
					match(RSQUARE);
					//return new Declaration(first, type, name, width, height);
				}
							
			} //everything else like int, float, boolean, filename
			else {
				consume();
				name = t;
				match(IDENTIFIER);
				//return new Declaration(first, type, name, width, height);
			}
			return new Declaration(first, type, name, width, height);
		}
		else {
			throw new SyntaxException(t, "Illegal Declaration");
		}
	}
	/*
	 * Expression ::= OrExpression  ?  Expression  :  Expression
	               |   OrExpression

	 * Expression ::=  OrExpression  (E | ?  Expression  :  Expression)
	 */
	public Expression expression() throws SyntaxException {
		//TODO
		//System.out.println("inside Expression");
		Token first = t;		
		Expression guard = null;
		Expression trueExp = null;
		Expression falseExp = null;
		
		guard = OrExpression();
				
		if(isKind(OP_QUESTION)) {
			match(OP_QUESTION);
			trueExp = expression();
			match(OP_COLON);
			falseExp = expression();
			return new ExpressionConditional(first, guard, trueExp, falseExp); 
		}
		else {
			return guard;	
		}	
		
	}
	
	/*
	 * OrExpression  ::=  AndExpression   (  |  AndExpression ) *
	 */
	public Expression OrExpression() throws SyntaxException {
		//TODO
		//System.out.println("Or Expression");
		
		Token first = t;
		Expression left = null;
		Token op = null;
		Expression right = null;
		
		left = AndExpression();
		
		while(isKind(OP_OR)) {
			op = t;
			match(OP_OR);
			
			right = AndExpression();
			left = new ExpressionBinary(first, left, op, right);
		}
		
		return left;
	}
	
	/*
	 * AndExpression ::=  EqExpression ( & EqExpression )*
	 */
	public Expression AndExpression() throws SyntaxException {
		//TODO
		//System.out.println("And Expression");
		
		Token first = t;
		Expression left = null;
		Token op = null;
		Expression right = null;
		
		left = EqExpression();
		
		while(isKind(OP_AND)) {
			op = t;
			match(OP_AND);
			
			right = EqExpression();
			left = new ExpressionBinary(first, left, op, right);
		}
		
		return left;
	}
	
	/*
	 * EqExpression ::=  RelExpression  (  (== | != )  RelExpression )*
	 */
	public Expression EqExpression() throws SyntaxException {
		//TODO
		//System.out.println("Eq Expression");
		Token first = t;
		Expression left = null;
		Token op = null;
		Expression right = null;
		
		
		left = RelExpression();
		
		while(isKind(OP_EQ) || isKind(OP_NEQ)) {
			if(isKind(OP_EQ)) {
				op = t;
				match(OP_EQ);
				right = RelExpression();
			}
			else if(isKind(OP_NEQ)) {
				op = t;
				match(OP_NEQ);
				right = RelExpression();
			}
			else {break;}
			
			left = new ExpressionBinary(first, left, op, right);
		}
		
		return left;
	}
	
	/*
	 * RelExpression ::= AddExpression (  (<  | > |  <=  | >= )   AddExpression)*
	 */
	public Expression RelExpression() throws SyntaxException {
		//TODO
		//System.out.println("Rel Expression");
		Token first = t;
		Expression left = null;
		Token op = null;
		Expression right = null;
		
		left = AddExpression();	
		
		while(isKind(OP_LT) || isKind(OP_GT) || isKind(OP_LE) || isKind(OP_GE)) {
			
			if(isKind(OP_LT)) {
				op = t;
				match(OP_LT);
				right = AddExpression();
			}
			else if(isKind(OP_GT)) {
				op = t;
				match(OP_GT);
				right = AddExpression();
			}
			else if(isKind(OP_LE)) {
				op = t;
				match(OP_LE);
				right = AddExpression();
			}			
			else if(isKind(OP_GE)) {
				op = t;
				match(OP_GE);
				right = AddExpression();
			}
			else {break;}
			left = new ExpressionBinary(first, left, op, right);
		}
		
		return left;
	}
	
	/*
	 * AddExpression ::= MultExpression   (  ( + | - ) MultExpression )*
	 */
	public Expression AddExpression() throws SyntaxException {
		//TODO
		//System.out.println("Add Expression");
		Token first = t;
		Expression left = null;
		Token op = null;
		Expression right = null;
		
		left = MultExpression();
		
		while(isKind(OP_PLUS) || isKind(OP_MINUS)) {
			if(isKind(OP_PLUS)) {
				op = t;
				match(OP_PLUS);
				right = MultExpression();
			}
			else if(isKind(OP_MINUS)) {
				op = t;
				match(OP_MINUS);
				right = MultExpression();
			}
			else {break;}
			left = new ExpressionBinary(first, left, op, right);
		}
		return left;
	}
	
	/*
	 * MultExpression := PowerExpression ( ( * | /  | % ) PowerExpression )*
	 */
	public Expression MultExpression() throws SyntaxException {
		//TODO
		//System.out.println("Mult Expression");
		Token first = t;
		Expression left = null;
		Token op = null;
		Expression right = null;
		
		left = PowerExpression();
		
		while(isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_MOD) ) {
			if(isKind(OP_TIMES)) {
				op = t;
				match(OP_TIMES);
				right = PowerExpression();
			}			
			else if(isKind(OP_DIV)) {
				op = t;
				match(OP_DIV);
				right = PowerExpression();
			}			
			else if(isKind(OP_MOD)) {
				op = t;
				match(OP_MOD);
				right = PowerExpression();
			}
			else {break;}
			left = new ExpressionBinary(first, left, op, right);
		}
		return left;

	}
	

	
	/*
	 * PowerExpression := UnaryExpression  (** PowerExpression | ε)
	 */
	public Expression PowerExpression() throws SyntaxException {
		//TODO
		//System.out.println("Power Expression");
		Token first = t;		
		Expression left = null;
		Token op = null;
		Expression right = null;
		
		left = UnaryExpression();
				
		if(isKind(OP_POWER)) {
			op = t;
			match(OP_POWER);
			right = PowerExpression();
			return new ExpressionBinary(first, left, op, right);
		}
		
		
		return left;

	}
	
	/*
	 * UnaryExpression ::= + UnaryExpression 
	 * 					 | - UnaryExpression 
	 * 					 | UnaryExpressionNotPlusMinus
	 */
	public Expression UnaryExpression() throws SyntaxException {
		//TODO
		//System.out.println("Unary Expression");
		Token first = t;
		Expression expression = null;
		Token op = null;

		
		if(isKind(OP_PLUS)) {
			//System.out.println("Plus");
			op = t;
			match(OP_PLUS);
			expression = UnaryExpression();
			return new ExpressionUnary(first, op, expression);
		}	
		else if(isKind(OP_MINUS)) {
			op = t;
			match(OP_MINUS);
			expression = UnaryExpression();
			return new ExpressionUnary(first, op, expression);
		}
		else {
			//System.out.println("After UnaryExpressionNotPlusMinus");
			return UnaryExpressionNotPlusMinus();
		}
	}
	
	/*
	 * UnaryExpressionNotPlusMinus ::=  ! UnaryExpression  
	 * 								   | Primary 
	 */
	public Expression UnaryExpressionNotPlusMinus() throws SyntaxException {
		//TODO
		//System.out.println("UnaryExpressionNotPlusMinus Expression");
		
		Token first = t;
		Expression expression = null;
		Token op = null;
		
		if(isKind(OP_EXCLAMATION)) {
			op = t;
			match(OP_EXCLAMATION);
			expression = UnaryExpression();
			return new ExpressionUnary(first, op, expression); 
		}
		else {
			return primary();
		}
	}
	
	
	Kind[] functionName = { KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, 
			KW_polar_a, KW_polar_r, KW_int, KW_float, KW_width, KW_height,
			KW_red, KW_blue, KW_green, KW_alpha };
	Kind[] predefinedName = { KW_Z, KW_default_width, KW_default_height};
	
	/*
	 * Primary ::= INTEGER_LITERAL | BOOLEAN_LITERAL | FLOAT_LITERAL | 
                ( Expression ) | FunctionApplication  | IDENTIFIER | PixelExpression | 
                 PredefinedName | PixelConstructor
	
	   Primary ::= INTEGER_LITERAL | BOOLEAN_LITERAL | FLOAT_LITERAL | 
                ( Expression ) | FunctionApplication | PredefinedName | PixelConstructor
                | IDENTIFIER ( E | PixelSelector)
	 */
	public Expression primary() throws SyntaxException {
		//TODO
		//System.out.println("Primary Expression");
		Token first = t;
		if(isKind(INTEGER_LITERAL)) {
			Token intLiteral = t;
			match(INTEGER_LITERAL);
			return new ExpressionIntegerLiteral(first, intLiteral);
		}
		else if(isKind(BOOLEAN_LITERAL)) {
			Token boolLiteral = t;
			match(BOOLEAN_LITERAL);
			return new ExpressionBooleanLiteral(first, boolLiteral);
		}
		else if(isKind(FLOAT_LITERAL)) {
			Token floatLiteral = t;
			match(FLOAT_LITERAL);
			return new ExpressionFloatLiteral(first, floatLiteral);
		}
		else if(isKind(LPAREN)) {
			match(LPAREN);
			Expression e = expression();
			match(RPAREN);
			return e;
		}
		else if(isKind(functionName)) {
			//functionApplication
			return functionApplication();
		}
		else if(isKind(predefinedName)) {
			//Z | default_height | default_width
			return predefinedName();
		}
		else if(isKind(LPIXEL)) {
			//pixel constructor
			return PixelConstructor();
		}
		else if(isKind(IDENTIFIER)) {
			//System.out.println("inside primary -> identifier");
			//System.out.println(t.getText());
			Token name = t;
			match(IDENTIFIER);
			
			/*
			if(isKind(EOF)){
				return new ExpressionIdent(first, name);
			}
			*/
			if(isKind(LSQUARE)) {
				PixelSelector ps = PixelSelector();
				return new ExpressionPixel(first, name, ps);
			}
			else {
				return new ExpressionIdent(first, name);
			}
			/*
			else if(isKind(OP_PLUS) || isKind(OP_MINUS) || isKind(OP_TIMES) ||isKind(OP_DIV) || isKind(OP_MOD)) {
				UnaryExpression();
				return;
			}
			*/

		}
		else {
			throw new SyntaxException(t, "Illegal Primary");
		} 
	}
	
	/*
	 *PredefinedName ::= Z | default_height | default_width
	 */
	public Expression predefinedName() throws SyntaxException {
		//System.out.println("Function predefinedName");	
		Token first = t;
		Token name = null;
		
		if(isKind(KW_Z)) {
			name = t;
			match(KW_Z);
			return new ExpressionPredefinedName(first, name);
			
		}
		else if(isKind(KW_default_height)) {
			name = t;
			match(KW_default_height);
			return new ExpressionPredefinedName(first, name);
		}
		else if(isKind(KW_default_width)) {
			name = t;
			match(KW_default_width);
			return new ExpressionPredefinedName(first, name);
		}
		else {
			throw new SyntaxException(t, "Illegal PredefinedName Syntax");
		}
	}
	
	
	/*
	 * FunctionApplication ::= FunctionName ( Expression )  
	 * 						  | FunctionName  [ Expression , Expression ] 
	 * 
	 * FunctionApplication ::= FunctionName ( ( Expression )  |  [ Expression , Expression ] )
	 */
	public Expression functionApplication() throws SyntaxException {
		//System.out.println("Function Application");
		//System.out.print("Tok : ");
		//System.out.println(t.kind);
		Token first = t;
		Token functionName = t;
		
		consume();
		if(isKind(LPAREN)) {
			// ( Expression ) 
			match(LPAREN);
			Expression e = expression();
			match(RPAREN);
			return new ExpressionFunctionAppWithExpressionArg(first, functionName, e);
			
		}
		else if(isKind(LSQUARE)) {
			// [ Expression , Expression ]
			match(LSQUARE);
			Expression e0 = expression();
			match(COMMA);
			Expression e1 = expression();
			match(RSQUARE);
			return new ExpressionFunctionAppWithPixel(first, functionName, e0, e1);
		}
		else {
			throw new SyntaxException(t, "Illegal functionApplication Syntax");
		}
	}
	/*
	 * PixelSelector ::= [ Expression , Expression ]
	 */
	public PixelSelector PixelSelector() throws SyntaxException {
		Token first = t; 
		Expression ex = null;
		Expression ey = null;
		
		match(LSQUARE);
		ex = expression();
		match(COMMA);
		ey = expression();
		match(RSQUARE);
		
		return new PixelSelector(first, ex, ey);
	}
	
	/*
	 * PixelConstructor ::=  <<  Expression , Expression , Expression , Expression  >> 
	 */
	public Expression PixelConstructor() throws SyntaxException {
		Token first = t; 
		Expression alpha = null;
		Expression red = null; 
		Expression green = null;
		Expression blue = null;
		
		match(LPIXEL);
		alpha = expression();
		match(COMMA);
		red = expression();
		match(COMMA);
		green = expression();
		match(COMMA);
		blue = expression();
		match(RPIXEL);
		
		return new ExpressionPixelConstructor(first, alpha, red, green, blue);
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
		System.out.print("Token : ");
		System.out.println(kind);
		throw new SyntaxException(t,"Match Syntax Error : Not same kind"); //TODO  give a better error message!
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

