import java.io.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

/*
    clasa realizata cu scopul de a prelucra comenzile
*/
public class OrdersProcess extends RecursiveTask<Void> {
    public BufferedReader lineSearched;
    public String fileProducts;
    public ForkJoinPool fjp;

    public OrdersProcess(ForkJoinPool fjp,
                         BufferedReader lineSearched, String fileProducts) {
        this.fjp = fjp;
        this.lineSearched = lineSearched;
        this.fileProducts = fileProducts;
    }

    /*
        citire comanda
    */
    public synchronized String takeOrder() throws IOException {
        String command;
        command = lineSearched.readLine();
        return  command;
    }

    /*
        scrie comanda finala in fisierul de out al comenzilor
    */
    public synchronized void writeOrders(String line) throws IOException {
        Tema2.getOrdersOut().write(line);
    }

    /*
        se asteapta ca produsele din cadrul unei comenzi sa fi fost
        finalizate
    */
    public synchronized void dummyOperation(AtomicInteger at , int prodNum) {
        while (true) {
            if (at.get() >= prodNum)
                break;
        }
    }

    /*
        pentru fiecare comanda se creeaza task-uri pentru fiecare produs
        din cadrul comenzii si dupa ce sunt finalizate se scrie in
        fisierul orders_out , comanda ca fiind finalizata
    */
    public synchronized void readAndProcess(String linePosition) {
        String[] orderNumber = linePosition.split(",");
        String orderName = orderNumber[0];
        if (Integer.parseInt(orderNumber[1]) != 0) {
            AtomicInteger orderProducts = new AtomicInteger(0);
            FileReader productsFile;
            try {
                productsFile = new FileReader(fileProducts);
                BufferedReader productsReader = new BufferedReader(productsFile);
                for (int i = 1; i <= Integer.parseInt(orderNumber[1]); i++) {
                    fjp.invoke(new ProductsProcess(orderName, productsReader, fjp, orderProducts, Integer.parseInt(orderNumber[1])));
                }
                dummyOperation(orderProducts, Integer.parseInt(orderNumber[1]));
                try {
                    writeOrders(orderName + "," + Integer.parseInt(orderNumber[1]) + ",shipped" + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        iau linie cu linie din fisierul de order si pentru fiecare
        invoc un nou task , nu inainte sa procesez fiecare produs
        ce face parte din comanda precedenta
    */
    @Override
    public synchronized Void compute() {
        String line;
        line = null;
        try {
            line = takeOrder();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (line != null) {
            readAndProcess(line);
            fjp.invoke(new OrdersProcess(fjp, lineSearched, fileProducts));
        }
        return null;
    }
}
