package pl.edu.agh.flowshop.utils;

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

    /** Threshold for decision between good and bad labels */
    public static final int DECISION_THRESHOLD;

    /** Lenght of history entries used for learning */
    public static final int USED_HISTORY;

    /** Parameter lambda used for Watkins and Peng algorithms */
    public static final double LAMBDA;

    /** Parameter epsilon used for Watkins and Peng algorithms */
    public static final double EPSILON;

    /** Parameter gamma used for Watkins and Peng algorithms */
    public static final double GAMMA;

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

    /** Reward for completion od order */
    private static final String REWARD_KEY = "REWARD";

    /** Penalty for not completing order in time */
    private static final String PENALTY_KEY = "PENALTY";

    /** Threshold for decision between good and bad labels */
    private static final String DECISION_THRESHOLD_KEY = "DECISION_THRESHOLD";

    /** Lenght of history entries used for learning */
    private static final String USED_HISTORY_KEY = "USED_HISTORY";

    /** Parameter lambda used for Watkins and Peng algorithms */
    private static final String LAMBDA_KEY = "LAMBDA";

    /** Parameter epsilon used for Watkins and Peng algorithms */
    private static final String EPSILON_KEY = "EPSILON";

    /** Parameter gamma used for Watkins and Peng algorithms */
    private static final String GAMMA_KEY = "GAMMA";

    /** File with experiment configuration */
    private static final String PROPERTIES_FILE_KEY = "flowshop.properties";

    static {
        Properties configuration = new Properties();
        getConfig(configuration);

        LEARNING_TURN = Integer.parseInt(configuration.getProperty(LEARNING_TURN_KEY));
        TURN_LIMIT = Integer.parseInt(configuration.getProperty(TURN_LIMIT_KEY));
        QUEUE_SIZE = Integer.parseInt(configuration.getProperty(QUEUE_SIZE_KEY));
        PRODUCT_TYPES_NO = Integer.parseInt(configuration.getProperty(PRODUCT_TYPES_NO_KEY));
        REWARD = Integer.parseInt(configuration.getProperty(REWARD_KEY));
        DECISION_THRESHOLD = Integer.parseInt(configuration.getProperty(DECISION_THRESHOLD_KEY));
        PENALTY = Double.parseDouble(configuration.getProperty(PENALTY_KEY));
        USED_HISTORY = Integer.parseInt(configuration.getProperty(USED_HISTORY_KEY));
        LAMBDA = configuration.getProperty(LAMBDA_KEY) != null ?
                Double.parseDouble(configuration.getProperty(LAMBDA_KEY)) : 0.5;
        EPSILON = configuration.getProperty(LAMBDA_KEY) != null ?
                Double.parseDouble(configuration.getProperty(LAMBDA_KEY)) : 0.9;
        GAMMA = configuration.getProperty(LAMBDA_KEY) != null ?
                Double.parseDouble(configuration.getProperty(LAMBDA_KEY)) : 0.9;

        COSTS = new HashMap<>();
        for (int i = 0; i < PRODUCT_TYPES_NO; i++) {
            COSTS.put(i, Integer.valueOf(configuration.getProperty(PRODUCT_PRICE_KEY + i)));
        }
    }

    private Parameters() {
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
