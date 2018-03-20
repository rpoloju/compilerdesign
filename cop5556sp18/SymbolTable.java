package cop5556sp18;

import java.util.Stack;
import cop5556sp18.AST.Declaration;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

	// Le blanc-Cook symbol table
	Map<String, Map<Integer, Declaration>> symTable = new HashMap<String, Map<Integer, Declaration>>();
	// Scope Stack
	Stack<Integer> scopeStack = new Stack<Integer>();

	int scope = 0;

	public void enterScope() {
		scopeStack.push(scope++);
	}

	public void leaveScope() {
		scopeStack.pop();
	}

	/*
	 * This method looks up whether the declaration is in scope, else returns null
	 */
	public Declaration getDeclaration(String identifier) {
		int x = 0;
		boolean isDecFound = false;
		Declaration declaration = null;
		Map<Integer, Declaration> actualScopeMap = symTable.get(identifier);
		if (actualScopeMap != null && !isDecFound) {

			// select from the stack the value nearer to the top of the stack whose scope
			// contains the identifier
			for (int i = scopeStack.size() - 1; i >= 0 && !isDecFound; i--) {

				// for each scope x, get the variables contained in the scope into
				// currentScopeMap
				x = scopeStack.get(i);
				if (actualScopeMap.containsKey(x)) {
					declaration = actualScopeMap.get(x);
					isDecFound = true;
				}

			}
		}
		return declaration;
	}
	
	public Declaration getDeclarationinCurrentScope(String identifier) {
		int x = 0;
		Declaration declaration = null;
		Map<Integer, Declaration> actualScopeMap = symTable.get(identifier);
		
		if (actualScopeMap != null) {
			x = scopeStack.get(scopeStack.size() - 1);
			
			if (actualScopeMap.containsKey(x)) {
				declaration = actualScopeMap.get(x);
			}
		}
		return declaration;
	}

	public boolean addToMap(String identifier, Declaration declaration) {
		int topOfStack = scopeStack.peek();
		Map<Integer, Declaration> newDeclarationMap;

		// If the symbol table has no such identifier, simply add
		if (!symTable.containsKey(identifier)) {
			newDeclarationMap = new HashMap<Integer, Declaration>();
			newDeclarationMap.put(topOfStack, declaration);
			symTable.put(identifier, newDeclarationMap);
			return true;
		}
		newDeclarationMap = symTable.get(identifier);

		// If there is already a declaration of same variable in the current scope,
		// re-declaration of same variable is not allowed
		if (newDeclarationMap.containsKey(topOfStack)) {
			return false;
		}

		newDeclarationMap.put(topOfStack, declaration);

		// Replace the old map with newDeclarationMap in the symbolTable containing
		// previous entries and the new entry
		symTable.put(identifier, newDeclarationMap);
		return true;
	}

	public void symbolTable() {
		scopeStack.push(scope++);
	}

}
