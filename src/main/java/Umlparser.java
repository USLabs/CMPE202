public class Umlparser {

    public static void main(String[] args) throws Exception {

            ParseEngine pe = new ParseEngine("/home/techmint/Downloads/MyUMLParser/src/test/java/classDiagramTest1", "/home/techmint/Downloads/MyUMLParser/src/sampleOutput/classDiagramTest1");
            pe.start();

            ParseSeqEngine pse = new ParseSeqEngine("/home/techmint/Downloads/MyUMLParser/src/test/java/sequenceDiagramTest1", "Customer", "depositMoney", "/home/techmint/Downloads/MyUMLParser/src/sampleOutput/seq");
            pse.start();
    }
}