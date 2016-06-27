package pl.edu.agh.flowshop.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.agh.flowshop.utils.Attributes;
import pl.edu.agh.flowshop.utils.Parameters;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Machine processing tasks
 *
 * @author Bartosz Sądel
 *         Created on 02.02.2016.
 */
public class Machine {

    private final static Logger logger = LogManager.getLogger(Machine.class);

    /** Agents counter */
    private static int counter = 0;

    /** Agent id */
    private final int id;

    /** Classifier name */
    protected String classifierName = "";

    /** Train set used to teach {@link #classifier} */
    protected Instances trainSet;

    /** Type of processed product */
    private int productType = -1;

    private boolean processing = false;

    /** Turns left for product to be processed */
    private int turnsLeft = 0;

    /** Indicates if machine is broken */
    private boolean broken = false;

    /**
     * Machine configuration in form of map: </br>
     * <li>
     * <ul>key - product type</ul>
     * <ul>value - processing time</ul>
     * </li>
     */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Map<Integer, Integer> timeTable = new HashMap<>();

    /** Classifier used for machine to learn */
    private Classifier classifier;

    public Machine() {
        this.id = counter++;
    }

    public int getId() {
        return this.id;
    }

    /** Assigns classifier based on its name from config */
    public void init() {
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

    public boolean isBroken() {
        return this.broken;
    }

    /** Returns product machine is going to finish in next turn. -1 if it won't finish anyting */
    public int getProductToBeProcessed() {
        if (this.turnsLeft <= 1 && this.productType > -1) {
            return this.productType;
        }
        return -1;
    }

    /** Fires learning process for this machine */
    protected void train() throws Exception {
        if (this.trainSet == null) {
            this.trainSet = new Instances("TrainSet", Attributes.attributes, 0);
            this.trainSet.setClassIndex(this.trainSet.numAttributes() - 1);
        }
        this.classifier.buildClassifier(this.trainSet);
    }

    /** Adds sample to {@link #trainSet} dataset of this agent and all underlying. */
    protected void addTrainData(final Instance instance) {
        if (this.trainSet == null) {
            this.trainSet = new Instances("TrainSet", Attributes.attributes, 0);
            this.trainSet.setClassIndex(this.trainSet.numAttributes() - 1);
        }

        this.trainSet.add(instance);
    }

    /** Return number of product which should be worked on */
    protected double getActionScore(final Instance instance) throws Exception {
        Instances data = new Instances("Decide", Attributes.attributes, 0);
        data.setClassIndex(data.numAttributes() - 1);
        instance.setDataset(data);

        double[] probabilities = this.classifier.distributionForInstance(instance);
        return probabilities[2];
    }

    /** Simulates one turn for agent */
    protected int tick(final int[] newTasks) throws Exception {
        logger.debug("Machine " + getId() + " tick!");
        if (shouldMachineBreak()) {
            logger.debug("Machine " + getId() + " broken!");
            //return processed product to queue
            if (this.productType != 0) {
                newTasks[this.productType] += 1;
            }
            this.productType = -1;
            this.turnsLeft = 1;
            this.processing = false;
            return -1;
        }

        //take task from queue
        if (this.productType > -1) {
            if (this.turnsLeft <= 0 && newTasks[this.productType] > 0) {
                logger.debug("Machine " + getId() + " takes task from queue!");
                newTasks[this.productType] -= 1;
                this.turnsLeft = this.timeTable.get(this.productType);
                this.processing = true;
            }

            this.turnsLeft--;

            //finished product is moved to finishedProduct field
            if (this.turnsLeft <= 0 && this.processing) {
                this.processing = false;
                logger.debug("Machine " + getId() + " finishes product " + this.productType);
                return this.productType;
            }
        }

        return -1;
    }

    /**
     * Classifies given example based on {@link #classifier} decision.
     *
     * @param instance instance to decide on
     * @param buffer   layer buffer
     * @throws Exception
     */
    protected void decideOnAction(final Instance instance, final int[] buffer) throws Exception {
        //changing production type while working is forbidden
        if (this.turnsLeft > 0 && this.processing) {
            logger.debug("Machine " + getId() + " still working!");
            return;
        }

        double[] probabilities = new double[Parameters.PRODUCT_TYPES_NO];
        for (int productType = 0; productType < Parameters.PRODUCT_TYPES_NO; productType++) {
            instance.setValue(Attributes.size() - 2, productType);
            probabilities[productType] = getActionScore(instance);
        }


        int actionToChoose = chooseActionFromProbabilities(probabilities);

        // zmienilismy typ -> czekamy ture
        if (actionToChoose != this.productType) {
            logger.debug("Machine " + getId() + " changed to " + actionToChoose);
            this.turnsLeft++;
        }

        this.productType = actionToChoose;


        boolean exists = buffer[actionToChoose] > 0;
        int value = exists ? Parameters.COSTS.get(actionToChoose) : 0;
        int switchCost = actionToChoose != this.productType ? 5 : 0;
        int reward = value - switchCost;
        instance.setValue(Attributes.size() - 2, actionToChoose);

        if (reward < -1) {
            instance.setValue(Attributes.size() - 1, Attributes.BAD);
        } else  if (reward > 1) {
            instance.setValue(Attributes.size() - 1, Attributes.GOOD);
        } else {
            instance.setValue(Attributes.size() - 1, Attributes.NORMAL);
        }

        addTrainData(instance);
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

    /** Checks if machine should break this turn */
    private boolean shouldMachineBreak() {
        if (this.broken || this.turnsLeft <= 0 || this.productType < 0) {
            return false;
        }

        //check if machine should break
        return (this.broken = new Random().nextInt(100) < 5);
    }

}
