package pl.edu.agh.flowshop.engine;

import agents.AbstractAgent;
import environment.ActionList;
import pl.edu.agh.flowshop.entity.Action;
import pl.edu.agh.flowshop.utils.Attributes;
import pl.edu.agh.flowshop.utils.Parameters;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;

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

    /** Classifier name */
    protected String classifierName = "";

    /** Train set used to teach {@link #classifier} */
    protected Instances trainSet;

    /** Classifier used for machine to learn */
    private Classifier classifier;

    public LearningAgent() {
        super(null, null);
        this.id = counter++;
    }

    public int getId() {
        return this.id;
    }

    @Override
    protected ActionList getActionList() {
        ActionList result = new ActionList(getCurrentState());
        for (int productNo = 0; productNo < Parameters.PRODUCT_TYPES_NO; productNo++) {
            result.add(new Action(id, productNo));
        }

        return result;
    }

    /** Fires learning process for this machine */
    protected void train() throws Exception {
        if (this.trainSet == null) {
            this.trainSet = new Instances("TrainSet", Attributes.attributes, 0);
            this.trainSet.setClassIndex(this.trainSet.numAttributes() - 1);
        }
        getClassifier().buildClassifier(this.trainSet);
    }

    /** Adds sample to {@link #trainSet} dataset of this agent and all underlying. */
    protected void addTrainData(final Instance instance) {
        if (this.trainSet == null) {
            this.trainSet = new Instances("TrainSet", Attributes.attributes, 0);
            this.trainSet.setClassIndex(this.trainSet.numAttributes() - 1);
        }

        this.trainSet.add(instance);
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
        Instances data = new Instances("Decide", Attributes.attributes, 0);
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
