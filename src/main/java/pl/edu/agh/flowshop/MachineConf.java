package pl.edu.agh.flowshop;

import java.util.Map;

/**
 * Machine configuration.
 *
 * @author Bartosz
 *         Created on 2016-03-07.
 */
public class MachineConf {

    /**
     * Machine configuration in form of map: </br>
     * <li>
     *     <ul>key - product type</ul>
     *     <ul>value - processing time</ul>
     * </li>
     */
    protected Map<Integer, Integer> timeTable;

    protected String classifierName = "";

    public MachineConf(final Map<Integer, Integer> timeTable) {
        this.timeTable = timeTable;
    }

}
