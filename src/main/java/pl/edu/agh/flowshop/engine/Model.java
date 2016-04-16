package pl.edu.agh.flowshop.engine;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.MinMaxPriorityQueue;
import environment.ActionList;
import environment.IAction;
import environment.IEnvironment;
import environment.IState;
import org.apache.commons.math3.distribution.PoissonDistribution;
import pl.edu.agh.flowshop.entity.Order;
import pl.edu.agh.flowshop.utils.AttributesInitializer;
import pl.edu.agh.flowshop.utils.OrderComparator;
import pl.edu.agh.flowshop.utils.Parameters;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.SparseInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Represents whole model in experiment.
 *
 * @author Bartosz Sądel
 *         Created on 11.03.2016.
 */
public class Model extends LearningAgent implements IEnvironment {

    /** Model history used for learning */
    private final ModelHistory history;

    /** Buffer of finished products waiting for delivery */
    private int[] finishedProducts = new int[Parameters.PRODUCT_TYPES_NO];

    public Model(final List<Layer> layers, final String classifierName) {
        super(classifierName, layers, Parameters.MODEL);
        this.history = new ModelHistory();

        /** Init attributes */
        AttributesInitializer.initAttributes(this);
    }

    /** Experiment main loop */
    public void run() throws Exception {
        PoissonDistribution random = new PoissonDistribution(3);
        Queue<Order> orders =
                MinMaxPriorityQueue.orderedBy(new OrderComparator()).maximumSize(Parameters.QUEUE_SIZE).create();//
        // EvictingQueue.create(Parameters.QUEUE_SIZE);
        Order order;
        int[] products;
        int newOrderTurn = random.sample();

        //main loop
        for (int turnNo = 0; turnNo < Parameters.TURN_LIMIT; turnNo++) {
            //train on collected data
            if (turnNo % Parameters.LEARNING_TURN == 0 && turnNo != 0) {
                train();
            }

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

            for (Order order1 : orders) {
                order1.decreaseDueTime();
            }
        }
    }

    @Override
    public ActionList getActionList(final IState iState) {
        return null;
    }

    @Override
    public IState successorState(final IState iState, final IAction iAction) {
        return null;
    }

    @Override
    public double getReward(final IState iState, final IState iState1, final IAction iAction) {
        return 0;
    }

    @Override
    public boolean isFinal(final IState iState) {
        return false;
    }

    @Override
    public int whoWins(final IState iState) {
        return 0;
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

    /** Prepares entry for decision */
    protected Instance prepareInstanceForDecision() {
        Instance instance = new SparseInstance(getAttributes().size() - 1);

        setAttributesValues(instance);

        return instance;
    }

    /** Set attributes values in given instance */
    private void setAttributesValues(final Instance instance) {
        int attrIdx = 0;
        for (LearningAgent agent : getAgents()) {
            Layer layer = (Layer) agent;
            for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
                instance.setValue(attrIdx++, layer.getQuantityInBuffer(i));
            }
            for (LearningAgent agent1 : layer.getAgents()) {
                instance.setValue(attrIdx++, ((Machine) agent1).isBroken() ? 0 : 1);
            }
        }
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

            reward += order.getReward() + order.getValue();
            if (order.getDueTime() > 0) {
                reward -= order.getPenalty();
            }

            this.history.addEntry(getAgents());
            addTrainData(this.history.getTrainingExample(reward));

            train();
        }

        return reward;
    }

    /** Method generates new order. */
    private Order generateOrder() {
        Random random = new Random();
        int[] order = new int[Parameters.PRODUCT_TYPES_NO];

        int reward = Parameters.REWARD != 0 ? Parameters.REWARD : random.nextInt(10);
        int penalty = Parameters.PENALTY != 0 ? (int) (reward * Parameters.PENALTY) : random.nextInt(reward);

        return new Order(order, random.nextInt(10) + 8, reward, penalty, reward);
    }

    /**
     * Data instance used for holding model history.
     */
    private class ModelHistory {

        private Queue<List<Integer>> entries = EvictingQueue.create(Parameters.USED_HISTORY);

        /** Adds entry to {@link #entries} set */
        public void addEntry(final List<? extends LearningAgent> agents) {
            List<Integer> entry = new ArrayList<>();
            for (LearningAgent agent : agents) {
                Layer layer = (Layer) agent;
                for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
                    entry.add(layer.getQuantityInBuffer(i));
                }
                for (LearningAgent agent1 : layer.getAgents()) {
                    entry.add(((Machine) agent1).isBroken() ? 0 : 1);
                }
            }
            this.entries.add(entry);
        }

        /** Creates instance of training data based on model history */
        public Instance getTrainingExample(final int reward) {
            Instance instance = new SparseInstance(getAttributes().size());

            int attrIdx = 0;
            for (List<Integer> entry : this.entries) {
                for (Integer val : entry) {
                    instance.setValue(attrIdx, val);
                }
            }
            instance.setValue((Attribute) getAttributes().lastElement(), reward > Parameters.DECISION_THRESHOLD ? "GOOD" : "BAD");

            return instance;
        }
    }
}