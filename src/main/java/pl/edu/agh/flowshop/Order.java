package pl.edu.agh.flowshop;

import pl.edu.agh.utils.Parameters;

/**
 * Order class.
 *
 * @author Bartosz Sądel
 *         Created on 13.03.2016.
 */
public class Order {

    /** Product list */
    private final int[] productsList;

    /** Reward for completion */
    private final int reward;

    /** Penalty for not completion on time */
    private final int penalty;

    /** Order priority */
    private final int priority;

    /** Due time for completion */
    private int dueTime;

    public Order(final int[] productsList, final int dueTime, final int reward, final int penalty, final int priority) {
        this.productsList = productsList;
        this.dueTime = dueTime;
        this.reward = reward;
        this.penalty = penalty;
        this.priority = priority;
    }

    /** Counts and returns value of order */
    public int getValue() {
        int result = 0;
        for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
            result += productsList[i] * Parameters.COSTS.get(i + 1);
        }
        return result;
    }

    public void decreaseDueTime() {
        this.dueTime--;
    }

    public int[] getProductsList() {
        return productsList;
    }

    public int getDueTime() {
        return dueTime;
    }

    public int getReward() {
        return reward;
    }

    public int getPenalty() {
        return penalty;
    }

    public int getPriority() {
        return priority;
    }
}
