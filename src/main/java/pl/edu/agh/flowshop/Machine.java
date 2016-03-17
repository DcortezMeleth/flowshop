package pl.edu.agh.flowshop;

import pl.edu.agh.utils.Parameters;
import weka.core.Instances;

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

    /** Type of processed product */
    protected int productType;

    /** Turns left for product to be processed */
    protected int turnsLeft;

    /**
     * Machine configuration in form of map: </br>
     * <li>
     * <ul>key - product type</ul>
     * <ul>value - processing time</ul>
     * </li>
     */
    protected Map<Integer, Integer> timeTable;

    /** Constructor using configuration object. */
    public Machine(final Map<Integer, Integer> timeTable, final String classifierName) {
        super(classifierName, new ArrayList<LearningAgent>(), Parameters.MACHINE);
        this.timeTable = timeTable;
        this.trainSet = new Instances("TrainSet", attributes, 0);
    }

    /**
     * Starts processing product on this machine.
     *
     * @param productType type of product to process
     */
    public boolean processProduct(final int productType) {
        if (this.turnsLeft != 0 || this.productType != productType) {
            return false;
        }
        this.turnsLeft = this.timeTable.get(productType);
        return true;
    }

    /** Returns type of processed product. */
    public int getProcessed() {
        return turnsLeft == 0 ? productType : -1;
    }

    /** Decrements a counter of {@link #turnsLeft}. Used on begining of a turn. */
    public void tick() {
        this.turnsLeft--;
    }

    /**
     * Checks if machine should break this turn
     *
     * @return product type which machine was working on, -1 if it was idle
     */
    public int isMachineBroken() {
        //check if machine should break
        if (new Random().nextInt(100) > 5) {
            return -1;
        }

        return this.turnsLeft >= 0 && this.productType >= 0 ? this.productType : -1;
    }

    @Override
    protected void decideOnAction(final int action) throws Exception {
        //zmienic typ mozemy tylko gdy aktualnie czegos nie przetwarzamy
        if (this.turnsLeft != 0) {
            return;
        }
        int result = action >= 0 ? action : getAction();
        if (result != this.productType) {
            this.turnsLeft++;
        }
        this.productType = result;
    }

}
