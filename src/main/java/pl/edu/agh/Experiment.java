package pl.edu.agh;

import pl.edu.agh.flowshop.Model;
import pl.edu.agh.utils.ConfigReader;

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
