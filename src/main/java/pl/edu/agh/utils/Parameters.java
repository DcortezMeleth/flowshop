package pl.edu.agh.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Experiment parameters container
 *
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

    /** Reward for completion od order */
    public static final int REWARD;

    /** Penalty for not completing order in time */
    public static final double PENALTY;

    /** Product types no. value in properties file. */
    public static final int PRODUCT_TYPES_NO;

    /**
     * Key for learning layer in properties file.
     * Value is equal {@link #MACHINE}, {@link #LAYER} or {@link #MODEL}.
     */
    public static final int LEARNING_LEVEL;

    /** Layer level of learning. */
    public static final int MACHINE = -1;

    /** Layer level of learning. */
    public static final int LAYER = -2;

    /** Layer level of learning. */
    public static final int MODEL = -3;

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

    /** Key for learning layer in properties file. */
    private static final String LEARNING_LEVEL_KEY = "LEARNING_LEVEL";

    /** File with experiment configuration */
    private static final String PROPERTIES_FILE_KEY = "flowshop.properties";

    /** Reward for completion od order */
    private static final String REWARD_KEY = "REWARD";

    /** Penalty for not completing order in time */
    private static final String PENALTY_KEY = "PENALTY";

    static {
        Properties configuration = new Properties();
        getConfig(configuration);

        LEARNING_TURN = Integer.parseInt(configuration.getProperty(LEARNING_TURN_KEY));
        TURN_LIMIT = Integer.parseInt(configuration.getProperty(TURN_LIMIT_KEY));
        QUEUE_SIZE = Integer.parseInt(configuration.getProperty(QUEUE_SIZE_KEY));
        PRODUCT_TYPES_NO = Integer.parseInt(configuration.getProperty(PRODUCT_TYPES_NO_KEY));
        LEARNING_LEVEL = Integer.parseInt(configuration.getProperty(LEARNING_LEVEL_KEY));
        REWARD = Integer.parseInt(configuration.getProperty(REWARD_KEY));
        PENALTY = Double.parseDouble(configuration.getProperty(PENALTY_KEY));

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
