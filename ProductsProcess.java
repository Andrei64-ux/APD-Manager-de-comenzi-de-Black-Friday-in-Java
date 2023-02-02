import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

/*
    clasa ce prelucreaza prdousele din cadrul fiecarei comenzi
*/
public class ProductsProcess extends RecursiveTask<Void> {
    String order;
    BufferedReader productsFile;
    ForkJoinPool fjp;
    AtomicInteger orderProducts;
    int productsNumber;

    public ProductsProcess(String order, BufferedReader productsFile, ForkJoinPool fjp, AtomicInteger orderProducts, int productsNumber) {
        this.order = order;
        this.productsFile = productsFile;
        this.fjp = fjp;
        this.orderProducts = orderProducts;
        this.productsNumber = productsNumber;
    }

    /*
        analog ca la orders , preiau din fisier cate un produs
    */
    public synchronized String takeProduct() throws IOException {
        String product;

        product = productsFile.readLine();
        while (!product.contains(order)) {
            product = productsFile.readLine();
            if (product == null) {
                break;
            }
        }
        return product;
    }

    /*
        scrire in fisierul final
    */
    public synchronized void writeProducts(String line) throws IOException {
        Tema2.getProductsOut().write(line);
    }

    /*
        preiau produs cu produs si il scriu in fisier , practic imbim
        functiile de mai sus si am un atomicInteger pentru a stii cate
        produse am procesat pana la momentul respectiv
    */
    @Override
    public Void compute() {
        String product = null;
        try {
            product = takeProduct();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (product != null) {
            try {
                writeProducts(product + ",shipped" + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            orderProducts.incrementAndGet();
        }
        return null;
    }
}
