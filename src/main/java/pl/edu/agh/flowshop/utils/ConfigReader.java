package pl.edu.agh.flowshop.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import pl.edu.agh.flowshop.engine.Layer;
import pl.edu.agh.flowshop.engine.Machine;
import pl.edu.agh.flowshop.engine.Model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        List<List<Machine>> machinesConf = getMachinesConfig();
        List<Layer> layers = new ArrayList<>();
        Model model = new Model(layers);

        layers.addAll(machinesConf.stream().map(Layer::new).collect(Collectors.toList()));

        for (Layer layer : layers) {
            layer.setModel(model);
        }

        /** Init attributes */
        Attributes.initAttributes(model);

        for (Layer layer: layers) {
            layer.getMachines().forEach(Machine::init);
        }

        return model;
    }

    /** Reads machines configuration from file */
    private static List<List<Machine>> getMachinesConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Reader reader = new InputStreamReader(new ResourceFileReader().getResourcesFileStream("machines.json"))) {
            Type type = new TypeToken<List<List<Machine>>>() {

            }.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
