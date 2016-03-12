package pl.edu.agh.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import pl.edu.agh.flowshop.Layer;
import pl.edu.agh.flowshop.Machine;
import pl.edu.agh.flowshop.MachineConf;
import pl.edu.agh.flowshop.Model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Class responsible for reading an experiment configuration from file.
 *
 * @author Bartosz
 *         Created on 2016-03-09.
 */
public class ConfigReader {

    /** Key for learning turn value in properties file. */
    private static final String LEARNING_TURN_KEY = "LEARNING_TURN";

    /** Key for turn limit value in properties file. */
    private static final String TURN_LIMIT_KEY = "TURN_LIMIT";

    /** Key for queue size value in properties file. */
    private static final String QUEUE_SIZE_KEY = "QUEUE_SIZE";

    /** Key for product types no. value in properties file. */
    private static final String PRODUCT_TYPES_NO = "PRODUCT_TYPES";

    /** File with experiment configuration */
    private static final String PROPERTIES_FILE = "flowshop.properties";

    private ConfigReader() {}

    /** Creates model based on config files */
    public static Model createModel() {
        Properties configuration = new Properties();
        getConfig(configuration);

        List<List<MachineConf>> machinesConf = getMachinesConfig();

        int learningTurn = Integer.parseInt(configuration.getProperty(LEARNING_TURN_KEY));
        int turnLimit = Integer.parseInt(configuration.getProperty(TURN_LIMIT_KEY));
        int queueSize = Integer.parseInt(configuration.getProperty(QUEUE_SIZE_KEY));
        int productTypesNo = Integer.parseInt(configuration.getProperty(PRODUCT_TYPES_NO));

        List<Layer> layers = new ArrayList<>();
        for(List<MachineConf> configurations : machinesConf) {
            List<Machine> machines = new ArrayList<>();
            for(MachineConf machineConf : configurations) {
                machines.add(new Machine(machineConf));
            }
            layers.add(new Layer(machines, productTypesNo, learningTurn));
        }

        return new Model(layers, productTypesNo, turnLimit, queueSize);
    }

    /** Reads machines configuration from file */
    private static List<List<MachineConf>> getMachinesConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Reader reader = new InputStreamReader(new ResourceFileReader().getResourcesFileStream("machines.json"))) {
            Type type = new TypeToken<List<List<MachineConf>>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /** Reads configuration from file */
    private static void getConfig(final Properties properties) {
        try {
            properties.load(new ResourceFileReader().getResourcesFileStream(PROPERTIES_FILE));
            System.out.println(properties.getProperty(ConfigReader.LEARNING_TURN_KEY));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
