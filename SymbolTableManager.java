package cop5556sp18;

import cop5556sp18.AST.Declaration;
import java.util.*;



public class SymbolTableManager {
	//Stack for scopes and IDs
	private Stack<Integer> scope_stack = new Stack<Integer>();
	
	//Hashmap to store key value pairs
	//Wrapper object stores scopeNum and the attributes of symbol
	private HashMap<String, ArrayList<Wrapper>> symbolTable = new HashMap<String, ArrayList<Wrapper>>();
	
	//variable to hold current scope id
	private int currentScope;
	
	//variable to hold next scope id
	private int nextScope;
	
	public SymbolTableManager() {
		this.currentScope = 0;
		this.nextScope = 1;
		this.scope_stack.push(0);
	}
	
	//enter a new scope
	void enterScope() {
		this.currentScope = this.nextScope++;
		this.scope_stack.push(this.currentScope);
		System.out.println("currentScope");
		System.out.println(this.currentScope);
	}
	
	//exit the current scope
	void exitScope() {
		
		int value = this.scope_stack.pop();
		if(!scope_stack.empty())	this.currentScope = scope_stack.peek();
		else this.currentScope = value;
	}
	
	//inserts the token in the HashMap
	public boolean insert(String identifier, Declaration dec) {
		
		ArrayList<Wrapper> list = new ArrayList<Wrapper>();
		Wrapper symbol = new Wrapper(this.currentScope, dec);
		
		//if identifier name already present
		if(this.symbolTable.containsKey(identifier)) {
			System.out.println("YOOOOO alredy present");
			list = this.symbolTable.get(identifier);
			
			for(Wrapper it: list) {
				if(it.getScopeNum() == this.currentScope) {
					System.out.println("Same scope");
					return false;
				}
			}
			
		}
		
		list.add(symbol);
		this.symbolTable.put(identifier, list);
		
		System.out.println(identifier + "inserted");
		return true;
	}
	
	//lookup a certain key in the symbol table for declaration
	public Declaration lookupdec(String identifier) {
		Declaration dec = null;
		
		ArrayList<Wrapper> list = this.symbolTable.get(identifier);
		if(list != null) {
			//find if any wrapper object has scope in the scope stack
			for(Wrapper it: list) {
				int wrScope = it.getScopeNum();
								
				if(wrScope == this.currentScope) {
					dec = it.getDeclaration();
					break;
				}

			}
		}
		
		return dec;
	}
	
	//lookup a certain key in the symbol table
	public Declaration lookup(String identifier) {
		Declaration dec = null;
		
		ArrayList<Wrapper> list = this.symbolTable.get(identifier);
		ListIterator<Integer> it = this.scope_stack.listIterator(this.scope_stack.size());
		
		while(it.hasPrevious()) {
			int symbolScope = it.previous();
			for(int i = 0;i < list.size(); i++) {
				Wrapper wr = list.get(i);
				dec = wr.dec;
				if(wr.getScopeNum() == symbolScope) {
					return dec;
				}
				
			}
		}
		
		return dec;
	}
	

	
	//peek a certain key in the symbol table
		public Declaration peek(String identifier) {
			Declaration dec = null;
			
			ArrayList<Wrapper> list = this.symbolTable.get(identifier);
			System.out.println("Peeking");
			
			System.out.println("Scope Stack Scope");
			System.out.println(this.scope_stack.peek());
			if(list != null) {
				//find if any wrapper object has scope in the scope stack
				for(Wrapper it: list) {
					int wrScope = it.getScopeNum();
					dec = it.getDeclaration();
					
					System.out.println("Identifier");
					System.out.println(wrScope);
					System.out.println(dec);
					
					
					
				}
			}
		    
			System.out.println("End peeking");
			
			return dec;
		}
	
	
	
	
	public class Wrapper{
		int scopeNum;
		Declaration dec;
		
		public Wrapper(int scope, Declaration dec) {
			this.scopeNum = scope;
			this.dec = dec;
		}
		
		public int getScopeNum() {
			return this.scopeNum;
		}
		
		public Declaration getDeclaration() {
			return this.dec;
		}
	}
	
	
	
	
}
