package pl.edu.agh.flowshop;

import com.google.common.collect.EvictingQueue;
import org.apache.commons.math3.distribution.PoissonDistribution;
import pl.edu.agh.utils.Parameters;

import java.util.List;
import java.util.Queue;

/**
 * Represents whole model in experiment.
 *
 * @author Bartosz Sądel
 *         Created on 11.03.2016.
 */
public class Model extends LearningAgent {

    /** List of machine layers */
    private final List<Layer> layers;

    public Model(final List<Layer> layers, final String classifierName) {
        super(classifierName, layers, Parameters.MODEL);
        this.layers = layers;
    }

    /** Experiment main loop */
    public void run() throws Exception {
        PoissonDistribution random = new PoissonDistribution(3);
        Queue<Order> orders = EvictingQueue.create(Parameters.QUEUE_SIZE);
        Order order;
        int[] products;
        int[] finishedProducts = new int[Parameters.PRODUCT_TYPES_NO];
        int newOrderTurn = random.sample();

        //main loop
        for (int turnNo = 0; turnNo < Parameters.TURN_LIMIT; turnNo++) {

            //generate new order
            if (turnNo == newOrderTurn) {
                order = generateOrder();
                orders.offer(order);
                products = order.getProductsList();
                newOrderTurn += random.sample();
            } else {
                products = new int[Parameters.PRODUCT_TYPES_NO];
            }

            //execute turn across layers
            for (Layer layer : this.layers) {
                products = layer.tick(turnNo, products);
            }

            //collect finished products
            for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
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
    private int deliverOrders(final Queue<Order> orders, final int[] finishedProducts) throws Exception {
        int reward = 0;
        for(int i=0; i<orders.size(); i++) {
            Order order = orders.element();
            int[] product = order.getProductsList();
            for (int j = 0; j < finishedProducts.length; j++) {
                //not enough product -> we are finished
                if (product[j] > finishedProducts[j]) {
                    return reward;
                }
            }

            //order finished -> remove from queue
            for (int j = 0; j < finishedProducts.length; j++) {
                finishedProducts[j] -= product[j];
            }
            orders.poll();

            train();
            addTrainData();

            reward += order.getReward();
        }

        return reward;
    }

    @Override
    protected void decideOnAction(final int action) throws Exception {

    }


    /** Method generates new order. */
    private Order generateOrder() {
        PoissonDistribution random = new PoissonDistribution(3);
        int[] order = new int[Parameters.PRODUCT_TYPES_NO];

        int reward = 0;
        for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
            order[i] = random.sample();
            reward += order[i] * Parameters.COSTS.get(i+1);
        }

        int penalty = random.sample();
        penalty = penalty > 0 ? penalty : 0;

        return new Order(order, random.sample() + 3, reward, penalty);
    }

}
