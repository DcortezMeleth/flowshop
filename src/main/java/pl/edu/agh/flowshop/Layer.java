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
    private final int learningTurn;

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
    public Layer(final List<Machine> machines, final int productTypesNo, final int learningTurn) {
        this.machines = machines;
        this.tasksQueue = new int[productTypesNo];
        this.learningTurn = learningTurn;
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
            if (machine.getProcessed() > 0) {
                finishedProducts[machine.getProcessed()] += 1;
            }
        }

        //train on collected data
        if (turnNo % this.learningTurn == 0) {
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
