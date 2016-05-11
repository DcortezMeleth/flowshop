package pl.edu.agh.flowshop.engine;

import pl.edu.agh.flowshop.utils.Parameters;

import java.util.List;

/**
 * Class representing one layer of {@link Machine machines} in model.
 *
 * @author Bartosz
 *         Created on 2016-03-09.
 */
public class Layer {

    /** layers counter */
    private static int count = 0;

    /** layer */
    private final int id;

    /** Tasks queue */
    private int[] tasksQueue;

    /** Model to whom layer belongs */
    private Model model;

    /** machines list */
    private List<Machine> machines;

    public Layer(final List<Machine> machines) {
        this.tasksQueue = new int[Parameters.PRODUCT_TYPES_NO];
        this.machines = machines;
        this.id = ++count;
    }

    public void setModel(final Model model) {
        this.model = model;
    }

    public int getId() {
        return id;
    }

    /** Returns quantity of product type in buffer */
    public int getQuantityInBuffer(final int productType) {
        return this.tasksQueue[productType];
    }

    /** Returns summed number of products in queue */
    public int getQueueSize() {
        int result = 0;
        for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
            result += tasksQueue[i];
        }
        return result;
    }

    public List<Machine> getMachines() {
        return machines;
    }

    public int[] tick(final int turnNo, final int[] newTasks) throws Exception {
        //add new tasks to queue
        addArrayElements(this.tasksQueue, newTasks);

        //chance for changing processing product type
        for (Machine machine : this.machines) {
            machine.decideOnAction(model.prepareInstanceForDecision());
        }

        //tick for machines
        int[] finishedProducts = new int[Parameters.PRODUCT_TYPES_NO];
        for (Machine machine : this.machines) {
            addArrayElements(finishedProducts, machine.tick(turnNo, this.tasksQueue));
        }

        return finishedProducts;
    }

    /** Adds products from list2 to list1 */
    private void addArrayElements(final int[] list1, final int[] list2) {
        for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
            list1[i] += list2[i];
        }
    }
}
