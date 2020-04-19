import java.io.*;
import java.util.Arrays;

public class tester {
    static int[] ReadVectorClock(char ClockRealName) {
        String clockName = "./VectorClocks/" + ClockRealName + ".txt";
        int[] ClockValue = {0, 0, 0};

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

            System.out.println("Clock" + ClockRealName + ":" + Arrays.toString(ClockValue));
            in.close();
        } catch (IOException e) {
            System.out.println("File Read Error");
        }

        return ClockValue;
    }


    static void WriteVectorClock(char ClockRealName, int[] ClockValue) throws IOException {
        String ClockName = "./VectorClocks/" + ClockRealName + ".txt";
        FileWriter fw = new FileWriter(ClockName, false);
        BufferedWriter UserQueue = new BufferedWriter(fw);
        //Write to Queue
        String Content = ClockValue[0] + "," + ClockValue[1] + "," + ClockValue[2];
        UserQueue.write(Content);
        UserQueue.close();
    }


    public static void main(String[] args) throws IOException {
        ReadVectorClock('A');
        ReadVectorClock('B');
        ReadVectorClock('C');

        int[] ClockValue= {10,10,10};
        WriteVectorClock('A', ClockValue);
        ReadVectorClock('A');
    }

}




