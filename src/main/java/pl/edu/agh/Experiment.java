package pl.edu.agh;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.edu.agh.utils.ResourceFileReader;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.*;

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

            FastVector v1 = new FastVector(3);
            v1.addElement("sunny");
            v1.addElement("overcast");
            v1.addElement("rainy");
            Attribute outlook = new Attribute("outlook", v1);
            Attribute temperature = new Attribute("temperature");
            Attribute humidity = new Attribute("humidity");
            FastVector v2 = new FastVector(2);
            v2.addElement("TRUE");
            v2.addElement("FALSE");
            Attribute windy = new Attribute("windy", v2);
            FastVector v3 = new FastVector(2);
            v3.addElement("yes");
            v3.addElement("no");
            Attribute play = new Attribute("play", v3);
            FastVector attributes = new FastVector(5);
            attributes.addElement(outlook);
            attributes.addElement(temperature);
            attributes.addElement(humidity);
            attributes.addElement(windy);
            attributes.addElement(play);

            Instance instance = new SparseInstance(4);
            instance.setValue(outlook, "sunny");
            instance.setValue(temperature, 80);
            instance.setValue(humidity, 70);
            instance.setValue(windy, "TRUE");

            Instances data = new Instances("Test", attributes, 0);
            data.setClassIndex(data.numAttributes() - 1);
            //data.add(instance);

            instance.setDataset(data);

            double[] result = naiveBayes.distributionForInstance(instance);
            double val = naiveBayes.classifyInstance(instance);

            System.out.println(result[0] + " " + result[1]);
            System.out.println(data.classAttribute().value((int) val));


            //Evaluation evaluation = new Evaluation(train);
            //evaluation.crossValidateModel(naiveBayes, train, 10, new Debug.Random(1));
            //System.out.println(evaluation.toSummaryString("\nResults\n=========\n", true));
            //System.out.println(evaluation.fMeasure(1) + " " + evaluation.precision(1) + " " + evaluation.recall(1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
    }
}
