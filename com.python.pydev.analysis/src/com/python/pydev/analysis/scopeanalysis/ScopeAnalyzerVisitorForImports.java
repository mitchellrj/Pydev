package com.python.pydev.analysis.scopeanalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.python.pydev.core.IModule;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.IToken;
import org.python.pydev.core.Tuple3;
import org.python.pydev.core.Tuple4;
import org.python.pydev.parser.visitors.scope.ASTEntry;

import com.python.pydev.analysis.visitors.Found;
import com.python.pydev.analysis.visitors.ImportChecker.ImportInfo;

/**
 * This scope analyzer works finding definitions that are based on some import.
 */
public class ScopeAnalyzerVisitorForImports extends ScopeAnalyzerVisitor {

    private ImportInfo importInfo;

    /**
     * @param importInfo we'll try to find matches for the given import info.
     */
    public ScopeAnalyzerVisitorForImports(IPythonNature nature, String moduleName, IModule current, IProgressMonitor monitor, 
            String nameToFind, String[] tokenAndQual, ImportInfo importInfo) throws BadLocationException {
        super(nature, moduleName, current, null, monitor, nameToFind, -1, tokenAndQual);
        this.importInfo = importInfo;
    }

    @Override
    protected boolean checkToken(Found found, IToken generator, ASTEntry parent) {
        if (found == null) {
            return false;
        }

        if (found.importInfo != null && found.importInfo.wasResolved && found.importInfo.rep.equals(this.importInfo.rep)) {
            return true;
        }

        return false;
    }

    /**
     * All the occurrences we find are correct occurrences (because we check if it was found by the module it resolves to)
     */
    protected ArrayList<Tuple4<IToken, Integer, ASTEntry, Found>> getCompleteTokenOccurrences() {
        ArrayList<Tuple4<IToken, Integer, ASTEntry, Found>> ret = new ArrayList<Tuple4<IToken,Integer,ASTEntry,Found>>();
        
        addImports(ret, importsFound);
        addImports(ret, importsFoundFromModuleName);
        return ret;
    }

    @SuppressWarnings("unchecked")
    private void addImports(ArrayList<Tuple4<IToken, Integer, ASTEntry, Found>> ret, Map<String, List<Tuple3<Found, Integer, ASTEntry>>> map) {
        for(List<Tuple3<Found, Integer, ASTEntry>> fList:map.values()){
            for (Tuple3<Found, Integer, ASTEntry> foundInFromModule : fList) {
                IToken generator = foundInFromModule.o1.getSingle().generator;
                
                Tuple4<IToken, Integer, ASTEntry, Found> tup3 = new Tuple4(generator, 0, foundInFromModule.o3, foundInFromModule.o1);
                ret.add(tup3);
            }
        }
    }
}
