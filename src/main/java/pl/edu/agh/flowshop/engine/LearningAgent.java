package pl.edu.agh.flowshop.engine;

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
public abstract class LearningAgent {

    /** Vector of {@link weka.core.Attribute Attributes} used for learning */
    private FastVector attributes;

    /** Agents counter */
    private static int counter = 0;

    /** learning level -> machine = -1, layer = -2, model=-3 */
    protected final int level;

    /** Agent id */
    private final int id;

    /** Lower agents layer */
    protected List<? extends LearningAgent> agents;

    /** Classifier name */
    protected String classifierName = "";

    /** Train set used to teach {@link #classifier} */
    protected Instances trainSet;

    /** Classifier used for machine to learn */
    private Classifier classifier;

    public LearningAgent(final String classifierName, List<? extends LearningAgent> agents, final int level) {
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

    /** Simulates one turn for agent */
    protected abstract int[] tick(final int turnNo, final int[] newTasks) throws Exception;

    public FastVector getAttributes() {
        return attributes;
    }

    public void setAttributes(final FastVector attributes) {
        this.attributes = attributes;
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

    /** Fires learning process for this machine */
    protected void train() throws Exception {
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

    /** Return number of product which should be worked on */
    protected int getAction(final Instance instance) throws Exception {
        if (this.level != Parameters.LEARNING_LEVEL) {
            return this.level;
        }

        Instances data = new Instances("Decide", getAttributes(), 0);
        data.setClassIndex(data.numAttributes() - 1);
        instance.setDataset(data);

        double[] probabilities = getClassifier().distributionForInstance(instance);
        return chooseActionFromProbabilities(probabilities);
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
            default:
                this.classifier = new NaiveBayes();
                break;
        }
    }

    /** Chooses action based on their probabilities */
    private int chooseActionFromProbabilities(final double[] probabilities) {
        Random random = new Random();

        // exploration
        if (random.nextInt(100) < 5) {
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
