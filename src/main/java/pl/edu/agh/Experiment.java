package pl.edu.agh;

import pl.edu.agh.utils.ResourceFileReader;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Debug;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author Bartosz
 *         Created on 2016-03-01.
 */
public class Experiment {

    public static void main(String[] args) {
        ResourceFileReader reader = new ResourceFileReader();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(reader.getFile("example.arff")));

            Instances train = new Instances(bufferedReader);
            train.setClassIndex(train.numAttributes() - 1);

            bufferedReader.close();

            NaiveBayes naiveBayes = new NaiveBayes();
            naiveBayes.buildClassifier(train);

            Evaluation evaluation = new Evaluation(train);
            evaluation.crossValidateModel(naiveBayes, train, 10, new Debug.Random(1));
            System.out.println(evaluation.toSummaryString("\nResults\n=========\n", true));
            System.out.println(evaluation.fMeasure(1) + " " + evaluation.precision(1) + " " + evaluation.recall(1));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
