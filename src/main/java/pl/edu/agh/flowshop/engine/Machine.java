package pl.edu.agh.flowshop.engine;

import agents.AbstractAgent;
import algorithms.PengSelector;
import algorithms.QLearningSelector;
import algorithms.WatkinsSelector;
import environment.ActionList;
import environment.IState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.flowshop.entity.Action;
import pl.edu.agh.flowshop.entity.AgentState;
import pl.edu.agh.flowshop.utils.Parameters;

import java.util.*;

/**
 * Machine processing tasks
 *
 * @author Bartosz Sądel
 *         Created on 02.02.2016.
 */
public class Machine extends AbstractAgent {

    private final static Logger logger = LogManager.getLogger(Machine.class);

    /** Agents counter */
    private static int counter = 0;

    /** Agent id */
    private final int id;

    /** Classifier name */
    protected String classifierName = "";

    /** Type of processed product */
    private int productType = -1;

    private boolean processing = false;

    /** Turns left for product to be processed */
    private int turnsLeft = 0;

    /** Indicates if machine is broken */
    private boolean broken = false;

    /**
     * Machine configuration in form of map: </br>
     * <li>
     * <ul>key - product type</ul>
     * <ul>value - processing time</ul>
     * </li>
     */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Map<Integer, Integer> timeTable = new HashMap<>();

    public Machine() {
        super(null, null);
        this.id = counter++;
    }

    public boolean isBroken() {
        return this.broken;
    }

    public boolean isWorking() {
        return this.turnsLeft > 0 && !this.broken;
    }

    /** Assigns classifier based on its name from config */
    public void init(final Model model) {
        switch (this.classifierName) {
            case "QLearning":
                this.setAlgorithm(new QLearningSelector());
                break;
            case "Watkins":
                this.setAlgorithm(new WatkinsSelector(Parameters.LAMBDA));
                break;
            case "Peng":
            default:
                this.setAlgorithm(new PengSelector(Parameters.LAMBDA));
                break;
        }

        // initialization of reinforcement learning params
        QLearningSelector strategy = (QLearningSelector) getAlgorithm();
        strategy.setEpsilon(Parameters.EPSILON);
        strategy.setGamma(Parameters.GAMMA);

        setUniverse(model);
        setCurrentState(getInitState(model));
    }

    public int getId() {
        return this.id;
    }

    /** Simulates one turn for agent */
    protected int tick(final int[] newTasks) throws Exception {
        logger.debug("Machine " + getId() + " tick!");
        if (shouldMachineBreak()) {
            logger.debug("Machine " + getId() + " broken!");
            //return processed product to queue
            if (this.productType != 0) {
                newTasks[this.productType] += 1;
            }
            this.productType = -1;
            this.turnsLeft = 1;
            this.processing = false;
            return -1;
        }

        //take task from queue
        if (this.productType > -1) {
            if (this.turnsLeft <= 0 && newTasks[this.productType] > 0) {
                logger.debug("Machine " + getId() + " takes task from queue!");
                newTasks[this.productType] -= 1;
                this.turnsLeft = this.timeTable.get(this.productType);
                this.processing = true;
            }

            this.turnsLeft--;

            //finished product is moved to finishedProduct field
            if (this.turnsLeft <= 0 && this.processing) {
                this.processing = false;
                logger.debug("Machine " + getId() + " finishes product " + this.productType);
                return this.productType;
            }
        }

        return -1;
    }

    /**
     * Classifies given example based on learning algorithm decision.
     *
     * @throws Exception
     */
    protected void decideOnAction() throws Exception {
        //changing production type while working is forbidden
        if (this.turnsLeft > 0 && this.processing) {
            logger.debug("Machine " + getId() + " still working on " + this.productType + " + !");
            return;
        }

        Action action = (Action) act();
        int actionToChoose = action.getProductToProcess();

        if (actionToChoose != this.productType) {
            logger.debug("Machine " + getId() + " changed to " + actionToChoose);
            this.turnsLeft++;
        }

        this.productType = actionToChoose;
    }

    @Override
    protected ActionList getActionList() {
        ActionList result = new ActionList(getCurrentState());
        for (int productNo = 0; productNo < Parameters.PRODUCT_TYPES_NO; productNo++) {
            result.add(new Action(id, productNo));
        }

        return result;
    }

    /** Checks if machine should break this turn */
    private boolean shouldMachineBreak() {
        if (this.broken || this.turnsLeft <= 0 || this.productType < 0) {
            return false;
        }

        //check if machine should break
        return (this.broken = new Random().nextInt(100) < 5);
    }

    private IState getInitState(final Model model) {
        AgentState state = new AgentState(model);
        int machinesNo=0;
        for(Layer layer : model.getLayers()) {
            machinesNo += layer.getMachines().size();
        }
        boolean[] health = new boolean[machinesNo];
        for(int i=0; i<health.length; i++) {
            health[i] = true;
        }
        state.setMachinesHealth(health);
        state.setProductsInBuffers(new int[model.getLayers().size() * Parameters.PRODUCT_TYPES_NO]);
        return state;
    }

}
