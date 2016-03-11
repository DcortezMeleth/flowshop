package pl.edu.agh.flowshop;

import java.util.List;
import java.util.Random;

/**
 * Represents whole model in experiment.
 *
 * @author Bartosz Sądel
 * Created on 11.03.2016.
 */
public class Model {

    /** List of machine layers */
    private final List<Layer> layers;

    /** Number of product types used in experiment. */
    private final int productTypesNo;

    public Model(List<Layer> layers, int productTypesNo) {
        this.layers = layers;
        this.productTypesNo = productTypesNo;
    }

    /** Method generates new order. */
    private int[] generateOrder() {
        Random random = new Random();
        int[] order = new int[this.productTypesNo];

        for(int i=0; i<this.productTypesNo; i++) {
            order[i] = random.nextInt(5);
        }

        return order;
    }

}
