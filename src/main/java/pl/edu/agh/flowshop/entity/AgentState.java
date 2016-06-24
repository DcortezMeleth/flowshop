package pl.edu.agh.flowshop.entity;

import environment.AbstractState;
import environment.IEnvironment;
import environment.IState;

import java.util.Arrays;

/**
 * Agent state.
 *
 * @author Bartosz Sądel
 *         Created on 09.04.2016.
 */
public class AgentState extends AbstractState {

    private int[] productsInBuffers;

    private boolean[] machinesHealth;

    public AgentState(final IEnvironment ct) {
        super(ct);
    }

    @Override
    public IState copy() {
        AgentState copy = new AgentState(getEnvironment());
        copy.machinesHealth = Arrays.copyOf(this.machinesHealth, this.machinesHealth.length);
        copy.productsInBuffers = Arrays.copyOf(this.productsInBuffers, this.productsInBuffers.length);
        return copy;
    }

    @Override
    public boolean equals(final Object obj) {
        if(!(obj instanceof AgentState)) {
            return false;
        }

        AgentState state = (AgentState) obj;
        return Arrays.equals(this.productsInBuffers, state.productsInBuffers) &&
                Arrays.equals(this.machinesHealth, state.machinesHealth);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.productsInBuffers) + Arrays.hashCode(this.machinesHealth);
    }

    @Override
    public int nnCodingSize() {
        return 0;
    }

    @Override
    public double[] nnCoding() {
        return new double[0];
    }

    public int[] getProductsInBuffers() {
        return productsInBuffers;
    }

    public boolean[] getMachinesHealth() {
        return machinesHealth;
    }

    public void setProductsInBuffers(final int[] productsInBuffers) {
        this.productsInBuffers = productsInBuffers;
    }

    public void setMachinesHealth(final boolean[] machinesHealth) {
        this.machinesHealth = machinesHealth;
    }
}
