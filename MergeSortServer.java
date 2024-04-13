import java.io.*;
import java.net.*;
import java.lang.System;
import java.util.ArrayList;

public class MergeSortServer {

    // Assuming system has 4 logical processors
    private static final int MAX_THREADS = 16;

    // Custom Thread class with constructors
    private static class SortThreads extends Thread{
        SortThreads(int[] array, int begin, int end){
            super(()->{
                mergeSort(array, begin, end);
            });
            this.start();
        }
    }

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8080);
        Socket clientSocket = serverSocket.accept();

        // Read the unsorted array from the client
        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
        int[] unsortedArray = (int[]) in.readObject();

        // Sort the array using merge sort
        int[] sortedArray = mergeSort(unsortedArray);
        long runTime = threadedSort(unsortedArray);

        // Send the sorted array back to the client
        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
        out.writeObject(sortedArray);
        out.writeObject(runTime);

        // Close the streams and the socket
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    private static int[] mergeSort(int[] array) {
        if (array.length <= 1) {
            return array;
        }

        // Divide the array into two halves
        int[] left = new int[array.length / 2];
        int[] right = new int[array.length - left.length];
        System.arraycopy(array, 0, left, 0, left.length);
        System.arraycopy(array, left.length, right, 0, right.length);

        // Sort the two halves recursively
        left = mergeSort(left);
        right = mergeSort(right);

        // Merge the two sorted halves
        return merge(left, right);
    }

    private static int[] merge(int[] left, int[] right) {
        int[] mergedArray = new int[left.length + right.length];
        int i = 0, j = 0, k = 0;
        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) {
                mergedArray[k] = left[i];
                i++;
            } else {
                mergedArray[k] = right[j];
                j++;
            }
            k++;
        }

        // Copy the remaining elements from the left half
        while (i < left.length) {
            mergedArray[k] = left[i];
            i++;
            k++;
        }

        // Copy the remaining elements from the right half
        while (j < right.length) {
            mergedArray[k] = right[j];
            j++;
            k++;
        }

        return mergedArray;
    }
    public static long threadedSort(int[] array){
        // For performance - get current time in millis before starting
        long time = System.currentTimeMillis();
        final int length = array.length;
        // Workload per thread (chunk_of_data) = total_elements/core_count
        // if the no of elements exactly go into no of available threads,
        // then divide work equally,
        // else if some remainder is present, then assume we have (actual_threads-1) available workers
        // and assign the remaining elements to be worked upon by the remaining 1 actual thread.
        boolean exact = length%MAX_THREADS == 0;
        int maxlim = exact? length/MAX_THREADS: length/(MAX_THREADS-1);
        // if workload is less and no more than 1 thread is required for work, then assign all to 1 thread
        maxlim = maxlim < MAX_THREADS? MAX_THREADS : maxlim;
        // To keep track of threads
        final ArrayList<SortThreads> threads = new ArrayList<>();
        // Since each thread is independent to work on its assigned chunk,
        // spawn threads and assign their working index ranges
        // ex: for 16 element list, t1 = 0-3, t2 = 4-7, t3 = 8-11, t4 = 12-15
        for(int i=0; i < length; i+=maxlim){
            int beg = i;
            int remain = (length)-i;
            int end = remain < maxlim? i+(remain-1): i+(maxlim-1);
            final SortThreads t = new SortThreads(array, beg, end);
            // Add the thread references to join them later
            threads.add(t);
        }
        for(Thread t: threads){
            try{
                // This implementation of merge requires, all chunks worked by threads to be sorted first.
                // so we wait until all threads complete
                t.join();
            } catch(InterruptedException ignored){}
        }
        // System.out.println("Merging k-parts array, where m number of parts are distinctly sorted by each Threads of available MAX_THREADS="+MAX_THREADS);
        /*
          The merge takes 2 parts at a time and merges them into 1,
          then again merges the resultant into next part and so on...until end
          For MAXLIMIT = 2 (2 elements per thread where total threads = 4, in a total of 4*2 = 8 elements)
          list1 = (beg, mid); list2 = (mid+1, end);
          1st merge = 0,0,1 (beg, mid, end)
          2nd merge = 0,1,3 (beg, mid, end)
          3rd merge = 0,3,5 (beg, mid, end)
          4th merge = 0,5,7 (beg, mid, end)
        */
        for(int i=0; i < length; i+=maxlim){
            int mid = i == 0? 0 : i-1;
            int remain = (length)-i;
            int end = remain < maxlim? i+(remain-1): i+(maxlim-1);
            // System.out.println("Begin: "+0 + " Mid: "+ mid+ " End: "+ end + " MAXLIM = " + maxlim);
            merge(array, 0, mid, end);
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent for custom multi-threaded recursive merge_sort(): "+ time+ "ms");
        return time;
    }

    // Typical recursive merge sort
    public static void mergeSort(int[] array, int begin, int end){
        if (begin<end){
            int mid = (begin+end)/2;
            mergeSort(array, begin, mid);
            mergeSort(array, mid+1, end);
            merge(array, begin, mid, end);
        }
    }

    //Typical 2-way merge
    public static void merge(int[] array, int begin, int mid, int end){
        int[] temp = new int[(end-begin)+1];

        int i = begin, j = mid+1;
        int k = 0;

        // Add elements from first half or second half based on whichever is lower,
        // do until one of the list is exhausted and no more direct one-to-one comparison could be made
        while(i<=mid && j<=end){
            if (array[i] <= array[j]){
                temp[k] = array[i];
                i+=1;
            }else{
                temp[k] = array[j];
                j+=1;
            }
            k+=1;
        }

        // Add remaining elements to temp array from first half that are left over
        while(i<=mid){
            temp[k] = array[i];
            i+=1; k+=1;
        }

        // Add remaining elements to temp array from second half that are left over
        while(j<=end){
            temp[k] = array[j];
            j+=1; k+=1;
        }

        for(i=begin, k=0; i<=end; i++,k++){
            array[i] = temp[k];
        }
    }
}



