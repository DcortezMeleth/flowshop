package pl.edu.agh.flowshop;

import pl.edu.agh.utils.Parameters;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

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
    protected final static FastVector attributes;

    /** Initialization of attributes for learning process */
    static {
        attributes = new FastVector(1);
        //TODO: Set attributes
    }

    /** learning level -> machine = -1, layer = -2, model=-3 */
    protected final int level;

    protected final List<? extends LearningAgent> agents;

    /** Classifier name */
    protected String classifierName = "";

    /** Train set used to teach {@link #classifier} */
    protected Instances trainSet; //TODO: initialize with attributes

    /** Classifier used for machine to learn */
    private Classifier classifier;

    public LearningAgent(final String classifierName, List<? extends LearningAgent> agents, final int level) {
        this.classifierName = classifierName;
        this.agents = agents;
        this.level = level;
    }

    /** Simulates one turn for agent */
    protected abstract int[] tick(final int turnNo, final int[] newTasks) throws Exception;

    protected List<? extends LearningAgent> getAgents() {
        return agents;
    }

    /**
     * Classifies given example based on {@link #classifier} decision.
     *
     * @param action choosen action, -1 if machine should choose itself
     * @throws Exception
     */
    protected void decideOnAction(int action) throws Exception {
        for (LearningAgent agent : this.agents) {
            agent.decideOnAction(getAction());
        }
    }

    /** Fires learning process for this machine */
    protected void train() throws Exception {
        getClassifier().buildClassifier(this.trainSet);
        for (LearningAgent agent : this.agents) {
            agent.train();
        }
    }

    /** Adds sample to {@link #trainSet} dataset. */
    protected void addTrainData() {
        Instance instance = new SparseInstance(4);
        //TODO: set data into instance

        this.trainSet.add(instance);

        for (LearningAgent agent : getAgents()) {
            agent.addTrainData();
        }
    }

    /** Returns classifier */
    protected Classifier getClassifier() {
        if (classifier == null) {
            assignClassifier();
        }

        return classifier;
    }

    /** Return number of product which should be worked on */
    protected int getAction() throws Exception {
        if (this.level != Parameters.LEARNING_LEVEL) {
            return this.level;
        }

        Instance instance = new SparseInstance(4);
        //TODO: set data into instance

        Instances data = new Instances("Test", attributes, 0);
        data.setClassIndex(data.numAttributes() - 1);
        instance.setDataset(data);

        double[] probabilities = getClassifier().distributionForInstance(instance);
        return chooseActionFromPropabilities(probabilities);
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

    /** Choses action based on their probabilities */
    private int chooseActionFromPropabilities(final double[] propabilities) {
        Random random = new Random();

        //eksploracja
        if(random.nextInt(100) < 5) {
            return random.nextInt(Parameters.PRODUCT_TYPES_NO);
        }

        int result = 0;
        double highest = propabilities[0];
        for(int i=1; i<Parameters.PRODUCT_TYPES_NO; i++) {
            if(propabilities[i] > highest) {
                result = i;
                highest = propabilities[i];
            }
        }
        return result;
    }
}
