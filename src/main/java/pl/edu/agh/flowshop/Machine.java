package pl.edu.agh.flowshop;

import pl.edu.agh.utils.Parameters;
import weka.core.FastVector;
import weka.core.Instance;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * Machine processing tasks
 *
 * @author Bartosz Sądel
 *         Created on 02.02.2016.
 */
public class Machine extends LearningAgent {

    /** Vector of {@link weka.core.Attribute Attributes} used for learning */
    private FastVector attributes;

    /** Type of processed product */
    private int productType = -1;

    /** Turns left for product to be processed */
    private int turnsLeft;

    /** Indicates if machine is broken */
    private boolean broken = false;

    /**
     * Machine configuration in form of map: </br>
     * <li>
     * <ul>key - product type</ul>
     * <ul>value - processing time</ul>
     * </li>
     */
    private Map<Integer, Integer> timeTable;

    public Machine(final Map<Integer, Integer> timeTable, final String classifierName) {
        super(classifierName, new ArrayList<LearningAgent>(), Parameters.MACHINE);
        this.timeTable = timeTable;
    }

    @Override
    public FastVector getAttributes() {
        return attributes;
    }

    public void setAttributes(final FastVector attributes) {
        this.attributes = attributes;
    }

    public boolean isBroken() {
        return broken;
    }

    @Override
    protected int[] tick(final int turnNo, final int[] newTasks) throws Exception {
        int[] processed = new int[Parameters.PRODUCT_TYPES_NO];
        if (shouldMachineBreak()) {
            //return processed product to queue
            newTasks[this.productType] += 1;
            this.productType = -1;
            this.turnsLeft = 1;
            return processed;
        }

        //take task from queue
        if (this.turnsLeft <= 0) {
            newTasks[this.productType] -= 1;
            this.turnsLeft = this.timeTable.get(this.productType + 1);
        }

        this.turnsLeft--;

        //finished product is moved to finishedProduct field
        if (this.turnsLeft <= 0 && this.productType > -1) {
            processed[this.productType] += 1;
        }

        return processed;
    }

    @Override
    protected void decideOnAction(final int action, final Instance instance) throws Exception {
        //changing production type while working is forbidden
        if (this.turnsLeft > 0) {
            return;
        }

        //we check whether we should generate choice or its generated above
        int result = action >= 0 ? action : getAction(instance);
        if (result != this.productType) {
            this.turnsLeft++;
        }
        this.productType = result;
    }

    /**
     * Checks if machine should break this turn
     *
     * @return product type which machine was working on, -1 if it was idle
     */
    private boolean shouldMachineBreak() {
        if (this.broken || this.turnsLeft <= 0 || this.productType < 0) {
            return false;
        }

        //check if machine should break
        return (this.broken = new Random().nextInt(100) > 5);
    }

}
