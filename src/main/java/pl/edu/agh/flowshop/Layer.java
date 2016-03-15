package pl.edu.agh.flowshop;

import pl.edu.agh.utils.Parameters;

import java.util.List;

/**
 * Class representing one layer of {@link Machine machines} in model.
 *
 * @author Bartosz
 *         Created on 2016-03-09.
 */
public class Layer {

    /** {@link Machine Machines} in layer. */
    private List<Machine> machines;

    /** Kolejki zadań */
    private int[] tasksQueue;

    /**
     * Constructor.
     *
     * @param machines       list of {@link Machine} objects in layer
     */
    public Layer(final List<Machine> machines) {
        this.machines = machines;
        this.tasksQueue = new int[Parameters.PRODUCT_TYPES_NO];
    }

    /** Simulates one turn for layer */
    public int[] tick(final int turnNo, final int[] newTasks) throws Exception {
        //TODO: adding training examples
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
        if (turnNo % Parameters.LEARNING_TURN == 0) {
            for (Machine machine : this.machines) {
                machine.train();
            }
        }

        //chance for changing processing product type
        for (Machine machine : this.machines) {
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

            int product = machine.isMachineBroken();
            if(product > 0) {
                this.tasksQueue[product] += 1;
                continue;
            }

            machine.tick();
        }

        return finishedProducts;
    }
}
