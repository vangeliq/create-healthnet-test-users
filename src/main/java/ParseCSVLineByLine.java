import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class ParseCSVLineByLine
{
    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception
    {
        String inputFile = "C:\\Users\\valery.angelique\\IdeaProjects\\create-healthnet-test-users\\src\\main\\java\\data\\input.csv";

        CSVReader reader = new CSVReader(new FileReader(inputFile), ',' , '"' , 0);
        List<String[]> allRows = reader.readAll();

        for(String[] row : allRows){
            System.out.println(Arrays.toString(row));
        }
    }
}