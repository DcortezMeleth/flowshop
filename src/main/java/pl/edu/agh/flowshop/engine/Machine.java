package pl.edu.agh.flowshop.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.flowshop.utils.Parameters;
import weka.core.Instance;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Machine processing tasks
 *
 * @author Bartosz Sądel
 *         Created on 02.02.2016.
 */
public class Machine extends LearningAgent {

    private final static Logger logger = LogManager.getLogger(Machine.class);

    /** Type of processed product */
    private int productType = -1;

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
        super();
    }

    public boolean isBroken() {
        return this.broken;
    }

    public boolean isWorking() {
        return this.turnsLeft > 0 && !this.broken;
    }

    /** Returns product machine is going to finish in next turn. -1 if it won't finish anyting */
    public int getProductToBeProcessed() {
        if (this.turnsLeft <= 1 && this.productType > -1) {
            return this.productType;
        }
        return -1;
    }

    /** Simulates one turn for agent */
    protected int tick(final int[] newTasks) throws Exception {
        logger.debug("Machine " + getId() + " tick!");
        if (shouldMachineBreak()) {
            logger.debug("Machine " + getId() + " broken!");
            //return processed product to queue
            if(this.productType != 0) {
                newTasks[this.productType] += 1;
            }
            this.productType = -1;
            this.turnsLeft = 1;
            return -1;
        }

        //take task from queue
        if(this.productType > -1) {
            if (this.turnsLeft <= 0 && newTasks[this.productType] > 0) {
                logger.debug("Machine " + getId() + " takes task from queue!");
                newTasks[this.productType] -= 1;
                this.turnsLeft = this.timeTable.get(this.productType);
            }

            this.turnsLeft--;

            //finished product is moved to finishedProduct field
            if (this.turnsLeft <= 0) {
                logger.debug("Machine " + getId() + " finishes product " + this.productType);
                return this.productType;
            }
        }

        return -1;
    }

    /**
     * Classifies given example based on {@link #classifier} decision.
     *
     * @param instance instance to decide on
     * @throws Exception
     */
    protected void decideOnAction(final Instance instance) throws Exception {
        //changing production type while working is forbidden
        if (this.turnsLeft > 0) {
            logger.debug("Machine " + getId() + " still working!");
            return;
        }

        int actionToChoose = getAction(instance);

        // zmienilismy typ -> czekamy ture
        if (actionToChoose != this.productType) {
            logger.debug("Machine " + getId() + " changed to " + actionToChoose);
            this.turnsLeft++;
        } else {
            logger.debug("Machine " + getId() + " producing same product.");
        }

        this.productType = actionToChoose;
    }

    /** Checks if machine should break this turn */
    private boolean shouldMachineBreak() {
        if (this.broken || this.turnsLeft <= 0 || this.productType < 0) {
            return false;
        }

        //check if machine should break
        return (this.broken = new Random().nextInt(100) < 5);
    }

}
