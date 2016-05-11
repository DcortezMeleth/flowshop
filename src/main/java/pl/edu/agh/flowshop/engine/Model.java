package pl.edu.agh.flowshop.engine;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.MinMaxPriorityQueue;
import environment.ActionList;
import environment.IAction;
import environment.IEnvironment;
import environment.IState;
import org.apache.commons.math3.distribution.PoissonDistribution;
import pl.edu.agh.flowshop.entity.Action;
import pl.edu.agh.flowshop.entity.AgentState;
import pl.edu.agh.flowshop.entity.Order;
import pl.edu.agh.flowshop.utils.Attributes;
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
public class Model implements IEnvironment {

    public static final int MAX_ORDER_SIZE = 5;

    public static final int MAX_REWARD_VALUE = 10;

    public static final int DUE_TIME_RAND_MAX = 10;

    public static final int DUE_TIME_MIN_VALUE = 8;

    public static final int MIN_ORDER_SIZE = 3;

    /** Model history used for learning */
    private final ModelHistory history;

    /** Buffer of finished products waiting for delivery */
    private int[] finishedProducts = new int[Parameters.PRODUCT_TYPES_NO];

    /** Layers inside model. */
    private List<Layer> layers;

    private List<Order> finishedOrders = new ArrayList<>();

    public Model(final List<Layer> layers) {
        this.history = new ModelHistory();
        this.layers = layers;

        /** Init attributes */
        Attributes.initAttributes(this);
    }

    /**
     * Experiment main loop
     *
     * @return return queue sizes from each iteration
     */
    public List<Double> run() throws Exception {
        PoissonDistribution random = new PoissonDistribution(3);
        Queue<Order> orders =
                MinMaxPriorityQueue.orderedBy(new OrderComparator()).maximumSize(Parameters.QUEUE_SIZE).create();
        Order order;
        int[] products;
        int newOrderTurn = random.sample();
        List<Double> queueSizes = new ArrayList<>();

        //main loop
        for (int turnNo = 0; turnNo < Parameters.TURN_LIMIT; turnNo++) {
            //train on collected data
            if (turnNo % Parameters.LEARNING_TURN == 0) {
                for (Layer layer : this.layers) {
                    for (Machine machine : layer.getMachines()) {
                        machine.train();
                    }
                }
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
            int[] finishedProducts = products;
            for (Layer layer : this.layers) {
                finishedProducts = layer.tick(turnNo, finishedProducts);
            }

            addArrayElements(this.finishedProducts, finishedProducts);

            //remove finished orders
            deliverOrders(orders, this.finishedProducts);

            for (Order order1 : orders) {
                order1.decreaseDueTime();
            }

            queueSizes.add(getQueuesSize());
            System.out.println("Orders size:" + orders.size());
            System.out.println("Finished orders size:" + finishedOrders.size());
        }

        return queueSizes;
    }

    @Override
    public ActionList getActionList(final IState iState) {
        ActionList result = new ActionList(iState);
        for (int productNo = 0; productNo < Parameters.PRODUCT_TYPES_NO; productNo++) {
            for (Layer layer : this.layers) {
                for (Machine machine : layer.getMachines()) {
                    if (!machine.isWorking()) {
                        result.add(new Action(machine.getId(), productNo));
                    }
                }
            }
        }

        return result;
    }

    @Override
    public IState successorState(final IState iState, final IAction iAction) {
        AgentState state = (AgentState) iState;
        Action action = (Action) iAction;

        AgentState result = new AgentState(this);
        result.setAttrValues(getAttributesValues(true, state.getAttrValues(), action.getAgentNo(),
                action.getProductToProcess()));

        return result;
    }

    @Override
    public double getReward(final IState iState, final IState iState1, final IAction iAction) {
        int[] buffersBefore = new int[Parameters.PRODUCT_TYPES_NO];
        int[] buffersAfter = new int[Parameters.PRODUCT_TYPES_NO];

        for (int i = 0; i < Attributes.size(); i++) {
            Attribute attr = (Attribute) Attributes.attributes.elementAt(i);
            String attrName = attr.name();
            if (attrName.contains(Attributes.BUFFER_PREFIX)) {
                int productNo = Character.getNumericValue(attrName.charAt(attrName.length()));
                buffersBefore[productNo] += ((AgentState) iState).getAttrValues().get(i);
                buffersAfter[productNo] += ((AgentState) iState1).getAttrValues().get(i);
            }
        }

        int result = 0;
        for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
            result += buffersBefore[i] - buffersAfter[i];
        }

        return result;
    }

    @Override
    public boolean isFinal(final IState iState) {
        return false;
    }

    @Override
    public int whoWins(final IState iState) {
        return 0;
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
    private Double getQueuesSize() {
        double result = 0;
        for (Layer layer : this.layers) {
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
                if (machine.getProductToBeProcessed() > -1) {
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
            finishedOrders.add(orders.poll());

            reward += order.getReward() + order.getValue();
            if (order.getDueTime() > 0) {
                reward -= order.getPenalty();
            }

            this.history.addEntry();
            for (Layer layer : this.layers) {
                for (Machine machine : layer.getMachines()) {
                    machine.addTrainData(this.history.getTrainingExample(reward));
                }
            }
        }

        return reward;
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

    /** Adds products from list2 to list1 */
    private void addArrayElements(final int[] list1, final int[] list2) {
        for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
            list1[i] += list2[i];
        }
    }

    /**
     * Data instance used for holding model history.
     *
     * @author Bartosz
     *         Created on 2016-04-20.
     */
    public class ModelHistory {

        private Queue<AgentState> entries = EvictingQueue.create(Parameters.USED_HISTORY);

        /** Adds entry to {@link #entries} set */
        public void addEntry() {
            List<Integer> entry = new ArrayList<>();
            for (Layer layer : layers) {
                for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
                    entry.add(layer.getQuantityInBuffer(i));
                }
                for (Machine machine : layer.getMachines()) {
                    entry.add(machine.isBroken() ? 0 : 1);
                }
            }
            AgentState state = new AgentState(Model.this);
            state.setAttrValues(entry);
            this.entries.add(state);
        }

        /** Creates instance of training data based on model history */
        public Instance getTrainingExample(final int reward) {
            Instance instance = new SparseInstance(Attributes.size());

            int attrIdx = 0;
            for (AgentState entry : this.entries) {
                for (Integer val : entry.getAttrValues()) {
                    instance.setValue(attrIdx, val);
                }
            }
            instance.setValue((Attribute) Attributes.attributes.lastElement(), reward > Parameters.DECISION_THRESHOLD ? "GOOD" : "BAD");

            return instance;
        }

    }
}
