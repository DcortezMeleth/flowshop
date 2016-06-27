package pl.edu.agh.flowshop.engine;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.flowshop.entity.Order;
import pl.edu.agh.flowshop.utils.Attributes;
import pl.edu.agh.flowshop.utils.Parameters;
import weka.core.Instance;
import weka.core.SparseInstance;

import java.util.*;

/**
 * Represents whole model in experiment.
 *
 * @author Bartosz Sądel
 *         Created on 11.03.2016.
 */
public class Model {

    public static final int MAX_REWARD_VALUE = 10;

    public static final int DUE_TIME_RAND_MAX = 10;

    public static final int DUE_TIME_MIN_VALUE = 8;

    public static final int MAX_ORDER_SIZE = 5;

    public static final int MIN_ORDER_SIZE = 3;

    private final static Logger logger = LogManager.getLogger(Model.class);

    /** Buffer of finished products waiting for delivery */
    private int[] finishedProducts = new int[Parameters.PRODUCT_TYPES_NO];

    /** Layers inside model. */
    private List<Layer> layers;

    private List<Order> finishedOrders = new ArrayList<>();

    public Model(final List<Layer> layers) {
        this.layers = layers;
    }

    /**
     * Experiment main loop
     *
     * @return return queue sizes from each iteration
     */
    public List<Double> run() throws Exception {
        logger.debug("------------------------------------------");
        logger.debug("---------- EXPERIMENT - START ------------");
        logger.debug("------------------------------------------");
        PoissonDistribution random = new PoissonDistribution(3);
        List<Order> orders = new LinkedList<>();
        Order order;
        int[] products;
        int newOrderTurn = random.sample();
        List<Double> queueSizes = new ArrayList<>();

        //main loop
        for (int turnNo = 0; turnNo < Parameters.TURN_LIMIT; turnNo++) {
            logger.debug("Turn: " + turnNo);

            //train on collected data
            if (turnNo % Parameters.LEARNING_TURN == 0) {
                logger.debug("Learning turn!");
                for (Layer layer : this.layers) {
                    for (Machine machine : layer.getMachines()) {
                        machine.train();
                    }
                }
            }

            //generate new order
            if (turnNo == newOrderTurn) {
                order = generateOrder();
                orders.add(order);
                products = order.getProductsList();
                newOrderTurn += random.sample() + 4;
                logger.debug("Order generated: " + order.toString());
            } else {
                products = new int[Parameters.PRODUCT_TYPES_NO];
            }

            //execute turn across layers and collect finished products
            int[] finishedProducts = products;
            for (Layer layer : this.layers) {
                finishedProducts = layer.tick(finishedProducts);
                for(int i=0; i<Parameters.PRODUCT_TYPES_NO; i++) {
                    if(finishedProducts[i] < 0) {
                        throw new Exception("Negative finished products number.");
                    }
                }
            }

            for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
                this.finishedProducts[i] += finishedProducts[i];
            }

            //remove finished orders
            deliverOrders(orders, this.finishedProducts);

            orders.forEach(Order::decreaseDueTime);

            queueSizes.add(getQueuesSize(turnNo));
            logger.debug("Orders size:" + orders.size());
            logger.debug("Finished orders size:" + finishedOrders.size());
        }

        logger.debug("------------------------------------------");
        logger.debug("---------- EXPERIMENT - STOP- ------------");
        logger.debug("------------------------------------------");

        return queueSizes;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    /** Prepares entry for decision */
    protected Instance prepareInstanceForDecision() {
        Instance instance = new SparseInstance(Attributes.size() - 1);
        setAttributesValues(instance);
        return instance;
    }

    /** Return number of all product in all queues */
    private Double getQueuesSize(final int turnNo) {
        double result = 0;
        StringBuilder sb;
        for (Layer layer : this.layers) {
            sb = new StringBuilder("Buffer of layer" + layer.getId() + " in turn " + turnNo + " [");
            for (int product : layer.getBuffer()) {
                sb.append(product).append(",");
            }
            logger.debug(sb.toString());
            result += layer.getQueueSize();
        }
        return result;
    }

    /** Returns attributes list in form of integer list */
    private List<Integer> getAttributesValues() {
        return getAttributesValues(false, null, 0, 0);
    }

