package pl.edu.agh;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.edu.agh.flowshop.MachineConf;

import java.io.*;
import java.util.*;

/**
 * @author Bartosz
 *         Created on 2016-03-01.
 */
public class Experiment {

    public static void main(String[] args) {
        /*ResourceFileReader reader = new ResourceFileReader();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(reader.getResourcesFile("example.arff")));

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
        }*/

        Gson gson = new GsonBuilder().setPrettyPrinting().create();


        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 3);
        map.put(2, 5);
        map.put(3, 2);
        MachineConf m1 = new MachineConf(1, map, "NaiveBayes");
        MachineConf m2 = new MachineConf(2, map, "NaiveBayes");
        MachineConf m3 = new MachineConf(3, map, "NaiveBayes");
        MachineConf m4 = new MachineConf(4, map, "NaiveBayes");
        MachineConf m5 = new MachineConf(5, map, "NaiveBayes");
        MachineConf m6 = new MachineConf(6, map, "NaiveBayes");
        List<MachineConf> machines = new ArrayList<>();
        machines.add(m1);
        machines.add(m2);
        machines.add(m3);
        machines.add(m4);

        List<MachineConf> machines2 = new ArrayList<>();
        machines2.add(m5);
        machines2.add(m6);
        List<List<MachineConf>> prop = new ArrayList<>();
        prop.add(machines);
        prop.add(machines2);

        try (Writer writer = new FileWriter("machines.json")) {
            gson.toJson(prop, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*List<List<MachineConf>> properties = new ArrayList<>();
        try (Reader reader = new InputStreamReader(new ResourceFileReader().getResourcesFileStream("machines.json"))) {
            Type type = new TypeToken<List<List<MachineConf>>>() {}.getType();
            properties = gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(properties);*/
    }
}
