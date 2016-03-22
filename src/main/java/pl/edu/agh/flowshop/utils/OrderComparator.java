package pl.edu.agh.flowshop.utils;

import pl.edu.agh.flowshop.entity.Order;

import java.util.Comparator;

/**
 * Orders comparator
 *
 * @author Bartosz Sądel
 *         Created on 19.03.2016.
 */
public class OrderComparator implements Comparator<Order> {

    @Override
    public int compare(final Order o1, final Order o2) {
        /* Sorting from least to greatest, for priority queue to work right */
        return o2.getPriority() - o1.getPriority();
    }
}
