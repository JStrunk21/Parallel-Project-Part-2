import java.io.*;
import java.net.*;
import java.io.FileWriter;
import java.util.Random;
public class RandomNumberGenerator {
    public static void main(String[] args) {
        //array for array size
        int arraySize = 300;
        int min = 0;
        int max = 250;
        try {

            FileWriter writer = new FileWriter("file.txt");
            for (int i = 0; i < arraySize; i++) {

                Random rng = new Random();
                double randNum = rng.nextInt(max - min) + min;
                writer.write(randNum + "\n");
                if(i == arraySize - 1)
                {
                    writer.write("bye");
                }
            }
            writer.close();

        } catch (IOException e) {
            System.out.println("Num Error");
        }
    }
}