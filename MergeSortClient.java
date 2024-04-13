import java.io.*;
import java.net.*;
import java.util.Arrays;

public class MergeSortClient {

    public static void main(String[] args) throws Exception {
        Socket clientSocket = new Socket("localhost", 8080);



        int arraySize = 200;
        int min = -250;
        int max = 250;
        int[] numArray = new int[arraySize];
        for(int i = 0; i < arraySize; i++)
        {
            numArray[i] = min + (int)(Math.random() * ((max - min) + 1));
        }
        System.out.println("Unsorted Array: ");
        for(int j = 0; j < arraySize; j++)
        {
            System.out.print(numArray[j] + " ");
        }
        System.out.println();
        // Send the unsorted array to the server
        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
        out.writeObject(numArray);

        // Read the sorted array from the server
        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
        int[] sortedArray = (int[]) in.readObject();
        long runTime = (long) in.readObject();

        // Print the sorted array
        System.out.println("Sorted Array: ");
        for (int i = 0; i < sortedArray.length; i++) {
            System.out.print(sortedArray[i] + " ");
        }
        System.out.println();
        System.out.println("Run Time: " + runTime + "ms");

        // Close the streams and the socket
        in.close();
        out.close();
        clientSocket.close();
    }
}