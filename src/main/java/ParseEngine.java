import java.io.*;
import java.util.*;
import java.lang.*;
import java.lang.String;

import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import net.sourceforge.plantuml.StringUtils;

public class ParseEngine {
    final String inPath;
    final String outPath;
    HashMap<String, Boolean> map;
    HashMap<String, String> mapClassConn;
    String yumlCode;
    ArrayList<CompilationUnit> cuArray;

    ParseEngine(String inPath, String outFile) {
        this.inPath = inPath;
        this.outPath = outFile + ".png";
        map = new HashMap<String, Boolean>();
        mapClassConn = new HashMap<String, String>();
        yumlCode = "";
    }

    public void start() throws Exception {
        cuArray = getCuArray(inPath);
        buildMap(cuArray);
        for (CompilationUnit cu : cuArray)
            yumlCode += parser(cu);
        System.out.println("Unique Code: " + yumlCode);
        GenerateDiagram.generatePNG(yumlCode, outPath);
    }

    private String parser(CompilationUnit cu) {
        String result = "";
        String className = "";
        String classShortName = "";
        String methods = "";
        String fields = "";
        String additions = ",";

        ArrayList<String> makeFieldPublic = new ArrayList<String>();
        List<TypeDeclaration<?>> ltd = cu.getTypes();
        Node node = ltd.get(0); // assuming no nested classes

        // Get className
        ClassOrInterfaceDeclaration coi = (ClassOrInterfaceDeclaration) node;
        if (coi.isInterface()) {
            className = "[" + "<<interface>>;";
        } else {
            className = "[";
        }
        className += coi.getName();
        classShortName = coi.getName().toString();

        // Parsing Methods
        boolean nextParam = false;
        for (Object o : ((TypeDeclaration) node).getMembers()) {
            // Get Methods
            BodyDeclaration<?> bd = (BodyDeclaration<?>) o;
            if (bd instanceof ConstructorDeclaration) {
                ConstructorDeclaration cd = ((ConstructorDeclaration) bd);
                if (cd.getDeclarationAsString().startsWith("public")
                        && !coi.isInterface()) {
                    if (nextParam)
                        methods += ";";
                    methods += "+ " + cd.getName() + "(";
                    for (Object gcn : cd.getChildNodes()) {
                        if (gcn instanceof Parameter) {
                            Parameter paramCast = (Parameter) gcn;
                            String paramClass = paramCast.getType().toString();
                            String paramName = paramCast.getChildNodes()
                                    .get(0).toString();
                            if (map.containsKey(paramClass)
                                    && !map.get(classShortName)) {
                                additions += "[" + classShortName
                                        + "] uses -.->";
                                if (map.get(paramClass))
                                    additions += "[<<interface>>;" + paramClass
                                            + "]";
                                else
                                    additions += "[" + paramClass + "]";
                            }
                            additions += ",";
                        }
                    }
                    methods += ")";
                    nextParam = true;
                }
            }
        }
        for (Object o : ((TypeDeclaration) node).getMembers()) {
            BodyDeclaration<?> bd = (BodyDeclaration<?>) o;
            if (bd instanceof MethodDeclaration) {
                MethodDeclaration md = ((MethodDeclaration) bd);
                // Get only public methods
                if (md.getDeclarationAsString().startsWith("public")
                        && !coi.isInterface()) {
                    // Identify Setters and Getters
                    if (md.getNameAsString().startsWith("set")
                            || md.getNameAsString().startsWith("get")) {
                        String varName = md.getNameAsString().substring(3);
                        makeFieldPublic.add(varName.toLowerCase());
                    } else {
                        if (nextParam)
                            methods += ";";
                        methods += "+ " + md.getName() + "(";
                        for (Object gcn : md.getChildNodes()) {
                            if (gcn instanceof Parameter) {
                                Parameter paramCast = (Parameter) gcn;
                                String paramClass = paramCast.getType()
                                        .toString();
                                String paramName = paramCast.getChildNodes()
                                        .get(0).toString();
                                methods += paramName + " : " + paramClass;
                                if (map.containsKey(paramClass)
                                        && !map.get(classShortName)) {
                                    additions += "[" + classShortName
                                            + "] uses -.->";
                                    if (map.get(paramClass))
                                        additions += "[<<interface>>;"
                                                + paramClass + "]";
                                    else
                                        additions += "[" + paramClass + "]";
                                }
                                additions += ",";
                            }
                        }
                        methods += ") : " + md.getType();
                        nextParam = true;
                    }
                }
            }
        }
        // Parsing Fields
        boolean nextField = false;
        for (Object o : ((TypeDeclaration) node).getMembers()) {
            BodyDeclaration<?> bd = (BodyDeclaration<?>) o;
            if (bd instanceof FieldDeclaration) {
                FieldDeclaration fd = ((FieldDeclaration) bd);
                PrettyPrinterConfiguration conf = new PrettyPrinterConfiguration();
                conf.setPrintComments(false);
                String fieldScope = aToSymScope(
                        bd.toString(conf).substring(0,
                                bd.toString(conf).indexOf(" ")));
                String fieldClass = changeBrackets(fd.getElementType().toString());
                String fieldName = fd.getChildNodes().get(0).toString();
                if (fieldName.contains("="))
                    fieldName = fd.getChildNodes().get(0).toString()
                            .substring(0, fd.getChildNodes().get(0)
                                    .toString().indexOf("=") - 1);
                // Change scope of getter, setters
                if (fieldScope.equals("-")
                        && makeFieldPublic.contains(fieldName.toLowerCase())) {
                    fieldScope = "+";
                }
                String getDepen = "";
                boolean getDepenMultiple = false;
                if (fieldClass.contains("(")) {
                    getDepen = fieldClass.substring(fieldClass.indexOf("(") + 1,
                            fieldClass.indexOf(")"));
                    getDepenMultiple = true;
                } else if (map.containsKey(fieldClass)) {
                    getDepen = fieldClass;
                }
                if (getDepen.length() > 0 && map.containsKey(getDepen)) {
                    String connection = "-";

                    if (mapClassConn
                            .containsKey(getDepen + "-" + classShortName)) {
                        connection = mapClassConn
                                .get(getDepen + "-" + classShortName);
                        if (getDepenMultiple)
                            connection = "*" + connection;
                        mapClassConn.put(getDepen + "-" + classShortName,
                                connection);
                    } else {
                        if (getDepenMultiple)
                            connection += "*";
                        mapClassConn.put(classShortName + "-" + getDepen,
                                connection);
                    }
                }
                if (fieldScope == "+" || fieldScope == "-") {
                    if (nextField)
                        fields += "; ";
                    fields += fieldScope + " " + fieldName + " : " + fieldClass;
                    nextField = true;
                }
            }

        }
        // Check extends, implements
        if (coi.getExtendedTypes() != null && coi.getExtendedTypes().size() != 0) {
            List<ClassOrInterfaceType> classesList = (List<ClassOrInterfaceType>) coi
                    .getExtendedTypes();
            for(ClassOrInterfaceType extendClasses : classesList){
                additions += "[" + classShortName + "] " + "-^ " + "[" + extendClasses.getElementType() + "]";
                additions += ",";
            }
        }
        if (coi.getImplementedTypes() != null && coi.getImplementedTypes().size() != 0) {
            List<ClassOrInterfaceType> interfaceList = (List<ClassOrInterfaceType>) coi
                    .getImplementedTypes();
            for (ClassOrInterfaceType intface : interfaceList) {
                additions += "[" + classShortName + "] " + "-.-^ " + "["
                        + "<<interface>>;" + intface + "]";
                additions += ",";
            }
        }
        // Combine className, methods and fields
        result += className;
        if (!StringUtils.isEmpty(fields)) {
            result += "|" + changeBrackets(fields);
        }
        if (!StringUtils.isEmpty(methods)) {
            result += "|" + changeBrackets(methods);
        }
        result += "]";
        result += additions;
        return result;
    }

