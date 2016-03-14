package pl.edu.agh.flowshop;

import com.google.common.collect.EvictingQueue;
import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.List;
import java.util.Map;
import java.util.Queue;

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

    /** Experiment main loop */
    public void run() throws Exception {
        PoissonDistribution random = new PoissonDistribution(3);
        Queue<Order> orders = EvictingQueue.create(this.queueSize);
        Order order;
        int[] products;
        int[] finishedProducts = new int[this.productTypesNo];
        int newOrderTurn = random.sample();

        //main loop
        for (int turnNo = 0; turnNo < this.turnsLimit; turnNo++) {

            //generate new order
            if (turnNo == newOrderTurn) {
                order = generateOrder();
                orders.offer(order);
                products = order.getProductsList();
                newOrderTurn += random.sample();
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
     *
     * @return reward for completed orders
     */
    private int deliverOrders(final Queue<Order> orders, final int[] finishedProducts) {
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

    /** Method generates new order. */
    private Order generateOrder() {
        PoissonDistribution random = new PoissonDistribution(3);
        int[] order = new int[this.productTypesNo];

        int reward = 0;
        for (int i = 0; i < this.productTypesNo; i++) {
            order[i] = random.sample();
            reward += order[i] * this.costs.get(i);
        }

        int penalty = random.sample();
        penalty = penalty > 0 ? penalty : 0;

        return new Order(order, random.sample() + 3, reward, penalty);
    }

}
