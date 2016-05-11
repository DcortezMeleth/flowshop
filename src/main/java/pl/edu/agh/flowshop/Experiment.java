package pl.edu.agh.flowshop;

import pl.edu.agh.flowshop.engine.Model;
import pl.edu.agh.flowshop.utils.ConfigReader;
import pl.edu.agh.flowshop.utils.GraphPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bartosz
 *         Created on 2016-03-01.
 */
public class Experiment {

    public static void main(String[] args) {
        try {
            GraphPanel graph;
            //SwingUtilities.invokeLater(new Runnable() {
            //    public void run() {
            graph = createAndShowGui();
            //    }
            //});
            Model model = ConfigReader.createModel();
            List<Double> results = model.run();
            graph.setScores(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static GraphPanel createAndShowGui() {
        GraphPanel mainPanel = new GraphPanel(new ArrayList<Double>());
        JFrame frame = new JFrame("FlowShop_Graph");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return mainPanel;
    }
}
