import java.io.*;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class Tema2 {
    static String ordersPath;
    static String productsPath;
    static int threadsNumber;
    private static FileWriter ordersOut;
    private static FileWriter productsOut;

    public static FileWriter getOrdersOut() {
        return ordersOut;
    }

    public static FileWriter getProductsOut() {
        return productsOut;
    }

    /*
        initializare variabile cu argumentele primite in linia
        de comanda
     */
    public static void initArgs(String[] args) {
        ordersPath = args[0] + "/orders.txt";
        productsPath = args[0] + "/order_products.txt";
        threadsNumber = Integer.parseInt(args[1]);
    }

    /*
        deschiderea fisierelor , initializarea ForkJoinPool-ului cu
        numarul maxim de threaduri primit si inchiderea fisierelor
        dupa ce au loc toate opratiile
    */
    public static void main(String[] args) throws FileNotFoundException {
        initArgs(args);

        FileReader ordersFile;
        ordersFile = new FileReader(ordersPath);

        BufferedReader ordersReader = new BufferedReader(ordersFile);
        ordersOut = null;
        productsOut = null;

        try {
            ordersOut = new FileWriter("orders_out.txt");
            productsOut = new FileWriter("order_products_out.txt");

            ForkJoinPool fjp = new ForkJoinPool(threadsNumber);
            fjp.invoke(new OrdersProcess(fjp, ordersReader, productsPath));
            fjp.shutdown();

            for (FileWriter fileWriter : Arrays.asList(ordersOut, productsOut)) {
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}