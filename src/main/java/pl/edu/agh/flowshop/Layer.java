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
        tasksQueue[productType - 1] += 1;
    }

    /** Simulates one turn for layer */
    public int[] tick(final int turnNo, final int[] newTasks) throws Exception {
        //add new tasks to queue
        for(int i=0; i<this.tasksQueue.length; i++) {
            this.tasksQueue[i] += newTasks[i];
        }

        //collect finished products
        int[] finishedProducts = new int[tasksQueue.length];
        for(Machine machine : machines) {
            if(machine.getProcessed() != null) {
                finishedProducts[machine.getProcessed()] += 1;
            }
        }

        if(turnNo % LEARNING_TURN == 0) {
            for(Machine machine : machines) {
                machine.train();
            }
        }

        //offer tasks to machines


        //tick
        for(Machine machine : machines) {
            machine.tick();
        }


        return finishedProducts;
    }
}
