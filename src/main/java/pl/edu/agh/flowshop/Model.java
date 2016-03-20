package pl.edu.agh.flowshop;

import com.google.common.collect.MinMaxPriorityQueue;
import org.apache.commons.math3.distribution.PoissonDistribution;
import pl.edu.agh.utils.OrderComparator;
import pl.edu.agh.utils.Parameters;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.SparseInstance;

import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Represents whole model in experiment.
 *
 * @author Bartosz Sądel
 *         Created on 11.03.2016.
 */
public class Model extends LearningAgent {

    /** Vector of {@link weka.core.Attribute Attributes} used for learning */
    private final FastVector attributes;

    /** Buffer of finished products waiting for delivery */
    private int[] finishedProducts = new int[Parameters.PRODUCT_TYPES_NO];

    public Model(final List<Layer> layers, final String classifierName) {
        super(classifierName, layers, Parameters.MODEL);

        //count number of attributes for learning
        int attrNo = Parameters.PRODUCT_TYPES_NO * layers.size();
        for(Layer layer : layers) {
            attrNo += layer.getAgents().size();
        }
        attrNo++;

        // attributes initialization, for now same for all learning layers
        FastVector attributes = new FastVector(attrNo);
        final String healthPrefix = "health_";
        final String bufferPrefix = "buffer_";
        for(Layer layer : layers) {
            layer.setAttributes(attributes);
            for(int i=1; i<=Parameters.PRODUCT_TYPES_NO; i++) {
                Attribute buffer = new Attribute(bufferPrefix + layer.getId() + "_" + i);
                attributes.addElement(buffer);
            }
            for(LearningAgent agent : layer.getAgents()) {
                Attribute health = new Attribute(healthPrefix + agent.getId());
                attributes.addElement(health);
                ((Machine)agent).setAttributes(attributes);
            }
        }

        FastVector result = new FastVector(2);
        result.addElement("GOOD");
        result.addElement("BAD");
        attributes.addElement(new Attribute("result", result));

        this.attributes = attributes;
    }

    /** Experiment main loop */
    public void run() throws Exception {
        PoissonDistribution random = new PoissonDistribution(3);
        Queue<Order> orders =
                MinMaxPriorityQueue.orderedBy(new OrderComparator()).maximumSize(Parameters.QUEUE_SIZE).create();// EvictingQueue.create(Parameters.QUEUE_SIZE);
        Order order;
        int[] products;
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

            //execute turn across layers and collect finished products
            addProducts(this.finishedProducts, tick(turnNo, products));

            //remove finished orders
            deliverOrders(orders, finishedProducts);

            for(Order order1 : orders) {
                order1.decreaseDueTime();
            }
        }
    }

    @Override
    protected int[] tick(final int turnNo, final int[] products) throws Exception {
        //execute turn across layers
        int[] products1 = products;
        for (LearningAgent layer : getAgents()) {
            products1 = layer.tick(turnNo, products1);
        }

        return products1;
    }

    @Override
    protected FastVector getAttributes() {
        return attributes;
    }

    /**
     * Removes orders from queue when all products are ready
     *
     * @return reward for completed orders
     */
    private int deliverOrders(final Queue<Order> orders, final int[] finishedProducts) throws Exception {
        int reward = 0;
        for (int i = 0; i < orders.size(); i++) {
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
            addTrainData(prepareTrainData());

            reward += order.getReward() + order.getValue();
            if(order.getDueTime() > 0) {
                reward -= order.getPenalty();
            }
        }

        return reward;
    }

    /** Prepares one entry in train set */
    private Instance prepareTrainData() {
        Instance instance = new SparseInstance(getAttributes().size());

        int attrIdx = 0;
        for(LearningAgent agent : getAgents()) {
            Layer layer = (Layer) agent;
            for(int i=1; i<=Parameters.PRODUCT_TYPES_NO; i++) {
                instance.setValue(attrIdx++, layer.getQuantityInBuffer(i));
            }
            for(LearningAgent agent1 : layer.getAgents()) {
                instance.setValue(attrIdx++, ((Machine)agent1).isBroken() ? 0 : 1);
            }
        }

        FastVector result = new FastVector(2);
        result.addElement("GOOD");
        result.addElement("BAD");
        attributes.addElement(new Attribute("result", result));
        return null;
    }

    /** Method generates new order. */
    private Order generateOrder() {
        Random random = new Random();
        int[] order = new int[Parameters.PRODUCT_TYPES_NO];

        int reward = Parameters.REWARD != 0 ? Parameters.REWARD : random.nextInt(10);
        int penalty = Parameters.PENALTY != 0 ? (int) (reward * Parameters.PENALTY) : random.nextInt(reward);

        return new Order(order, random.nextInt(10) + 8, reward, penalty, reward);
    }
}
