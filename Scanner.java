/**
* Initial code for the Scanner for the class project in COP5556 Programming Language Principles 
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

import java.util.ArrayList;
import java.util.Arrays;
//import java.util.HashMap;
import java.util.HashMap;

public class Scanner {

	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {

		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}

		public int getPos() {
			return pos;
		}
	}

	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL,
		KW_Z/* Z */, KW_default_width/* default_width */, KW_default_height/* default_height */, 
		KW_width /* width */, KW_height /* height*/, KW_show/*show*/, KW_write /* write */, KW_to /* to */,
		KW_input /* input */, KW_from /* from */, KW_cart_x/* cart_x*/, KW_cart_y/* cart_y */, 
		KW_polar_a/* polar_a*/, KW_polar_r/* polar_r*/, KW_abs/* abs */, KW_sin/* sin*/, KW_cos/* cos */, 
		KW_atan/* atan */, KW_log/* log */, KW_image/* image */, KW_int/* int */, KW_float /* float */, 
		KW_boolean/* boolean */, KW_filename/* filename */, KW_red /* red */, KW_blue /* blue */, 
		KW_green /* green */, KW_alpha /* alpha*/, KW_while /* while */, KW_if /* if */, OP_ASSIGN/* := */, 
		OP_EXCLAMATION/* ! */, OP_QUESTION/* ? */, OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, 
		OP_GE/* >= */, OP_LE/* <= */, OP_GT/* > */, OP_LT/* < */, OP_AND/* & */, OP_OR/* | */, 
		OP_PLUS/* +*/, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, OP_POWER/* ** */, 
		OP_AT/* @ */, LPAREN/*( */, RPAREN/* ) */, LSQUARE/* [ */, RSQUARE/* ] */, LBRACE /*{ */, 
		RBRACE /* } */, LPIXEL /* << */, RPIXEL /* >> */, SEMI/* ; */, COMMA/* , */, DOT /* . */, EOF, 
		KW_sleep;
	}

	/**
	 * Class to represent Tokens.
	 * 
	 * This is defined as a (non-static) inner class which means that each Token
	 * instance is associated with a specific Scanner instance. We use this when
	 * some token methods access the chars array in the associated Scanner.
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos; // position of first character of this token in the input. Counting starts at 0
								// and is incremented for every character.
		public final int length; // number of characters in this token

		public Token(Kind kind, int pos, int length) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		public String getText() {
			return String.copyValueOf(chars, pos, length);
		}

		/**
		 * precondition: This Token's kind is INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		/**
		 * precondition: This Token's kind is FLOAT_LITERAL]
		 * 
		 * @returns the float value represented by the token
		 */
		public float floatVal() {
			assert kind == Kind.FLOAT_LITERAL;
			return Float.valueOf(String.copyValueOf(chars, pos, length));
		}

		/**
		 * precondition: This Token's kind is BOOLEAN_LITERAL
		 * 
		 * @returns the boolean value represented by the token
		 */
		public boolean booleanVal() {
			assert kind == Kind.BOOLEAN_LITERAL;
			return getText().equals("true");
		}

		/**
		 * Calculates and returns the line on which this token resides. The first line
		 * in the source code is line 1.
		 * 
		 * @return line number of this Token in the input.
		 */
		public int line() {
			return Scanner.this.line(pos) + 1;
		}

		/**
		 * Returns position in line of this token.
		 * 
		 * @param line.
		 *            The line number (starting at 1) for this token, i.e. the value
		 *            returned from Token.line()
		 * @return
		 */
		public int posInLine(int line) {
			return Scanner.this.posInLine(pos, line - 1) + 1;
		}

		/**
		 * Returns the position in the line of this Token in the input. Characters start
		 * counting at 1. Line termination characters belong to the preceding line.
		 * 
		 * @return
		 */
		public int posInLine() {
			return Scanner.this.posInLine(pos) + 1;
		}

		public String toString() {
			int line = line();
			return "[" + kind + "," + String.copyValueOf(chars, pos, length) + "," + pos + "," + length + "," + line
					+ "," + posInLine(line) + "]";
		}

		/**
		 * Since we override equals, we need to override hashCode, too.
		 * 
		 * See
		 * https://docs.oracle.com/javase/9/docs/api/java/lang/Object.html#hashCode--
		 * where it says, "If two objects are equal according to the equals(Object)
		 * method, then calling the hashCode method on each of the two objects must
		 * produce the same integer result."
		 * 
		 * This method, along with equals, was generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + pos;
			return result;
		}

		/**
		 * Override equals so that two Tokens are equal if they have the same Kind, pos,
		 * and length.
		 * 
		 * This method, along with hashcode, was generated by eclipse.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (pos != other.pos)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is associated with.
		 * 
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}// Token

	/**
	 * Array of positions of beginning of lines. lineStarts[k] is the pos of the
	 * first character in line k (starting at 0).
	 * 
	 * If the input is empty, the chars array will have one element, the synthetic
	 * EOFChar token and lineStarts will have size 1 with lineStarts[0] = 0;
	 */
	int[] lineStarts;

	int[] initLineStarts() {
		ArrayList<Integer> lineStarts = new ArrayList<Integer>();
		int pos = 0;

		for (pos = 0; pos < chars.length; pos++) {
			lineStarts.add(pos);
			char ch = chars[pos];
			while (ch != EOFChar && ch != '\n' && ch != '\r') {
				pos++;
				ch = chars[pos];
			}
			if (ch == '\r' && chars[pos + 1] == '\n') {
				pos++;
			}
		}
		// convert arrayList<Integer> to int[]
		return lineStarts.stream().mapToInt(Integer::valueOf).toArray();
	}

	int line(int pos) {
		int line = Arrays.binarySearch(lineStarts, pos);
		if (line < 0) {
			line = -line - 2;
		}
		return line;
	}

	public int posInLine(int pos, int line) {
		return pos - lineStarts[line];
	}

	public int posInLine(int pos) {
		int line = line(pos);
		return posInLine(pos, line);
	}

	/**
	 * Sentinal character added to the end of the input characters.
	 */
	static final char EOFChar = 128;

	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;

	/**
	 * An array of characters representing the input. These are the characters from
	 * the input string plus an additional EOFchar at the end.
	 */
	final char[] chars;

	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	
	//Declare the variable to store keywords
	HashMap<String,Kind> keywords = new HashMap<String,Kind>();
	
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFChar;
		tokens = new ArrayList<Token>();
		lineStarts = initLineStarts();
	}




	 private enum State {
		 START, AFTER_GT, AFTER_LT, AFTER_EQ, AFTER_TIMES, AFTER_ASSIGN, AFTER_EXCLAMATION, AFTER_COLON, 
		 IS_DIGIT, IS_IDENTIFIER, AFTER_DIV, BEGIN_COMMENT, END_COMMENT, IS_INTEGER, IS_FLOAT,
		 DOUBLE_GT, DOUBLE_LT
		 };  //TODO:  this is incomplete
		 
	public void AddKeywords()
		{
			keywords.put("Z", Kind.KW_Z);
			keywords.put("default_width", Kind.KW_default_width);
			keywords.put("default_height", Kind.KW_default_height);
			keywords.put("show", Kind.KW_show);
			keywords.put("write", Kind.KW_write);
			keywords.put("to", Kind.KW_to);
			keywords.put("input", Kind.KW_input);
			keywords.put("from", Kind.KW_from);
			keywords.put("cart_x",Kind.KW_cart_x);
			keywords.put("cart_y", Kind.KW_cart_y);
			keywords.put("polar_a", Kind.KW_polar_a);
			keywords.put("polar_r", Kind.KW_polar_r);
			keywords.put("abs", Kind.KW_abs);
			keywords.put("sin", Kind.KW_sin);
			keywords.put("cos", Kind.KW_cos);
			keywords.put("atan", Kind.KW_atan);
			keywords.put("log", Kind.KW_log);
			keywords.put("image", Kind.KW_image);
			keywords.put("int", Kind.KW_int);
			keywords.put("float", Kind.KW_float);
			keywords.put("filename", Kind.KW_filename);
			keywords.put("boolean", Kind.KW_boolean);
			keywords.put("red", Kind.KW_red);
			keywords.put("blue", Kind.KW_blue);
			keywords.put("green", Kind.KW_green);
			keywords.put("alpha", Kind.KW_alpha);
			keywords.put("while", Kind.KW_while);
			keywords.put("if", Kind.KW_if);
			keywords.put("width", Kind.KW_width);
			keywords.put("height", Kind.KW_height);
			keywords.put("sleep", Kind.KW_sleep);
			}

	 
	 //TODO: Modify this to deal with the entire lexical specification
	public Scanner scan() throws LexicalException {
		
		int pos = 0;
		State state = State.START;
		int startPos = 0;
		
		//add the keywords to the HashMap
		AddKeywords();
		
		while (pos < chars.length) {
			char ch = chars[pos];
			switch(state) {
				case START: {
					startPos = pos;
					switch (ch) {
						case ' ':
						case '\n':
						case '\r':
						case '\t':
						case '\f': {
							pos++;
						}
						break;
						case EOFChar: {
							tokens.add(new Token(Kind.EOF, startPos, 0));
							pos++; // next iteration will terminate loop
						}
						break;

						case '>': {
							if(chars[pos+1] == '=') {
								// ">="
								state = State.AFTER_GT;
							}
							else if(chars[pos+1] == '>') {
								state = State.DOUBLE_GT;
							}
							else {
								tokens.add(new Token(Kind.OP_GT, startPos, pos - startPos + 1));
							}
							pos++;
						} 
						break;
						case '<': {
							if(chars[pos+1] == '=') {
								// "<="
								state = State.AFTER_LT;
							}
							else if(chars[pos+1] == '<') {
								state = State.DOUBLE_LT;
							}
							else {
								tokens.add(new Token(Kind.OP_LT, startPos, pos - startPos + 1));
							}
							pos++;
						}
						break;
						case '!': {
							if(chars[pos+1] == '=') {
								// "<="
								state = State.AFTER_EXCLAMATION;
								pos++;
							}
							else {
								tokens.add(new Token(Kind.OP_EXCLAMATION, startPos, pos - startPos + 1));
								pos++;
							}
						}
						break;
						case '?': {
							tokens.add(new Token(Kind.OP_QUESTION, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '=': {
							if(chars[pos+1] == '=') {
								// "<="
								state = State.AFTER_EQ;
								pos++;
							}
							else {
								throw new LexicalException("Illegal Character `=` ", pos);
							}
							/*
							else {
								tokens.add(new Token(Kind.OP_ASSIGN, startPos, pos - startPos + 1));
								pos++;
							}
							*/
						}
						break;
						case '&': {
							tokens.add(new Token(Kind.OP_AND, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '|': {
							tokens.add(new Token(Kind.OP_OR, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '+': {
							tokens.add(new Token(Kind.OP_PLUS, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '-': {
							tokens.add(new Token(Kind.OP_MINUS, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '*': {
							if(chars[pos+1] == '*') {
								// "<="
								state = State.AFTER_TIMES;
								pos++;
							}
							else {
								tokens.add(new Token(Kind.OP_TIMES, startPos, pos - startPos + 1));
								pos++;
							}
						}
						break;
						case '/': {
							//check if it might be a comment
							if(chars[pos+1] == '*') {
								state = State.AFTER_DIV;
								pos++;
							}
							else if(chars[pos+1] == '/') {
								error(pos, line(pos), posInLine(pos), "illegal double div(//)");
							}
							else {
							//not a comment, just a div operator
							tokens.add(new Token(Kind.OP_DIV, startPos, pos - startPos + 1));
							pos++;
							}
						}
						break;
						case '%': {
							tokens.add(new Token(Kind.OP_MOD, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case ':': {
							if(chars[pos+1] == '=') {
								// "<="
								state = State.AFTER_COLON;
								pos++;
							}
							else {
								tokens.add(new Token(Kind.OP_COLON, startPos, pos - startPos + 1));
								pos++;
							}
						}
						break;
						case '@': {
							tokens.add(new Token(Kind.OP_AT, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						
						case '(': {
							tokens.add(new Token(Kind.LPAREN, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						
						case ')': {
							tokens.add(new Token(Kind.RPAREN, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						
						case '[': {
							tokens.add(new Token(Kind.LSQUARE, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						
						case ']': {
							tokens.add(new Token(Kind.RSQUARE, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						
						case ';': {
							tokens.add(new Token(Kind.SEMI, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						
						case ',': {
							tokens.add(new Token(Kind.COMMA, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						
						case '{': {
							tokens.add(new Token(Kind.LBRACE, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						
						case '}': {
							tokens.add(new Token(Kind.RBRACE, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						
						case '.': {
							if(pos+1 < chars.length) {
								if(Character.isDigit(chars[pos+1])){
									state = State.IS_FLOAT;
								}
								else {
									tokens.add(new Token(Kind.DOT, startPos, pos - startPos + 1));
								}
								pos++;
							}
						}
						break;
						
						default: {
							//if character is a  digit
							if(Character.isDigit(ch)) {
								if(pos+1 < chars.length) {
									//first digit is 0 and next is not .
									if(chars[pos] == '0' && chars[pos+1] != '.') {
										tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, pos - startPos + 1));
									}
									//if first char is 0..9 and next char is .
									else {
									state = State.IS_DIGIT;
									}
									pos++;
								}
								else {
									if(chars[pos+1] != '.') {
									tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, pos - startPos + 1));
									pos++;	
									}
								}
							}
							else if(Character.isJavaIdentifierStart(ch)){	
								//if the character is one of Identifier Start Symbols
								if(chars[pos] == '$' || chars[pos] == '_') {
									if(pos+1<chars.length) {
										if(Character.isDigit(chars[pos+1]) || chars[pos+1] == 'i') {
											error(pos, line(pos), posInLine(pos), "illegal identifier");	
										}
									}
									else {
										error(pos, line(pos), posInLine(pos), "illegal char");
									}
								}
								pos++;
								state = State.IS_IDENTIFIER;
								
							}
							else {
								error(pos, line(pos), posInLine(pos), "illegal char");
							}							
						}
					}//switch ch
				}
				break;
				
				case DOUBLE_GT: {
					tokens.add(new Token(Kind.RPIXEL, startPos, pos - startPos + 1));
					state = State.START;
					pos++;
				}
				break;
				
				case DOUBLE_LT: {
					tokens.add(new Token(Kind.LPIXEL, startPos, pos - startPos + 1));	
					state = State.START;
					pos++;
				}
				break;
				
				case AFTER_DIV: {
					//deals with a potential comment
					if(chars[pos] == '*') {
						//beginning of a comment
						state = State.BEGIN_COMMENT;
					}
					else {
						state = State.START;
					}
					
					pos++;
				}
				break;
				
				case BEGIN_COMMENT: {
					if(chars[pos] == '*') {
						//could suggest end of comment
						if(chars[pos+1] == '/')
						{ // */ => declares the end of comment 
							
							//this is a valid comment
							//System.out.println("Valid Comment!");
							state = State.END_COMMENT;
						}
						else {
							// all other following characters
						}

					}
					else if(chars[pos] == EOFChar) {
						error(pos, line(pos), posInLine(pos), "Invalid Comment");
					}
					else {
						
					}
					pos++;
								
				}
				break;
				
				case END_COMMENT:{
					state = State.START;
					pos++;
				}
				break;
				
				case IS_IDENTIFIER:{
					//if previous char is an identifier start/part
					if(Character.isJavaIdentifierPart(ch) && chars[pos] != EOFChar) {
						pos++;
					}
					else {
						//the current char is not an identifier => extract the identifier preceding
						String identifier = new String(chars, startPos, pos-startPos);
						
						//check if the identifier is one of the keywords or a boolean literal.
						if(identifier.equals("true") || identifier.equals("false") ) {
							tokens.add(new Token(Kind.BOOLEAN_LITERAL, startPos, pos - startPos));
						}
						else if(keywords.containsKey(identifier)) {
							//if identifier is a keyword
							tokens.add(new Token(keywords.get(identifier), startPos, pos - startPos));	
						}
						else {
							//its an identifier
							tokens.add(new Token(Kind.IDENTIFIER, startPos, pos - startPos));
							
						}
					//revert back to original start state
					state = State.START;
					}
				}
				break;
				
				case IS_DIGIT: {
					//check if the current char is also a digit
					if(Character.isDigit(ch)) {
						//if current char is also a digit
						pos++;
					}
					// is an integer
					else if(chars[pos] != '.') {
						state = State.IS_INTEGER;
					}
					//is  a float value
					else if(chars[pos] == '.')  {
						state = State.IS_FLOAT;
						pos++;
					}
					else { 			
						state = State.START;
					}
				}
				break;
				
				case IS_INTEGER:{
					//extract the preceding integer
					int digit = 0;
					String digitString = new String(chars, startPos, pos - startPos);
					try {
						digit = Integer.parseInt(digitString);
					}
					catch(NumberFormatException e) {
						error(startPos, line(startPos), posInLine(startPos), "String Conversion Illegal Integer Value!");
						//throw new LexicalException("Illegal String Conversion of Integer Number at ", startPos);
					}
					if(digit <= Integer.MAX_VALUE && digit >= Integer.MIN_VALUE) {
						tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, pos - startPos));
						state = State.START;
					}
					else
					{	
						error(startPos, line(startPos), posInLine(startPos), "Illegal Integar Value!");
						//throw new LexicalException("Illegal Integar Value! Not in range ", startPos);
					}	
				}
				break;
				
				case IS_FLOAT:{
					if(Character.isDigit(ch)) {
						pos++;
					}
					else {
						//extract the preceding float
						float digit = 0;
						String digitString = new String(chars, startPos, pos - startPos);
						try {
							digit = Float.parseFloat(digitString);
						}
						catch(NumberFormatException e) {
							error(startPos, line(startPos), posInLine(startPos), "String Conversion Illegal Float Value!");
						}
						if(Float.isFinite(digit)) {
							tokens.add(new Token(Kind.FLOAT_LITERAL, startPos, pos - startPos));
							state = State.START;
						}
						else
						{
							error(startPos, line(startPos), posInLine(startPos), "Illegal Float Value!");
						}	
					}
				}
				break;
				
				case AFTER_GT: {
					// preceding character is `>`
					switch(ch) 
					{
						case '=': {
							
							tokens.add(new Token(Kind.OP_GE, startPos, pos - startPos + 1));
							pos++;
							state = State.START;

						}
						break;
						
						default: {
							state = State.START;
							//throw new LexicalException("Illegal Character after greater than `>` sign! ", pos);
						}
					}
				}
				break;
				
				case AFTER_LT: {
					// preceding character is `<`
					switch(ch) 
					{
						case '=': {
							
							tokens.add(new Token(Kind.OP_LE, startPos, pos - startPos + 1));
							pos++;
							state = State.START;

						}
						break;
						
						default: {
							state = State.START;
							//throw new LexicalException("Illegal Character after greater than `>` sign! ", pos);
						}
					}
				}
				break;
				
				case AFTER_EXCLAMATION: {
					// preceding character is `!`
					switch(ch) 
					{
						case '=': {
							
							tokens.add(new Token(Kind.OP_NEQ, startPos, pos - startPos + 1));
							pos++;
							state = State.START;

						}
						break;
						
						default: {
							state = State.START;
							//throw new LexicalException("Illegal Character after greater than `>` sign! ", pos);
						}
					}
				}
				break;
				
				case AFTER_COLON: {
					// preceding character is `:`
					switch(ch) 
					{
						case '=': {
							
							tokens.add(new Token(Kind.OP_ASSIGN, startPos, pos - startPos + 1));
							pos++;
							state = State.START;

						}
						break;
						
						default: {
							state = State.START;
							//throw new LexicalException("Illegal Character after greater than `>` sign! ", pos);
						}
					}
				}
				break;
				
				case AFTER_EQ: {
					// preceding character is `=`
					switch(ch) 
					{
						case '=': {
							
							tokens.add(new Token(Kind.OP_EQ, startPos, pos - startPos + 1));
							pos++;
							state = State.START;

						}
						break;
						
						default: {
							state = State.START;
							//throw new LexicalException("Illegal Character after greater than `>` sign! ", pos);
						}
					}
				}
				break;
				
				case AFTER_TIMES: {
					// preceding character is `=`
					switch(ch) 
					{
						case '*': {
							
							tokens.add(new Token(Kind.OP_POWER, startPos, pos - startPos + 1));
							pos++;
							state = State.START;

						}
						break;
						
						default: {
							state = State.START;
							//throw new LexicalException("Illegal Character after greater than `>` sign! ", pos);
						}
					}
				}
				break;
				
				default: {
					error(pos, 0, 0, "undefined state");
				}
			}// switch state
		} // while
			
		return this;
	}
	


	private void error(int pos, int line, int posInLine, String message) throws LexicalException {
		String m = (line + 1) + ":" + (posInLine + 1) + " " + message;
		throw new LexicalException(m, pos);
	}

	/**
	 * Returns true if the internal iterator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that the next
	 * call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}

	/**
	 * Returns the next Token, but does not update the internal iterator. This means
	 * that the next call to nextToken or peek will return the same Token as
	 * returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}

	/**
	 * Resets the internal iterator so that the next call to peek or nextToken will
	 * return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens and line starts
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		sb.append("Line starts:\n");
		for (int i = 0; i < lineStarts.length; i++) {
			sb.append(i).append(' ').append(lineStarts[i]).append('\n');
		}
		return sb.toString();
	}

}
