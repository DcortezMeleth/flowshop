package pl.edu.agh.flowshop.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.flowshop.utils.Parameters;

import java.util.List;

/**
 * Class representing one layer of {@link Machine machines} in model.
 *
 * @author Bartosz
 *         Created on 2016-03-09.
 */
public class Layer {

    private final static Logger logger = LogManager.getLogger(Layer.class);

    /** layers counter */
    private static int count = 0;

    /** layer */
    private final int id;

    /** Tasks queue */
    private int[] buffer;

    /** Model to whom layer belongs */
    private Model model;

    /** machines list */
    private List<Machine> machines;

    public Layer(final List<Machine> machines) {
        this.buffer = new int[Parameters.PRODUCT_TYPES_NO];
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
        return this.buffer[productType];
    }

    public int[] getBuffer() {
        return buffer;
    }

    /** Returns summed number of products in queue */
    public int getQueueSize() {
        int result = 0;
        for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
            result += buffer[i];
        }
        return result;

    }

    public List<Machine> getMachines() {
        return machines;
    }

    public int[] tick(final int[] newTasks) throws Exception {
        logger.debug("Layer " + id + " tick!");
        //add new tasks to queue
        for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
            this.buffer[i] += newTasks[i];
        }

        //chance for changing processing product type
        for (Machine machine : this.machines) {
            logger.debug("Layer " + id + " decision time.");
            machine.decideOnAction(model.prepareInstanceForDecision(), this.buffer);
        }

        //tick for machines
        int[] finishedProducts = new int[Parameters.PRODUCT_TYPES_NO];
        for (Machine machine : this.machines) {
            int result = machine.tick(this.buffer);
            if(result > -1) {
                finishedProducts[result] += 1;
            }
        }


        StringBuilder sb = new StringBuilder("");
        for (int product : finishedProducts) {
            sb.append(product).append(",");
        }
        logger.debug("Finished products for layer " + id + ": [" + sb + "]");
        return finishedProducts;
    }

}
