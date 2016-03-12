package pl.edu.agh.flowshop;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 * Machine processing tasks
 *
 * @author Bartosz Sądel
 *         Created on 02.02.2016.
 */
public class Machine extends MachineConf {

    /** Vector of {@link weka.core.Attribute Attributes} used for learning */
    private final static FastVector attributes;

    /** Initialization of attributes for learning process */
    static {
        attributes = new FastVector(1);
        //TODO: Set attributes
    }

    /** Classifier used for machine to learn */
    private Classifier classifier;

    /** Train set used to teach {@link #classifier} */
    private Instances trainSet;

    /** Type of processed product */
    private Integer productType;

    /** Turns left for product to be processed */
    private Integer turnsLeft;

    /** Constructor using configuration object. */
    public Machine(final MachineConf conf) {
        super(conf.machineId, conf.timeTable);
        assignClassifier(conf.classifierName);

        this.trainSet = new Instances("TrainSet", attributes, 0);
    }

    /** Assigns classifier based on its name from config */
    private void assignClassifier(final String classifierName) {
        switch (classifierName) {
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

    /**
     * Starts processing product on this machine.
     *
     * @param productType type of product to process
     */
    public boolean processProduct(final int productType) {
        if (this.turnsLeft != 0 || this.productType != productType) {
            return false;
        }
        this.turnsLeft = this.timeTable.get(productType);
        return true;
    }

    /** Decrements a counter of {@link #turnsLeft}. Used on begining of a turn. */
    public void tick() {
        this.turnsLeft--;
    }

    /** Returns type of processed product. */
    public Integer getProcessed() {
        return turnsLeft == 0 ? productType : null;
    }

    /** Fires learning process for this machine */
    public void train() throws Exception {
        this.classifier.buildClassifier(this.trainSet);
    }

    /**
     * Classifies given example based on {@link #classifier} decision.
     *
     * @throws Exception
     */
    public void decideOnAction() throws Exception {
        //zmienic typ mozemy tylko gdy aktualnie czegos nie przetwarzamy
        if (this.turnsLeft != 0) {
            return;
        }
        Instance instance = new SparseInstance(4);
        //TODO: set data into instance

        Instances data = new Instances("Test", attributes, 0);
        data.setClassIndex(data.numAttributes() - 1);
        instance.setDataset(data);

        double[] probabilities = this.classifier.distributionForInstance(instance);
        int result = chooseAction(probabilities);
        if (result != this.productType) {
            this.turnsLeft++;
        }
        this.productType = result;
    }

    /**
     * Adds sample to {@link #trainSet} dataset.
     */
    public void addTrainData() {
        Instance instance = new SparseInstance(4);
        //TODO: set data into instance

        this.trainSet.add(instance);
    }

    /** Choses action based on their probabilities */
    private int chooseAction(final double[] propabilities) {
        int result = 0;
        //TODO strategia wyboru?

        return 0;
    }
}
