package pl.edu.agh.flowshop;

import weka.classifiers.Classifier;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 * Maszyna przetwarzajaca zadania.
 *
 * @author Bartosz Sądel
 *         Created on 02.02.2016.
 */
public class Machine extends MachineConf {

    private final static FastVector attributes;

    /** Initialization of attributes for learning process */
    static {
        attributes = new FastVector(1);
        //TODO: Set attributes
    }

    /** Classifier used for machine to learn */
    private final Classifier classifier;
    /** Train set used to teach {@link #classifier} */
    private Instances trainSet;
    /** Type of processed product */
    private Integer productType;
    /** Turns left for product to be processed */
    private Integer turnsLeft;

    public Machine(final Classifier classifier, final MachineConf conf) {
        super(conf.machineId, conf.timeTable);
        this.classifier = classifier;
    }

    /**
     * Starts processing product on this machine.
     *
     * @param productType type of product to process
     */
    public void processProduct(final int productType) {
        this.productType = productType;
        this.turnsLeft = this.timeTable.get(productType);
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
     * @return class number
     * @throws Exception
     */
    public int classify() throws Exception {
        Instance instance = new SparseInstance(4);
        //TODO: set data into instance

        Instances data = new Instances("Test", attributes, 0);
        data.setClassIndex(data.numAttributes() - 1);
        instance.setDataset(data);

        return (int) this.classifier.classifyInstance(instance);
    }

    /**
     * Adds sample to {@link #trainSet} dataset.
     */
    public void addTrainData() {
        Instance instance = new SparseInstance(4);
        //TODO: set data into instance

        this.trainSet.add(instance);
    }
}
