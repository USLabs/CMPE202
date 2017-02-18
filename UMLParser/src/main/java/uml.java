import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.JavaParser;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class uml {
    private static List<String> filesList = new ArrayList();

    public static void main(String[] args) {
        File mainFolder = new File("/home/techmint/Downloads/lec1/UMLParser/src/main/resources/inputFiles");
        getFilesFromFolder(mainFolder);
        parse();
    }

    public static void parse() {
        String file = filesList.get(0);
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        CompilationUnit cu = JavaParser.parse(input);
        new MethodVisitor().visit(cu, null);
    }

    private static class MethodVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            System.out.println(n.getName());
            super.visit(n, arg);
        }
    }

    public static void getFilesFromFolder(File f) {
        File files[];
        if (f.isFile())
            filesList.add(f.getAbsolutePath());
        else {
            files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                getFilesFromFolder(files[i]);
            }
        }
    }
}