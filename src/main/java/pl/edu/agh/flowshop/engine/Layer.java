package pl.edu.agh.flowshop.engine;

import pl.edu.agh.flowshop.utils.Parameters;

import java.util.List;

/**
 * Class representing one layer of {@link Machine machines} in model.
 *
 * @author Bartosz
 *         Created on 2016-03-09.
 */
public class Layer extends LearningAgent {

    /** Tasks queue */
    private int[] tasksQueue;

    /** Model to whom layer belongs */
    private Model model;

    public Layer(final List<Machine> machines, final String classifierName) {
        super(classifierName, machines, Parameters.LAYER);
        this.tasksQueue = new int[Parameters.PRODUCT_TYPES_NO];
    }

    public void setModel(final Model model) {
        this.model = model;
    }

    /** Returns quantity of product type in buffer */
    public int getQuantityInBuffer(final int productType) {
        return this.tasksQueue[productType];
    }

    @Override
    public int[] tick(final int turnNo, final int[] newTasks) throws Exception {
        //add new tasks to queue
        addProducts(this.tasksQueue, newTasks);

        //chance for changing processing product type
        model.decideOnAction(Parameters.LEARNING_LEVEL, model.prepareInstanceForDecision());

        //tick for machines
        int[] finishedProducts = new int[Parameters.PRODUCT_TYPES_NO];
        for (LearningAgent machine : getAgents()) {
            addProducts(finishedProducts, machine.tick(turnNo, this.tasksQueue));
        }

        return finishedProducts;
    }

}
