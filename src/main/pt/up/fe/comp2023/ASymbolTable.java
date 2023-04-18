package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASymbolTable implements SymbolTable {
    public List<String> imports = new ArrayList<>();
    public String className = null;
    public String superClass = null;
    public List<Symbol> fields=new ArrayList<>();
    public List<String> methods=new ArrayList<>();
    protected Map<String, Type> typeret =new HashMap<>();
    protected Map<String, List<Symbol>> methparams =new HashMap<>();
    protected Map<String, List<Symbol>> methvars =new HashMap<>();

    @Override
    public List<String> getImports() {
        return imports;
    }

    @Override
    public String getClassName() {
        return className;
    }
    @Override
    public String getSuper() {
        return superClass;
    }

    @Override
    public List<Symbol> getFields() {
        return fields;
    }

    @Override
    public List<String> getMethods() {
        return methods;
    }

    @Override
    public Type getReturnType(String methodSignature) {
        return typeret.get(methodSignature);
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return methparams.get(methodSignature);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return methvars.get(methodSignature);
    }
}
