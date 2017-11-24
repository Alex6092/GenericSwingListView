package utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotations to customise your class fields to be
 * displayed in the GenericEditableTable.
 * @author Alex6092
 */
public class ReflectionUtils {
	/**
	 * Manage the fields order in the GenericEditableTable instance.
	 * (the columns order).
	 * @author Alex6092
	 */
	@Retention(RetentionPolicy.RUNTIME)
    public @interface Order {
        int value();
    }
	
	/**
	 * The text displayed in the corresponding column header.
	 * @author Alex6092
	 */
	@Retention(RetentionPolicy.RUNTIME)
    public @interface DisplayName {
        String name();
    }
	
	/**
	 * Indicate to the GenericEditableTable if it shall allow 
	 * the field to be editable.
	 * @author Alex6092
	 */
	@Retention(RetentionPolicy.RUNTIME)
    public @interface Editable {
		boolean enabled();
    }
}
