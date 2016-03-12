package pl.edu.agh.flowshop;

import com.google.common.collect.EvictingQueue;

import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Represents whole model in experiment.
 *
 * @author Bartosz Sądel
 *         Created on 11.03.2016.
 */
public class Model {

    /** List of machine layers */
    private final List<Layer> layers;

    /** Number of product types used in experiment. */
    private final int productTypesNo;

    /** Turn limit for experiment */
    private final int turnsLimit;

    /** Maximum size of orders queue */
    private final int queueSize;

    public Model(final List<Layer> layers, final int productTypesNo, final int turnsLimit, final int queueSize) {
        this.layers = layers;
        this.productTypesNo = productTypesNo;
        this.turnsLimit = turnsLimit;
        this.queueSize = queueSize;
    }

    /** Method generates new order. */
    private int[] generateOrder() {
        Random random = new Random();
        int[] order = new int[this.productTypesNo];

        for (int i = 0; i < this.productTypesNo; i++) {
            order[i] = random.nextInt(5);
        }

        return order;
    }

    /** Experiment main loop */
    public void run() throws Exception {
        Random random = new Random();
        Queue<int[]> orders = EvictingQueue.create(this.queueSize);
        int[] order;
        int[] finishedProducts = new int[this.productTypesNo];

        //main loop
        for (int turnNo = 0; turnNo < this.turnsLimit; turnNo++) {

            //generate new order
            if (random.nextInt() % 5 == 0) {
                order = generateOrder();
                orders.offer(order);
            } else {
                order = new int[this.productTypesNo];
            }

            //execute turn across layers
            for (Layer layer : this.layers) {
                order = layer.tick(turnNo, order);
            }

            //collect finished products
            for (int i = 0; i < this.productTypesNo; i++) {
                finishedProducts[i] += order[i];
            }

            //remove finished orders
            deliverOrders(orders, finishedProducts);
        }
    }

    /** Removes orders from queue when all products are ready */
    public void deliverOrders(final Queue<int[]> orders, final int[] finishedProducts) {
        for (int[] order : orders) {
            for (int i = 0; i < finishedProducts.length; i++) {
                //not enough product -> we are finished
                if (order[i] > finishedProducts[i]) {
                    return;
                }
            }

            //order finished -> remove from queue
            for (int i = 0; i < finishedProducts.length; i++) {
                finishedProducts[i] -= order[i];
            }
            orders.poll();
        }
    }

}
