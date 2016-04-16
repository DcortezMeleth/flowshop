package pl.edu.agh.flowshop.entity;

import environment.AbstractState;
import environment.IEnvironment;
import environment.IState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Agent state.
 *
 * @author Bartosz Sądel
 *         Created on 09.04.2016.
 */
public class AgentState extends AbstractState {

    private List<Integer> attrValues;

    public AgentState(final IEnvironment ct) {
        super(ct);
    }

    public List<Integer> getAttrValues() {
        return attrValues;
    }

    public void setAttrValues(final List<Integer> attrValues) {
        this.attrValues = attrValues;
    }

    @Override
    public IState copy() {
        AgentState copy = new AgentState(getEnvironment());
        copy.attrValues = new ArrayList<>(this.attrValues);
        return copy;
    }

    @Override
    public boolean equals(final Object obj) {
        if(!(obj instanceof AgentState)) {
            return false;
        }

        for(int i=0; i<attrValues.size(); i++) {
            Integer val1 = this.attrValues.get(i);
            Integer val2 = ((AgentState) obj).getAttrValues().get(i);
            if(!val1.equals(val2)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(attrValues);
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
