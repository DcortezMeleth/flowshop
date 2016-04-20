package pl.edu.agh.flowshop.entity;

import environment.IAction;

import java.util.Objects;

/**
 * Action in our model.
 *
 * @author Bartosz Sądel
 *         Created on 09.04.2016.
 */
public class Action implements IAction {

    /** Number of agent in learning layer */
    private final int agentNo;

    /** Product type to process */
    private final int productToProcess;

    public Action(final int agentNo, final int productToProcess) {
        this.agentNo = agentNo;
        this.productToProcess = productToProcess;
    }

    public int getProductToProcess() {
        return productToProcess;
    }

    public int getAgentNo() {
        return agentNo;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Action && this.productToProcess == ((Action) obj).productToProcess;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.productToProcess);
    }

    @Override
    public Object copy() {
        return new Action(agentNo, this.productToProcess);
    }

    @Override
    public int nnCodingSize() {
        return 0;
    }

    @Override
    public double[] nnCoding() {
        return new double[0];
    }
}
