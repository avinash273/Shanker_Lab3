import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class tester {
    static int[] ReadVectorClock(char ClockRealName) {
        char fromClock = 'A';
        String clockName = "./VectorClocks/" + fromClock + ".txt";
        int[] ClockValue = {0,0,0};

        //https://stackoverflow.com/questions/18838781/converting-string-array-to-an-integer-array
        try {
            BufferedReader in = new BufferedReader(
                    new FileReader(clockName));
            String str;
            String[] ClockTimeArray = new String[0];

            while ((str = in.readLine()) != null) {
                ClockTimeArray = str.split(",");
            }

            ClockValue[0] = Integer.parseInt(ClockTimeArray[0]);
            ClockValue[1] = Integer.parseInt(ClockTimeArray[1]);
            ClockValue[2] = Integer.parseInt(ClockTimeArray[2]);

            System.out.println(Arrays.toString(ClockValue));
            in.close();
        } catch (IOException e) {
            System.out.println("File Read Error");
        }
        
        return ClockValue;
    }


    public static void main(String[] args) {
        ReadVectorClock('A');
        ReadVectorClock('B');
        ReadVectorClock('C');
    }

}




