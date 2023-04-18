package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp2023.ASymbolTable;
import pt.up.fe.comp2023.Visitor;

import java.util.Collections;

public class AnalysisClass implements JmmAnalysis {

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        ASymbolTable symbolTable = new ASymbolTable();

        Visitor visitor = new Visitor(symbolTable);

        visitor.visit(parserResult.getRootNode(),null);

        return new JmmSemanticsResult(parserResult, symbolTable, Collections.emptyList());
    }
}
