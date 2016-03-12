package pl.edu.agh.flowshop;

import java.util.List;

/**
 * Class representing one layer of {@link Machine machines} in model.
 *
 * @author Bartosz
 *         Created on 2016-03-09.
 */
public class Layer {

    /** Number of turn between learning process */
    private static final int LEARNING_TURN = 20;

    /** {@link Machine Machines} in layer. */
    private List<Machine> machines;

    /** Kolejki zadań */
    private int[] tasksQueue;

    /**
     * Constructor.
     *
     * @param machines       list of {@link Machine} objects in layer
     * @param productTypesNo number of types of product used in simulation
     */
    public Layer(final List<Machine> machines, final int productTypesNo) {
        this.machines = machines;
        this.tasksQueue = new int[productTypesNo];
    }

    /** Adds produkt of type <code>productType</code> to queue */
    public void addProductToQueue(final int productType) {
        this.tasksQueue[productType - 1] += 1;
    }

    /** Simulates one turn for layer */
    public int[] tick(final int turnNo, final int[] newTasks) throws Exception {
        //add new tasks to queue
        for (int i = 0; i < this.tasksQueue.length; i++) {
            this.tasksQueue[i] += newTasks[i];
        }

        //collect finished products
        int[] finishedProducts = new int[tasksQueue.length];
        for (Machine machine : this.machines) {
            if (machine.getProcessed() != null) {
                finishedProducts[machine.getProcessed()] += 1;
            }
        }

        //train on collected data
        if (turnNo % LEARNING_TURN == 0) {
            for (Machine machine : this.machines) {
                machine.train();
            }
        }

        //chance for changing processing product type
        for(Machine machine : this.machines){
            machine.decideOnAction();
        }

        //offer tasks to machines and tick
        for (Machine machine : this.machines) {
            for (int i = 0; i < this.tasksQueue.length; i++) {
                if (this.tasksQueue[i] > 0) {
                    if (machine.processProduct(i)) {
                        this.tasksQueue[i] -= 1;
                        break;
                    }
                }
            }

            machine.tick();
        }

        return finishedProducts;
    }
}