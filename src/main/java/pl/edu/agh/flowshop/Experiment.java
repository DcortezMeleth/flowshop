package pl.edu.agh.flowshop;

import pl.edu.agh.flowshop.engine.Model;
import pl.edu.agh.flowshop.utils.ConfigReader;

/**
 * @author Bartosz
 *         Created on 2016-03-01.
 */
public class Experiment {

    public static void main(String[] args) {
        //TODO: dodac logowanie
        try {
            Model model = ConfigReader.createModel();
            model.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
