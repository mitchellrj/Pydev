/*
 * Created on Jan 19, 2005
 *
 * @author Fabio Zadrozny
 */
package org.python.pydev.editor.codecompletion.revisited.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.python.parser.SimpleNode;
import org.python.parser.ast.Assign;
import org.python.parser.ast.ClassDef;
import org.python.parser.ast.FunctionDef;
import org.python.parser.ast.Import;
import org.python.parser.ast.ImportFrom;
import org.python.parser.ast.NameTok;
import org.python.parser.ast.aliasType;
import org.python.pydev.core.IModule;
import org.python.pydev.core.IToken;
import org.python.pydev.parser.visitors.NodeUtils;

/**
 * @author Fabio Zadrozny
 */
public class FindDefinitionModelVisitor extends AbstractVisitor{

    /**
     * This is the token to find.
     */
    private String tokenToFind;
    
    /**
     * List of definitions.
     */
    public List<Definition> definitions = new ArrayList<Definition>();
    
    /**
     * Stack of classes / methods to get to a definition.
     */
    private Stack<SimpleNode> defsStack = new Stack<SimpleNode>();
    
    /**
     * This is the module we are visiting
     */
    private IModule module;
    
    /**
     * It is only available if the cursor position is upon a NameTok in an import (it represents the complete
     * path for finding the module from the current module -- it can be a regular or relative import).
     */
    public String moduleImported;

	private int line;

	private int col;
    
    /**
     * Constructor
     */
    public FindDefinitionModelVisitor(String token, int line, int col, IModule module){
        this.tokenToFind = token;
        this.module = module;
        this.line = line;
        this.col = col;
        this.moduleName = module.getName();
    }
    
    @Override
    public Object visitImportFrom(ImportFrom node) throws Exception {
    	String modRep = NodeUtils.getRepresentationString(node.module);
		if( NodeUtils.isWithin(line, col, node.module) ){
    		//it is a token in the definition of a module
    		int startingCol = node.module.beginColumn;
			int endingCol = startingCol;
    		while(endingCol < this.col){
    			endingCol++;
    		}
    		int lastChar = endingCol-startingCol;
    		moduleImported = modRep.substring(0, lastChar);
    		int i = lastChar;
    		while(i < modRep.length()){
    			if(Character.isJavaIdentifierPart(modRep.charAt(i))){
    				i++;
    			}else{
    				break;
    			}
    		}
    		moduleImported += modRep.substring(lastChar, i);
    	}else{
    		//it was not the module, so, we have to check for each name alias imported
    		for (aliasType alias: node.names){
    			//we do not check the 'as' because if it is some 'as', it will be gotten as a global in the module
    			if( NodeUtils.isWithin(line, col, alias.name) ){
    				moduleImported = modRep + "." + 
    							     NodeUtils.getRepresentationString(alias.name);
    			}
    		}
    	}
    	return super.visitImportFrom(node);
    }

    /**
     * @see org.python.parser.ast.VisitorBase#unhandled_node(org.python.parser.SimpleNode)
     */
    protected Object unhandled_node(SimpleNode node) throws Exception {
        return null;
    }

    /**
     * @see org.python.parser.ast.VisitorBase#traverse(org.python.parser.SimpleNode)
     */
    public void traverse(SimpleNode node) throws Exception {
        node.traverse(this);
    }
    
    /**
     * @see org.python.parser.ast.VisitorBase#visitClassDef(org.python.parser.ast.ClassDef)
     */
    public Object visitClassDef(ClassDef node) throws Exception {
        defsStack.push(node);
        node.traverse(this);
        defsStack.pop();
        return null;
    }
    
    /**
     * @see org.python.parser.ast.VisitorBase#visitFunctionDef(org.python.parser.ast.FunctionDef)
     */
    public Object visitFunctionDef(FunctionDef node) throws Exception {
        defsStack.push(node);
        node.traverse(this);
        defsStack.pop();
        return null;
    }
    
    /**
     * @see org.python.parser.ast.VisitorBase#visitAssign(org.python.parser.ast.Assign)
     */
    public Object visitAssign(Assign node) throws Exception {
        
        for (int i = 0; i < node.targets.length; i++) {
            String rep = NodeUtils.getFullRepresentationString(node.targets[i]);
	        
            if(rep != null && rep.equals(tokenToFind)){
	            String value = NodeUtils.getFullRepresentationString(node.value);
	            AssignDefinition definition;
	            int line = NodeUtils.getLineDefinition(node.value);
	            int col = NodeUtils.getColDefinition(node.value);
	            
	            if (node.targets != null && node.targets.length > 0){
	            	line = NodeUtils.getLineDefinition(node.targets[0]);
	            	col = NodeUtils.getColDefinition(node.targets[0]);
	            }

	            definition = new AssignDefinition(value, rep, i, node, line, col, new Scope(this.defsStack), module);
	            definitions.add(definition);
	        }
        }
        
        return null;
    }
}
