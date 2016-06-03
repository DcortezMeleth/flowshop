package pl.edu.agh.flowshop.engine;

import agents.AbstractAgent;
import algorithms.IStrategy;
import algorithms.PengSelector;
import algorithms.QLearningSelector;
import algorithms.WatkinsSelector;
import environment.ActionList;
import environment.IState;
import pl.edu.agh.flowshop.entity.Action;
import pl.edu.agh.flowshop.entity.AgentState;
import pl.edu.agh.flowshop.utils.Attributes;
import pl.edu.agh.flowshop.utils.Parameters;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
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

    /** Classifier name */
    protected String classifierName = "";

    /** Train set used to teach {@link #classifier} */
    protected Instances trainSet;

    /** Classifier used for machine to learn */
    private Classifier classifier;

    /** Is classifier already initialized */
    private boolean initialized;

    /** True if using supervised learning, false if using reinforcement learning */
    private boolean supervised;

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
        if(supervised) {
            if (this.trainSet == null) {
                this.trainSet = new Instances("TrainSet", Attributes.attributes, 0);
                this.trainSet.setClassIndex(this.trainSet.numAttributes() - 1);
            }
            this.classifier.buildClassifier(this.trainSet);
        }
    }

    /** Adds sample to {@link #trainSet} dataset of this agent and all underlying. */
    protected void addTrainData(final Instance instance) {
        if(supervised) {
            if (this.trainSet == null) {
                this.trainSet = new Instances("TrainSet", Attributes.attributes, 0);
                this.trainSet.setClassIndex(this.trainSet.numAttributes() - 1);
            }

            this.trainSet.add(instance);
        }
    }

    /** Return number of product which should be worked on */
    protected int getAction(final Instance instance) throws Exception {
        Instances data = new Instances("Decide", Attributes.attributes, 0);
        data.setClassIndex(data.numAttributes() - 1);
        instance.setDataset(data);

        //if classifier != null we use weka for decisions
        if(supervised) {
            double[] probabilities = this.classifier.distributionForInstance(instance);
            return chooseActionFromProbabilities(probabilities);
        } else {
            Action action = (Action) act();
            return action.getProductToProcess();
        }
    }

    /** Assigns classifier based on its name from config */
    public void init(final Model model) {
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
            case "QLearning":
                this.setAlgorithm(new QLearningSelector());
                break;
            case "Watkins":
                this.setAlgorithm(new WatkinsSelector(Parameters.LAMBDA));
                break;
            case "Peng":
                this.setAlgorithm(new PengSelector(Parameters.LAMBDA));
                break;
            default:
                this.classifier = new J48();
                break;
        }

        // initialization of reinforcement learning params
        if(getAlgorithm() != null) {
            QLearningSelector strategy = (QLearningSelector) getAlgorithm();
            strategy.setEpsilon(Parameters.EPSILON);
            strategy.setGamma(Parameters.GAMMA);
            this.supervised = false;

            setUniverse(model);
            setCurrentState(getInitState(model));
        }

        this.initialized = true;
    }

    private IState getInitState(final Model model) {
        AgentState state = new AgentState(model);
        List<Integer> attrs = new ArrayList<>();
        for(int i=0; i<Attributes.size(); i++) {
            attrs.add(0);
        }
        state.setAttrValues(attrs);
        return state;
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
