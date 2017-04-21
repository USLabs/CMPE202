public class Umlparser {

    public static void main(String[] args) throws Exception {

            //ParseEngine pe = new ParseEngine("/home/techmint/Downloads/labby/new/TheUMLParser/src/test/java/classDiagramTest1", "/home/techmint/Downloads/labby/new/TheUMLParser/src/sampleOutput/classDiagramTest1");
            //pe.start();

            ParseSeqEngine pse = new ParseSeqEngine("/home/techmint/Downloads/labby/new/TheUMLParser/src/test/java/sequenceDiagramTest1", "Customer", "depositMoney", "/home/techmint/Downloads/labby/new/TheUMLParser/src/sampleOutput/seq");
            pse.start();
    }
}