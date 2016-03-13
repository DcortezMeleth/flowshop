package pl.edu.agh.flowshop;

import com.google.common.collect.EvictingQueue;

import java.util.*;

/**
 * Represents whole model in experiment.
 *
 * @author Bartosz Sądel
 *         Created on 11.03.2016.
 */
public class Model {

    /** List of machine layers */
    private final List<Layer> layers;

    /** Unit prices for products */
    private final Map<Integer, Integer> costs;

    /** Number of product types used in experiment. */
    private final int productTypesNo;

    /** Turn limit for experiment */
    private final int turnsLimit;

    /** Maximum size of orders queue */
    private final int queueSize;

    public Model(final List<Layer> layers, final Map<Integer, Integer> costs, final int productTypesNo, final int turnsLimit, final int queueSize) {
        this.layers = layers;
        this.costs = costs;
        this.productTypesNo = productTypesNo;
        this.turnsLimit = turnsLimit;
        this.queueSize = queueSize;
    }

    /** Method generates new order. */
    private Order generateOrder() {
        Random random = new Random();
        int[] order = new int[this.productTypesNo];

        int reward = 0;
        for (int i = 0; i < this.productTypesNo; i++) {
            order[i] = random.nextInt(5);
            reward += order[i] * this.costs.get(i);
        }

        int penalty = random.nextInt(reward) - 3;
        penalty = penalty > 0 ? penalty : 0;

        return new Order(order, random.nextInt(8) + 3, reward, penalty);
    }

    /** Experiment main loop */
    public void run() throws Exception {
        Random random = new Random();
        Queue<Order> orders = EvictingQueue.create(this.queueSize);
        Order order;
        int[] products;
        int[] finishedProducts = new int[this.productTypesNo];

        //main loop
        for (int turnNo = 0; turnNo < this.turnsLimit; turnNo++) {

            //generate new order
            if (random.nextInt() % 5 == 0) {
                order = generateOrder();
                orders.offer(order);
                products = order.getProductsList();
            } else {
                products = new int[this.productTypesNo];
            }

            //execute turn across layers
            for (Layer layer : this.layers) {
                products = layer.tick(turnNo, products);
            }

            //collect finished products
            for (int i = 0; i < this.productTypesNo; i++) {
                finishedProducts[i] += products[i];
            }

            //remove finished orders
            deliverOrders(orders, finishedProducts);
        }
    }

    /**
     * Removes orders from queue when all products are ready
     * @return reward for completed orders
     */
    public int deliverOrders(final Queue<Order> orders, final int[] finishedProducts) {
        int reward = 0;
        for (Order order : orders) {
            int[] product = order.getProductsList();
            for (int i = 0; i < finishedProducts.length; i++) {
                //not enough product -> we are finished
                if (product[i] > finishedProducts[i]) {
                    return reward;
                }
            }

            //order finished -> remove from queue
            for (int i = 0; i < finishedProducts.length; i++) {
                finishedProducts[i] -= product[i];
            }
            orders.poll();

            reward += order.getReward();
        }

        return reward;
    }

}
