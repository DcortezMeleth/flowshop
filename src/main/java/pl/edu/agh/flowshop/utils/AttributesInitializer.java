package pl.edu.agh.flowshop.utils;

import pl.edu.agh.flowshop.engine.LearningAgent;
import pl.edu.agh.flowshop.engine.Model;
import weka.core.Attribute;
import weka.core.FastVector;

/**
 * @author Bartosz
 *         Created on 2016-03-22.
 */
public abstract class AttributesInitializer {

    /** Attribute holding buffer content prefix */
    public static final String BUFFER_PREFIX = "_buffer_";

    /** Attribute holding health info prefix */
    public static final String HEALTH_PREFIX = "_health_";

    public static void initAttributes(final Model model) {
        //count number of attributes for learning
        int attrNo = Parameters.PRODUCT_TYPES_NO * model.getAgents().size();
        for (LearningAgent layer : model.getAgents()) {
            attrNo += layer.getAgents().size();
        }
        attrNo *= Parameters.USED_HISTORY;
        attrNo++;

        // attributes initialization, for now same for all learning layers
        FastVector attributes = new FastVector(attrNo);
        for (int i = 0; i < Parameters.USED_HISTORY; i++) {
            for (LearningAgent layer : model.getAgents()) {
                layer.setAttributes(attributes);
                for (LearningAgent machine : layer.getAgents()) {
                    machine.setAttributes(attributes);
                    Attribute health = new Attribute(i + " " + HEALTH_PREFIX + machine.getId());
                    attributes.addElement(health);
                }
                for (int j = 1; j <= Parameters.PRODUCT_TYPES_NO; j++) {
                    Attribute buffer = new Attribute(i + " " + BUFFER_PREFIX + layer.getId() + "_" + j);
                    attributes.addElement(buffer);
                }
            }
        }

        FastVector result = new FastVector(2);
        result.addElement("GOOD");
        result.addElement("BAD");
        attributes.addElement(new Attribute("result", result));

        model.setAttributes(attributes);
    }

}
