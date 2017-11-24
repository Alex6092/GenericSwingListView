package utils;

import java.lang.reflect.Field;
import java.util.Comparator;

import utils.ReflectionUtils.Order;

/**
 * Comparator to order fields in the generic table according to
 * the @Order annotation attached to your class field.
 * Field without the @Order annotation are placed at the end.
 * @author Alex6092
 */
public class FieldComparator implements Comparator<Field>
{
    public int compare(Field o1, Field o2) {
        Order or1 = o1.getAnnotation(Order.class);
        Order or2 = o2.getAnnotation(Order.class);
        // nulls last
        if (or1 != null && or2 != null) {
            return or1.value() - or2.value();
        } else
        if (or1 != null && or2 == null) {
            return -1;
        } else
        if (or1 == null && or2 != null) {
            return 1;
        }
        return o1.getName().compareTo(o2.getName());
    }
}