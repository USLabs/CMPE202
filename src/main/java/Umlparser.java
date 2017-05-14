public class Umlparser {

    public static void main(String[] args) throws Exception {

        if(args[0] == "uml") {
            UMLDiagramProcessor umlP = new UMLDiagramProcessor(args[1], args[2]);
            umlP.start();
        }

        if (args[0] == "seq") {
            SequenceProcessor seqP = new SequenceProcessor(args[1], args[2], args[3], args[4]);
            seqP.start();
        }
    }
}