    private String changeBrackets(String foo) {
        foo = foo.replace("[", "(");
        foo = foo.replace("]", ")");
        foo = foo.replace("<", "(");
        foo = foo.replace(">", ")");
        return foo;
    }

    private String aToSymScope(String stringScope) {
        if(stringScope.equals("private")) return "-";
        if(stringScope.equals("public")) return "+";
        return "";
        /*
        switch (stringScope) {
            case "private":
                return "-";
            case "public":
                return "+";
            default:
                return "";
        }
        */
    }

    private void buildMap(ArrayList<CompilationUnit> cuArray) {
        for (CompilationUnit cu : cuArray) {
            List<TypeDeclaration<?>> cl = cu.getTypes();
            for (Node n : cl) {
                ClassOrInterfaceDeclaration coi = (ClassOrInterfaceDeclaration) n;
                map.put(coi.getNameAsString(), coi.isInterface()); // false is class,
                // true is interface
            }
        }
    }

    @SuppressWarnings("unused")
    private void printMaps() {
        System.out.println("Map:");
        Set<String> keys = mapClassConn.keySet(); // get all keys
        for (String i : keys) {
            System.out.println(i + "->" + mapClassConn.get(i));
        }
        System.out.println("---");
    }

    private ArrayList<CompilationUnit> getCuArray(String inPath)
            throws Exception {
        File folder = new File(inPath);

        File[] files = folder.listFiles();
        Arrays.sort(files);

        ArrayList<CompilationUnit> cuArray = new ArrayList<CompilationUnit>();
        for (final File f : files) {
            if (f.isFile() && f.getName().endsWith(".java")) {
                FileInputStream in = new FileInputStream(f);
                CompilationUnit cu;
                try {
                    cu = JavaParser.parse(in);
                    cuArray.add(cu);
                } finally {
                    in.close();
                }
            }
        }
        return cuArray;
    }

}
