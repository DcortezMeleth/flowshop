package pl.edu.agh.flowshop.utils;

import pl.edu.agh.flowshop.engine.Layer;
import pl.edu.agh.flowshop.engine.Machine;
import pl.edu.agh.flowshop.engine.Model;
import weka.core.Attribute;
import weka.core.FastVector;

/**
 * @author Bartosz
 *         Created on 2016-03-22.
 */
public abstract class Attributes {

    /** Attribute holding buffer content prefix */
    public static final String BUFFER_PREFIX = "_buffer_";

    /** Attribute holding health info prefix */
    public static final String HEALTH_PREFIX = "_health_";

    public static FastVector attributes;

    public static int size() {
        return attributes.size();
    }

    public static void initAttributes(final Model model) {
        //count number of attributes for learning
        int attrNo = Parameters.PRODUCT_TYPES_NO * model.getLayers().size();
        for (Layer layer : model.getLayers()) {
            attrNo += layer.getMachines().size();
        }
        attrNo *= Parameters.USED_HISTORY;
        attrNo++;

        // attributes initialization, for now same for all learning layers
        FastVector attr_vec = new FastVector(attrNo);
        for (int i = 0; i < Parameters.USED_HISTORY; i++) {
            for (Layer layer : model.getLayers()) {
                for (Machine machine : layer.getMachines()) {
                    Attribute health = new Attribute(i + " " + HEALTH_PREFIX + machine.getId());
                    attr_vec.addElement(health);
                }
                for (int j = 1; j <= Parameters.PRODUCT_TYPES_NO; j++) {
                    Attribute buffer = new Attribute(i + " " + BUFFER_PREFIX + layer.getId() + "_" + j);
                    attr_vec.addElement(buffer);
                }
            }
        }

        FastVector result = new FastVector(2);
        result.addElement("GOOD");
        result.addElement("BAD");
        attr_vec.addElement(new Attribute("result", result));

        attributes = attr_vec;
    }

}
