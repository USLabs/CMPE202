package codeparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;



public class first {

    private static List<String> fileSet=new ArrayList<String>();
    //temp variables
    int n;
    int ip;
    public first(int abcd){
        System.out.println("hi");
    }
    protected void getFiles(File f)
    {
        //just checking if getfiles is displayed
        File files[];
        if(f.isFile())
            fileSet.add(f.getAbsolutePath());
        else
        {
            files = f.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                getFiles(files[i]);
            }
        }
    }
    public static void temp()
    {
        System.out.print("");
    }
}

public class A{
    A(int abcd){}

}

public class B{
    B(int abcd){}

}