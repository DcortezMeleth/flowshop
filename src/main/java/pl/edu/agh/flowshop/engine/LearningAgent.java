package pl.edu.agh.flowshop.engine;

import agents.AbstractAgent;
import environment.ActionList;
import environment.IEnvironment;
import environment.IState;
import pl.edu.agh.flowshop.entity.Action;
import pl.edu.agh.flowshop.utils.Parameters;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Abstract class containing everything what learning agent will need.
 *
 * @author Bartosz
 *         Created on 2016-03-17.
 */
public abstract class LearningAgent extends AbstractAgent {

    /** Agents counter */
    private static int counter = 0;

    /** Agent id */
    private final int id;

    /** Learning level -> machine = -1, layer = -2, model=-3 */
    protected int level = Parameters.MACHINE;

    /** Lower agents layer */
    protected List<? extends LearningAgent> agents;

    /** Classifier name */
    protected String classifierName = "";

    /** Train set used to teach {@link #classifier} */
    protected Instances trainSet;

    /** Vector of {@link weka.core.Attribute Attributes} used for learning */
    private FastVector attributes;

    /** Classifier used for machine to learn */
    private Classifier classifier;

    /** Variable for holding id of agent which should act */
    private int choosenMachineId;

    public LearningAgent(List<? extends LearningAgent> agents, final int level, final String classifierName) {
        super(null, null);
        this.classifierName = classifierName;
        this.agents = agents;
        this.level = level;
        this.id = counter++;
    }

    public int getId() {
        return this.id;
    }

    public List<? extends LearningAgent> getAgents() {
        if (this.agents == null) {
            this.agents = new ArrayList<>();
        }
        return this.agents;
    }

    public FastVector getAttributes() {
        return attributes;
    }

    public void setAttributes(final FastVector attributes) {
        this.attributes = attributes;
    }

    @Override
    public void setCurrentState(final IState currentState) {
        super.setCurrentState(currentState);
        for (LearningAgent agent : getAgents()) {
            agent.setCurrentState(currentState);
        }
    }

    @Override
    public void setUniverse(final IEnvironment universe) {
        super.setUniverse(universe);
        for (LearningAgent agent : getAgents()) {
            agent.setUniverse(universe);
        }
    }

    @Override
    public void setOldState(final IState oldState) {
        super.setOldState(oldState);
        for (LearningAgent agent : getAgents()) {
            agent.setOldState(oldState);
        }
    }

    @Override
    protected ActionList getActionList() {
        ActionList result = new ActionList(getCurrentState());
        for (int productNo = 0; productNo < Parameters.PRODUCT_TYPES_NO; productNo++) {
            result.add(new Action(choosenMachineId, productNo));
        }

        return result;
    }

    public void setLevel(final int level) {
        this.level = level;
    }

    /** Simulates one turn for agent */
    protected abstract int[] tick(final int turnNo, final int[] newTasks) throws Exception;

    /** Fires learning process for this machine */
    protected void train() throws Exception {
        if (this.trainSet == null) {
            this.trainSet = new Instances("TrainSet", getAttributes(), 0);
            this.trainSet.setClassIndex(this.trainSet.numAttributes() - 1);
        }
        getClassifier().buildClassifier(this.trainSet);
        for (LearningAgent agent : this.agents) {
            agent.train();
        }
    }

    /** Adds sample to {@link #trainSet} dataset of this agent and all underlying. */
    protected void addTrainData(final Instance instance) {
        if (this.trainSet == null) {
            this.trainSet = new Instances("TrainSet", getAttributes(), 0);
            this.trainSet.setClassIndex(this.trainSet.numAttributes() - 1);
        }

        this.trainSet.add(instance);

        for (LearningAgent agent : getAgents()) {
            agent.addTrainData(instance);
        }
    }

    /** Returns classifier */
    protected Classifier getClassifier() {
        if (this.classifier == null) {
            assignClassifier();
        }

        return this.classifier;
    }

    /**
     * Classifies given example based on {@link #classifier} decision.
     *
     * @param action   chosen action, -1 if machine should choose itself
     * @param instance instance to decide on
     * @throws Exception
     */
    protected void decideOnAction(final int action, final Instance instance) throws Exception {
        for (LearningAgent agent : this.agents) {
            agent.decideOnAction(getAction(instance), instance);
        }
    }

    /** Return number of product which should be worked on */
    protected int getAction(final Instance instance) throws Exception {
        if (this.level != Parameters.LEARNING_LEVEL) {
            return this.level;
        }

        Instances data = new Instances("Decide", getAttributes(), 0);
        data.setClassIndex(data.numAttributes() - 1);
        instance.setDataset(data);

        //if classifier != null we use weka for decisions
        if (getClassifier() != null) {
            double[] probabilities = getClassifier().distributionForInstance(instance);
            return chooseActionFromProbabilities(probabilities);
        } else {

            Action action = (Action) act();
            return action.getProductToProcess();
        }
    }

    /** Adds products from list2 to list1 */
    protected void addProducts(final int[] list1, final int[] list2) {
        for (int i = 0; i < Parameters.PRODUCT_TYPES_NO; i++) {
            list1[i] += list2[i];
        }
    }

    /** Assigns classifier based on its name from config */
    private void assignClassifier() {
        switch (this.classifierName) {
            case "J48":
                this.classifier = new J48();
                break;
            case "JRip":
                this.classifier = new JRip();
                break;
            case "BayesNet":
                this.classifier = new BayesNet();
                break;
            case "NaiveBayes":
                this.classifier = new NaiveBayes();
                break;
            default:
                this.classifier = new J48();
                break;
        }
    }

    /** Chooses action based on their probabilities */
    private int chooseActionFromProbabilities(final double[] probabilities) {
        Random random = new Random();

        // exploration
        if (random.nextInt(100) < 5 || probabilities.length != Parameters.PRODUCT_TYPES_NO) {
            return random.nextInt(Parameters.PRODUCT_TYPES_NO);
        }

        int result = 0;
        double highest = probabilities[0];
        for (int i = 1; i < Parameters.PRODUCT_TYPES_NO; i++) {
            if (probabilities[i] > highest) {
                result = i;
                highest = probabilities[i];
            }
        }
        return result;
    }
}
