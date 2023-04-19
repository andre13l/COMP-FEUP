package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.ollir.optimizations.MarkUncertainVariables;
import pt.up.fe.comp.semantic.Constants;
import pt.up.fe.comp.semantic.models.ExtendedSymbol;
import pt.up.fe.comp.semantic.models.Origin;
import pt.up.fe.comp.semantic.symbol_table.SymbolTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<String, List<String>> {
    private final StringBuilder code = new StringBuilder();
    private final SymbolTable symbolTable;

    OllirGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("MainMethodDeclaration", this::visitMainMethod);
        addVisit("MethodDeclaration", this::visitMethod);
        addVisit("MethodBody", this::visitMethodBody);
        addVisit("FnCallOp", this::visitFnCallOp);
        addVisit("VarName", this::visitVarName);
        addVisit("IntegerLiteral", (node, dummy) -> Arrays.asList(String.format("%s.%s", node.get("image"), "i32"), "Constant", node.get("image")));
        addVisit("False", (node, dummy) -> Arrays.asList("0" + ".bool", "Constant", "0"));
        addVisit("True", (node, dummy) -> Arrays.asList("1" + ".bool", "Constant", "1"));
        addVisit("BinOp", this::visitBinOp);
        addVisit("UnaryOp", this::visitUnaryOp);
        addVisit("CreateObj", this::visitCreateObj);
        addVisit("CreateArrObj", this::visitCreateArrObj);
        addVisit("Variable", this::visitVariable);
        addVisit("ArrAccess", this::visitArrAccess);
        addVisit("ReturnStatement", this::visitReturnStatement);
        addVisit("MethodArgsList", this::visitMethodArgsList);
        addVisit("AsmOp", this::visitAsmOp);
        addVisit("WhileStm", this::visitWhile);
        addVisit("IfStm", this::visitIfStm);
        addVisit("Length", this::visitLength);
        setDefaultVisit((node, dummy) -> null);
    }
}