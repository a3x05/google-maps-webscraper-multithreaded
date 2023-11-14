package at.sirebi;

import org.eclipse.collections.api.factory.Lists;
import java.io.*;
import java.util.List;

public class Main {

    private static final List<String> linkList = Lists.mutable.empty();

    public static void main(String[] args) throws InterruptedException {
        List<Integer> plzList = getPlz();
        List<List<Integer>> plzSubList = Lists.mutable.empty();

        for (int count = 0; count < 5; count++) { // = threads
            plzSubList.add(Lists.mutable.empty());
        }
        for (int index = 0; index < plzList.size(); index++) {
            plzSubList.get(index % plzSubList.size()).add(plzList.get(index));
        }

        List<Thread> threadList = Lists.mutable.empty();
        for (int index = 0; index < plzSubList.size(); index++) {
            WorkerThread workerThread = new WorkerThread(plzSubList.get(index), index);
            Thread thread = new Thread(workerThread);
            threadList.add(thread);
            thread.start();
        }

        for (Thread thread : threadList) {
            thread.join();
        }
    }

    private static List<Integer> getPlz() {
        List<Integer> plzList = Lists.mutable.empty();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/plz.csv"))) {
            String line;
            reader.readLine(); // skip first line
            while ((line = reader.readLine()) != null) {
                String[] array = line.split(";");
                if (array[7].equals("Ja")) {
                    plzList.add(
                            Integer.parseInt(array[0])
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plzList;
    }

}
