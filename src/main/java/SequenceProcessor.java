import java.io.*;
import java.util.*;

import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.*;
import net.sourceforge.plantuml.SourceStringReader;

public class SequenceProcessor {
    HashMap<String, String> mapMethodClass;
    ArrayList<CompilationUnit> compilationUnitArray;
    HashMap<String, ArrayList<MethodCallExpr>> mapMethodCalls;
    String plantUMLCode;
    final String inputPath, outputPath, inputFunctionName, inputClassName;

    SequenceProcessor(String inputPath, String inputClassName, String inputFunctionName,
                      String outFile) {
        this.inputPath = inputPath;
        this.outputPath = outFile + ".png";
        this.inputClassName = inputClassName;
        this.inputFunctionName = inputFunctionName;
        mapMethodClass = new HashMap<String, String>();
        mapMethodCalls = new HashMap<String, ArrayList<MethodCallExpr>>();
        plantUMLCode = "@startuml\n";
    }

    public void start() throws Exception {
        compilationUnitArray = getcompilationUnitArray(inputPath);
        makeAllMaps();
        plantUMLCode += "actor user #black\n";
        plantUMLCode += "user" + " -> " + inputClassName + " : " + inputFunctionName + "\n";
        plantUMLCode += "activate " + mapMethodClass.get(inputFunctionName) + "\n";
        parse(inputFunctionName);
        plantUMLCode += "@enduml";
        drawDiagram(plantUMLCode);
        System.out.println("Plant UML Code:\n" + plantUMLCode);
    }

    private void parse(String callerFunc) {

        for (MethodCallExpr expr : mapMethodCalls.get(callerFunc)) {
            String callerClass = mapMethodClass.get(callerFunc);
            String calleeFunction = expr.getName().toString();
            String calleeClass = mapMethodClass.get(calleeFunction);
            if (mapMethodClass.containsKey(calleeFunction)) {
                plantUMLCode += callerClass + " -> " + calleeClass + " : "
                        + expr.toString() + "\n";
                plantUMLCode += "activate " + calleeClass + "\n";
                parse(calleeFunction);
                plantUMLCode += calleeClass + " -->> " + callerClass + "\n";
                plantUMLCode += "deactivate " + calleeClass + "\n";
            }
        }
    }

    private void makeAllMaps() {
        for (CompilationUnit cu : compilationUnitArray) {
            String className = "";
            List<TypeDeclaration<?>> td = cu.getTypes();
            for (Node n : td) {
                ClassOrInterfaceDeclaration coi = (ClassOrInterfaceDeclaration) n;
                className = coi.getName().toString();
                for (Object o : ((TypeDeclaration) coi)
                        .getMembers()) {
                    BodyDeclaration<?> bd = (BodyDeclaration<?>) o;
                    if (bd instanceof MethodDeclaration) {
                        MethodDeclaration md = (MethodDeclaration) bd;
                        ArrayList<MethodCallExpr> mcea = new ArrayList<MethodCallExpr>();
                        for (Object bs : md.getChildNodes()) {
                            if (bs instanceof BlockStmt) {
                                for (Object es : ((Node) bs)
                                        .getChildNodes()) {
                                    if (es instanceof ExpressionStmt) {
                                        if (((ExpressionStmt) (es))
                                                .getExpression() instanceof MethodCallExpr) {
                                            mcea.add(
                                                    (MethodCallExpr) (((ExpressionStmt) (es))
                                                            .getExpression()));
                                        }
                                    }
                                }
                            }
                        }
                        mapMethodCalls.put(md.getName().toString(), mcea);
                        mapMethodClass.put(md.getName().toString(), className);
                    }
                }
            }
        }
        //printMaps();
    }

    private ArrayList<CompilationUnit> getcompilationUnitArray(String inputPath)
            throws Exception {
        File folder = new File(inputPath);
        ArrayList<CompilationUnit> compilationUnitArray = new ArrayList<CompilationUnit>();
        for (final File f : folder.listFiles()) {
            if (f.isFile() && f.getName().endsWith(".java")) {
                FileInputStream in = new FileInputStream(f);
                CompilationUnit cu;
                try {
                    cu = JavaParser.parse(in);
                    compilationUnitArray.add(cu);
                } finally {
                    in.close();
                }
            }
        }
        return compilationUnitArray;
    }

    private String drawDiagram(String source) throws IOException {
        OutputStream img = new FileOutputStream(outputPath);
        SourceStringReader reader = new SourceStringReader(source);
        String imgStr = reader.generateImage(img);
        return imgStr;
    }

    @SuppressWarnings("unused")
    private void printMaps() {
        System.out.println("mapMethodCalls:");
        Set<String> keys = mapMethodCalls.keySet(); // get all keys
        for (String i : keys) {
            System.out.println(i + "->" + mapMethodCalls.get(i));
        }
        System.out.println("---");
        keys = null;

        System.out.println("mapMethodClass:");
        keys = mapMethodClass.keySet(); // get all keys
        for (String i : keys) {
            System.out.println(i + "->" + mapMethodClass.get(i));
        }
        System.out.println("---");
    }

}
