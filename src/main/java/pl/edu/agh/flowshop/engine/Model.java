package pl.edu.agh.flowshop.engine;

import environment.ActionList;
import environment.IAction;
import environment.IEnvironment;
import environment.IState;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.flowshop.entity.Action;
import pl.edu.agh.flowshop.entity.AgentState;
import pl.edu.agh.flowshop.entity.Order;
import pl.edu.agh.flowshop.utils.Parameters;

import java.util.*;

/**
 * Represents whole model in experiment.
 *
 * @author Bartosz Sądel
 *         Created on 11.03.2016.
 */
public class Model implements IEnvironment {

    public static final int MAX_REWARD_VALUE = 10;

    public static final int DUE_TIME_RAND_MAX = 10;

    public static final int DUE_TIME_MIN_VALUE = 8;

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

            //generate new order
            if (turnNo == newOrderTurn) {
                order = generateOrder();
                orders.add(order);
                products = order.getProductsList();
                newOrderTurn += random.sample() + 1;
                logger.debug("Order generated: " + order.toString());
            } else {
                products = new int[Parameters.PRODUCT_TYPES_NO];
            }

            //execute turn across layers and collect finished products
            int[] finishedProducts = products;
            for (Layer layer : this.layers) {
                finishedProducts = layer.tick(finishedProducts);
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
        AgentState agentState = (AgentState) iState;
        Action action = (Action) iAction;

        int[] buffers = new int[agentState.getProductsInBuffers().length];
        boolean[] health = new boolean[agentState.getMachinesHealth().length];

        int i = 0;
        int j = 0;
        int targetLayer = 0;
        for (Layer layer : this.layers) {
            for (Machine machine : layer.getMachines()) {
                if(machine.getId() == action.getAgentNo()) {
                    targetLayer = j;
                }
                health[i++] = !machine.isBroken();
            }

            System.arraycopy(layer.getBuffer(), 0, buffers, j * Parameters.PRODUCT_TYPES_NO, layer.getBuffer().length);
            j++;
        }

        int bufferIdx = targetLayer * Parameters.PRODUCT_TYPES_NO + action.getProductToProcess();
        buffers[bufferIdx] -= buffers[bufferIdx] > 0 ? 1 : 0;


        AgentState state = new AgentState(this);
        state.setMachinesHealth(health);
        state.setProductsInBuffers(buffers);
        return state;
    }

    @Override
    public double getReward(final IState iState, final IState iState1, final IAction iAction) {
        AgentState before = (AgentState) iState;
        AgentState after = (AgentState) iState1;

        int reward = 0;
        for (int i = 0; i < before.getProductsInBuffers().length; i++) {
            reward += before.getProductsInBuffers()[i] - after.getProductsInBuffers()[i];
        }

        return reward;
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

    /**
     * Removes orders from queue when all products are ready
     *
     * @return reward for completed orders
     */
    private int deliverOrders(final List<Order> orders, final int[] finishedProducts) throws Exception {
        int reward = 0;
        logger.debug("Delivering orders!");
        logger.debug("Orders queue size: " + orders.size());
        StringBuilder sb = new StringBuilder("");
        for (int product : finishedProducts) {
            sb.append(product).append(",");
        }
        logger.debug("Finished products: [" + sb + "]");
        for (Iterator<Order> it = orders.iterator(); it.hasNext(); ) {
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
            if (!completed) {
                // not completed order -> proceed to next one
                continue;
            }

            //order finished -> remove from queue
            for (int j = 0; j < finishedProducts.length; j++) {
                finishedProducts[j] -= demandedProducts[j];
                if (finishedProducts[j] < 0) {
                    throw new Exception("Negative finished products: " + finishedProducts[j]);
                }
            }
            finishedOrders.add(order);
            it.remove(); // removing finished order from list
            logger.debug("Order finished: " + order.toString());

            reward += order.getReward() + order.getValue();
            if (order.getDueTime() > 0) {
                logger.debug("Penalty received");
                reward -= order.getPenalty();
            }
        }

        logger.debug("Finished orders count: " + finishedOrders.size());

        return reward;
    }

    /** Method generates new order. */
    private Order generateOrder() {
        Random random = new Random();
        int[] order = new int[Parameters.PRODUCT_TYPES_NO];
        order[random.nextInt(order.length)] = 1;//random.nextInt(MAX_ORDER_SIZE - MIN_ORDER_SIZE) + MIN_ORDER_SIZE;

        int reward = Parameters.REWARD != 0 ? Parameters.REWARD : random.nextInt(MAX_REWARD_VALUE);
        int penalty = Parameters.PENALTY != 0 ? (int) (reward * Parameters.PENALTY) : random.nextInt(reward);

        return new Order(order, random.nextInt(DUE_TIME_RAND_MAX) + DUE_TIME_MIN_VALUE, reward, penalty, reward);
    }
}
