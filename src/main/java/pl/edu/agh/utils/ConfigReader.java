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

/**
 * Class responsible for reading an experiment configuration from file.
 *
 * @author Bartosz
 *         Created on 2016-03-09.
 */
public class ConfigReader {

    private ConfigReader() {
    }

    /** Creates model based on config files */
    public static Model createModel() {

        List<List<MachineConf>> machinesConf = getMachinesConfig();

        List<Layer> layers = new ArrayList<>();
        for (List<MachineConf> configurations : machinesConf) {
            List<Machine> machines = new ArrayList<>();
            for (MachineConf machineConf : configurations) {
                machines.add(new Machine(machineConf));
            }
            layers.add(new Layer(machines));
        }

        return new Model(layers);
    }

    /** Reads machines configuration from file */
    private static List<List<MachineConf>> getMachinesConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Reader reader = new InputStreamReader(new ResourceFileReader().getResourcesFileStream("machines.json"))) {
            Type type = new TypeToken<List<List<MachineConf>>>() {

            }.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
