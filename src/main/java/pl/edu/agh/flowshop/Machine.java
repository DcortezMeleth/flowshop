package pl.edu.agh.flowshop;

import pl.edu.agh.utils.Parameters;

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
    private int productType = -1;

    /** Type if product finished and waitning for delivery. -1 otherwise */
    private int finishedProduct = -1;

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

    public boolean isBroken() {
        return broken;
    }

    @Override
    protected int[] tick(final int trunNo, final int[] newTasks) throws Exception {
        int[] processed = new int[Parameters.PRODUCT_TYPES_NO];
        if (shouldMachineBreak()) {
            //return processed prodcut to queue
            newTasks[this.productType] += 1;
            this.productType = -1;
            this.turnsLeft = 1;
            return processed;
        }

        if (this.finishedProduct > -1) {
            processed[this.finishedProduct] += 1;
            this.finishedProduct = -1;
        }

        this.turnsLeft--;
        return processed;
    }

    @Override
    protected void decideOnAction(final int action) throws Exception {
        //zmienic typ mozemy tylko gdy aktualnie czegos nie przetwarzamy
        if (this.turnsLeft > 0) {
            return;
        }

        //jesli produkt skonczony to laduje w innym polu
        if (this.productType > -1) {
            this.finishedProduct = this.productType;
        }

        //sprawdzamy czy generujamy akcje czy wygenerowano ja wyzej
        int result = action >= 0 ? action : getAction();
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
