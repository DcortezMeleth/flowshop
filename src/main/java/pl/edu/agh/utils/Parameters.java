package pl.edu.agh.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Bartosz
 *         Created on 2016-03-15.
 */
public abstract class Parameters {

    /** Learning turn value in properties file. */
    public static final int LEARNING_TURN;

    /** Turn limit value in properties file. */
    public static final int TURN_LIMIT;

    /** Queue size value in properties file. */
    public static final int QUEUE_SIZE;

    /** Product types no. value in properties file. */
    public static final int PRODUCT_TYPES_NO;

    /** Products unit costs */
    public static final Map<Integer, Integer> COSTS;

    /** Key for learning turn value in properties file. */
    private static final String LEARNING_TURN_KEY = "LEARNING_TURN";

    /** Key for turn limit value in properties file. */
    private static final String TURN_LIMIT_KEY = "TURN_LIMIT";

    /** Key for queue size value in properties file. */
    private static final String QUEUE_SIZE_KEY = "QUEUE_SIZE";

    /** Key for product types no. value in properties file. */
    private static final String PRODUCT_TYPES_NO_KEY = "PRODUCT_TYPES";

    /** Key for product types no. value in properties file. */
    private static final String PRODUCT_PRICE_KEY = "PRODUCT_PRICE_";

    /** File with experiment configuration */
    private static final String PROPERTIES_FILE_KEY = "flowshop.properties";

    static {
        Properties configuration = new Properties();
        getConfig(configuration);

        LEARNING_TURN = Integer.parseInt(configuration.getProperty(LEARNING_TURN_KEY));
        TURN_LIMIT = Integer.parseInt(configuration.getProperty(TURN_LIMIT_KEY));
        QUEUE_SIZE = Integer.parseInt(configuration.getProperty(QUEUE_SIZE_KEY));
        PRODUCT_TYPES_NO = Integer.parseInt(configuration.getProperty(PRODUCT_TYPES_NO_KEY));

        COSTS = new HashMap<>();
        for (int i = 1; i <= PRODUCT_TYPES_NO; i++) {
            COSTS.put(i, Integer.valueOf(configuration.getProperty(PRODUCT_PRICE_KEY + i)));
        }
    }

    /** Reads configuration from file */
    private static void getConfig(final Properties properties) {
        try {
            properties.load(new ResourceFileReader().getResourcesFileStream(PROPERTIES_FILE_KEY));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}