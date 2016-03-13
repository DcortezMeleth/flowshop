package pl.edu.agh.flowshop;

/**
 * Order class.
 *
 * @author Bartosz Sądel
 *         Created on 13.03.2016.
 */
public class Order {

    /** Product list */
    private final int[] productsList;

    /** Due time for completion */
    private final int dueTime;

    /** Reward for completion */
    private final int reward;

    /** Penalty for not completion on time */
    private final int penalty;

    public Order(final int[] productsList, final int dueTime, final int reward, final int penalty) {
        this.productsList = productsList;
        this.dueTime = dueTime;
        this.reward = reward;
        this.penalty = penalty;
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
}
