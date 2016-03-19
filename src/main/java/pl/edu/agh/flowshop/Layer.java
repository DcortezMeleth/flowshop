package pl.edu.agh.flowshop;

import pl.edu.agh.utils.Parameters;
import weka.core.FastVector;

import java.util.List;

/**
 * Class representing one layer of {@link Machine machines} in model.
 *
 * @author Bartosz
 *         Created on 2016-03-09.
 */
public class Layer extends LearningAgent {

    /** Vector of {@link weka.core.Attribute Attributes} used for learning */
    private FastVector attributes;

    /** Tasks queue */
    private int[] tasksQueue;

    public Layer(final List<Machine> machines, final String classifierName) {
        super(classifierName, machines, Parameters.LAYER);
        this.tasksQueue = new int[Parameters.PRODUCT_TYPES_NO];
    }

    @Override
    public FastVector getAttributes() {
        return attributes;
    }

    public void setAttributes(final FastVector attributes) {
        this.attributes = attributes;
    }

    @Override
    public int[] tick(final int turnNo, final int[] newTasks) throws Exception {
        //add new tasks to queue
        addProducts(this.tasksQueue, newTasks);

        //train on collected data
        if (turnNo % Parameters.LEARNING_TURN == 0) {
            train();
        }

        //chance for changing processing product type
        decideOnAction(Parameters.LEARNING_LEVEL);

        //tick for machines
        int[] finishedProducts = new int[Parameters.PRODUCT_TYPES_NO];
        for (LearningAgent machine : getAgents()) {
            addProducts(finishedProducts, machine.tick(turnNo, this.tasksQueue));
        }

        return finishedProducts;
    }

}