    /** Set attributes values in given instance */
    private void setAttributesValues(final Instance instance) {
        int attrIdx = 0;
        for (Integer val : getAttributesValues()) {
            instance.setValue(attrIdx++, val);
        }
    }

    /**
     * Returns attributes list in form of integer list
     *
     * @param simulate   if we should simulate one turn
     * @param attrValues starting attributes values for simulation
     * @param agentNo    no. of agent performing an action
     * @param action     action performed by agent
     */
    private List<Integer> getAttributesValues(final boolean simulate, final List<Integer> attrValues, final int agentNo, final int action) {
        List<Integer> result = new ArrayList<>();
        int[] producedProducts = new int[Parameters.PRODUCT_TYPES_NO];
        int[] addToProducts = new int[Parameters.PRODUCT_TYPES_NO];
        int[] takenFromBufferProducts = new int[Parameters.PRODUCT_TYPES_NO];

        int attrNo = 0;
        for (Layer layer : this.layers) {
            for (Machine machine : layer.getMachines()) {
                if (machine.getProductToBeProcessed() > 0) {
                    producedProducts[machine.getProductToBeProcessed()] += 1;
                    int nextProductType = agentNo == machine.getId() ? action : machine.getProductToBeProcessed();
                    takenFromBufferProducts[nextProductType] += 1;
                }
                result.add(machine.isBroken() ? 0 : 1);
                attrNo++;
            }

            for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
                int quantity = layer.getQuantityInBuffer(i);

                if (simulate) {
                    quantity = attrValues.get(attrNo) + addToProducts[i] - takenFromBufferProducts[i];
                }

                if (quantity < 0) {
                    quantity = 0;
                }
                result.add(quantity);
                attrNo++;
            }

            addToProducts = producedProducts;
            producedProducts = new int[Parameters.PRODUCT_TYPES_NO];
            takenFromBufferProducts = new int[Parameters.PRODUCT_TYPES_NO];
        }

        return result;
    }

    /**
     * Removes orders from queue when all products are ready
     */
    private void deliverOrders(final List<Order> orders, final int[] finishedProducts) throws Exception {
        logger.debug("Delivering orders!");
        logger.debug("Orders queue size: " + orders.size());
        StringBuilder sb = new StringBuilder("");
        for (int product : finishedProducts) {
            sb.append(product).append(",");
        }
        logger.debug("Finished products: [" + sb + "]");
        for(Iterator<Order> it = orders.iterator(); it.hasNext();) {
            Order order = it.next();
            int[] demandedProducts = order.getProductsList();

            boolean completed = true;
            for (int j = 0; j < Parameters.PRODUCT_TYPES_NO; j++) {
                if (demandedProducts[j] > finishedProducts[j]) {
                    logger.debug("Not enough part to finish order: " + order.toString());
                    sb = new StringBuilder("");
                    for (int product : demandedProducts) {
                        sb.append(product).append(",");
                    }
                    completed = false;
                }
            }
            if(!completed) {
                // not completed order -> proceed to next one
                continue;
            }

            //order finished -> remove from queue
            for (int j = 0; j < finishedProducts.length; j++) {
                finishedProducts[j] -= demandedProducts[j];
                if(finishedProducts[j] < 0) {
                    throw new Exception("Negative finished products: " + finishedProducts[j]);
                }
            }
            finishedOrders.add(order);
            it.remove(); // removing finished order from list
            logger.debug("Order finished: " + order.toString());
        }

        logger.debug("Finished orders count: " + finishedOrders.size());
    }

    /** Method generates new order. */
    private Order generateOrder() {
        Random random = new Random();
        int[] order = new int[Parameters.PRODUCT_TYPES_NO];
        order[random.nextInt(order.length)] = random.nextInt(MAX_ORDER_SIZE - MIN_ORDER_SIZE) + MIN_ORDER_SIZE;

        int reward = Parameters.REWARD != 0 ? Parameters.REWARD : random.nextInt(MAX_REWARD_VALUE);
        int penalty = Parameters.PENALTY != 0 ? (int) (reward * Parameters.PENALTY) : random.nextInt(reward);

        return new Order(order, random.nextInt(DUE_TIME_RAND_MAX) + DUE_TIME_MIN_VALUE, reward, penalty, reward);
    }
}
