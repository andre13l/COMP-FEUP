package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
public interface JmmOptimization {
    OllirResult toOllir(JmmSemanticsResult semanticsResult);
}