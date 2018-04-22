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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Scanner.Token;
import static cop5556sp18.Scanner.Kind.*;

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
	/*
	@Test
	public void testEmpty() throws LexicalException {
		
		System.out.println("Start testEmpty()");
		
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
		
		System.out.println("End testEmpty() \n");
	}
	
/*
	//@Test
	public void testSemi() throws LexicalException {
		System.out.println("Start testSemi()");
		
		String input = ";;\n;;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
		
		System.out.println("End testSemi() \n");
	}
	

	//@Test
	public void testOperators() throws LexicalException {
		System.out.println("Start testOperators()");
		
		String input = ">=<!\n?==::= \n@***\n&|+-";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_GE, 0, 2, 1, 1);
		checkNext(scanner, OP_LT, 2, 1, 1, 3);
		checkNext(scanner, OP_EXCLAMATION, 3, 1, 1, 4);
		checkNext(scanner, OP_QUESTION, 5, 1, 2, 1);
		checkNext(scanner, OP_EQ, 6, 2, 2, 2);
		checkNext(scanner, OP_COLON, 8, 1, 2, 4);
		checkNext(scanner, OP_ASSIGN, 9, 2, 2, 5);
		checkNext(scanner, OP_AT, 13, 1, 3, 1);
		checkNext(scanner, OP_POWER, 14, 2, 3, 2);
		checkNext(scanner, OP_TIMES, 16, 1, 3, 4);
		checkNext(scanner, OP_AND, 18, 1, 4, 1);
		checkNext(scanner, OP_OR, 19, 1, 4, 2);
		checkNext(scanner, OP_PLUS, 20, 1, 4, 3);
		checkNext(scanner, OP_MINUS, 21, 1, 4, 4);
		checkNextIsEOF(scanner);
		
		System.out.println("End testOperators() \n");
	}

	//@Test
	public void testIntegerLiteralsstartwith0() throws LexicalException {
		System.out.println("Start testIntegerLiteralsstartwith0()");
		
		String input = "012345";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 1, 5, 1, 2);

		checkNextIsEOF(scanner);
		
		System.out.println("End testIntegerLiteralsstartwith0() \n");
	}
	

	//@Test
	public void testKeywords() throws LexicalException {
		System.out.println("Start testKeywords()");
		
		String input = "true@false";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, BOOLEAN_LITERAL, 0, 4, 1, 1);
		checkNext(scanner, OP_AT, 4, 1, 1, 5);
		checkNext(scanner, BOOLEAN_LITERAL, 5, 5, 1, 6);

		checkNextIsEOF(scanner);
		
		System.out.println("End testKeywords() \n");
	}
	

	//@Test
	public void testComments() throws LexicalException {
		System.out.println("Start testComments()");
		

		String input = "/**//*/";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_TIMES, 4, 1, 1, 5);
		checkNext(scanner, OP_DIV, 5, 1, 1, 6);
		checkNextIsEOF(scanner);
		
		System.out.println("End testComments() \n");
	}
	

	//@Test
	public void testInvalidComments() throws LexicalException {
		System.out.println("Start testInvalidComments()");
		
		String input = "/* abc***** ";
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(12,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
		
		System.out.println("End testInvalidComments() \n");
	}
	

	//@Test
	public void testIntegerLiterals() throws LexicalException {
		System.out.println("Start testIntegerLiterals()");
		
		String input = "9 8 9999\n 55>7";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 2, 1, 1, 3);
		checkNext(scanner, INTEGER_LITERAL, 4, 4, 1, 5);
		checkNext(scanner, INTEGER_LITERAL, 10, 2, 2, 2);
		checkNext(scanner, OP_GT, 12, 1, 2, 4);
		checkNext(scanner, INTEGER_LITERAL, 13, 1, 2, 5);

		checkNextIsEOF(scanner);
		
		System.out.println("End testIntegerLiterals() \n");
	}
	

	//@Test
	public void testFloatLiterals() throws LexicalException {
		System.out.println("Start testFLoatLiterals()");
		
		String input = "0.99";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 4, 1, 1);
		
		checkNextIsEOF(scanner);
		
		System.out.println("End testFLoatLiterals() \n");
	}

	//@Test
	public void Identifers() throws LexicalException {
		System.out.println("Start Identifiers()");
		
		String input = "1hub$:";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, IDENTIFIER, 1, 4, 1, 2);
		checkNext(scanner, OP_COLON, 5, 1, 1, 6);

		checkNextIsEOF(scanner);
		
		System.out.println("End Identifiers() \n");
	}

	//@Test
	public void Identifers2() throws LexicalException {
		System.out.println("Start Identifers2()");
		
		String input = "abc;def$\n:";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 3, 1, 1);
		checkNext(scanner, SEMI, 3, 1, 1, 4);
		checkNext(scanner, IDENTIFIER, 4, 4, 1, 5);
		checkNext(scanner, OP_COLON, 9, 1, 2, 1);
		checkNextIsEOF(scanner);
		
		System.out.println("End Identifers2() \n");
	}
	
	

	//@Test
	public void failIllegalChar() throws LexicalException {
		System.out.println("Start failIllegalChar()");
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
		System.out.println("End failIllegalChar() \n");
	}




	//@Test
	public void testParens() throws LexicalException {
		System.out.println("Start testParens()");
		String input = "()";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LPAREN, 0, 1, 1, 1);
		checkNext(scanner, RPAREN, 1, 1, 1, 2);
		checkNextIsEOF(scanner);
		System.out.println("End testParens() \n");
	}
	

	//@Test
	public void testDotFloats() throws LexicalException {
		System.out.println("Start testDotFloats()");
		String input = "0.";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 2, 1, 1);
		checkNextIsEOF(scanner);
		System.out.println("End testDotFloats() \n");
	}

	//@Test
	public void testPixels() throws LexicalException {
		System.out.println("Start testPixels()");
		String input = "<<>>";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LPIXEL, 0, 2, 1, 1);
		checkNext(scanner, RPIXEL, 2, 2, 1, 3);
		checkNextIsEOF(scanner);
		System.out.println("End testPixels() \n");
	}

	//@Test
	public void testExpression() throws LexicalException {
		System.out.println("Start testExpression()");
		String input = "([2+3]/5.0)";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LPAREN, 0, 1, 1, 1);
		checkNext(scanner, LSQUARE, 1, 1, 1, 2);
		checkNext(scanner, INTEGER_LITERAL, 2, 1, 1, 3);
		checkNext(scanner, OP_PLUS, 3, 1, 1, 4);
		checkNext(scanner, INTEGER_LITERAL, 4, 1, 1, 5);
		checkNext(scanner, RSQUARE, 5, 1, 1, 6);
		checkNext(scanner, OP_DIV, 6, 1, 1, 7);
		checkNext(scanner, FLOAT_LITERAL, 7, 3, 1, 8);
		checkNext(scanner, RPAREN, 10, 1, 1, 11);
		checkNextIsEOF(scanner);
		System.out.println("End testExpression() \n");
	}	

	
	
	//@Test
	public void failIllegalInteger() throws LexicalException {
		System.out.println("Start failIllegalInteger()");
		String input = "22312321312313123131313212312312312312313131313231432424285748957847487";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(0,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
		System.out.println("End failIllegalInteger() \n");
	}
	

	//@Test
	public void testCode() throws LexicalException {
		System.out.println("Start testCode()");
		
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_if, 0, 2, 1, 1);
		checkNext(scanner, LPAREN, 2, 1, 1, 3);
		checkNext(scanner, FLOAT_LITERAL, 3, 3, 1, 4);
		checkNext(scanner, OP_LE, 6, 2, 1, 7);
		checkNext(scanner, INTEGER_LITERAL, 8, 1, 1, 9);
		checkNext(scanner, RPAREN, 9, 1, 1, 10);
		checkNext(scanner, LBRACE, 10, 1, 1, 11);
		checkNext(scanner, KW_float, 24, 5, 1, 25);
		checkNext(scanner, IDENTIFIER, 30, 3, 1, 31);
		checkNext(scanner, OP_ASSIGN, 33, 2, 1, 34);
		checkNext(scanner, INTEGER_LITERAL, 36, 1, 1, 37);
		checkNext(scanner, SEMI, 37, 1, 1, 38);
		checkNext(scanner, RBRACE, 38, 1, 1, 39);
		checkNextIsEOF(scanner);
		System.out.println("End testCode() \n");
	}	
	

	
	//@Test
	public void failDoubleDiv() throws LexicalException {
		System.out.println("Start failDoubleDiv()");
		String input = "//";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(0,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
		System.out.println("End failDoubleDiv() \n");
	}
	*/
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it an illegal float
	 */
	/*
	//@Test
	public void failIllegalFLoat() throws LexicalException {
		System.out.println("Start failIllegalFLoat()");
		String input = "7000000000000000000000000000000000000000000000000000.4028235";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(0,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
		System.out.println("End failIllegalFLoat() \n");
	}
	
	//@Test
	public void failIllegalChar2() throws LexicalException {
		System.out.println("Start failIllegalChar2()");
		String input = "_123";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(0,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
		System.out.println("End failIllegalChar2() \n");
	}
	
	//@Test
	public void failSingleEq() throws LexicalException {
		System.out.println("Start failSingleEq()");
		String input = "abc=";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(3,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
		System.out.println("End failSingleEq() \n");
	}
	/*
	 * Testing failed with input: "$i". 
	 * Expected test to throw an instance of cop5556sp18.Scanner$LexicalException
	 */
	//@Test
	/*
	public void fail_$i() throws LexicalException {
		System.out.println("Start fail_$i()");
		String input = "$i";
		show(input);
		
		
		
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(0,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
		

		System.out.println("End fail_$i() \n");
	}
	
	/*
	 * Testing failed with input: "_i". 
	 * Expected test to throw an instance of cop5556sp18.Scanner$LexicalException
	 */
	//@Test
	/*
	public void fail_i() throws LexicalException {
		System.out.println("Start fail_$i()");
		String input = "_i";
		show(input);
		
		
		
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(0,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
		

		System.out.println("End fail_i() \n");
	}
	*/
}
	

