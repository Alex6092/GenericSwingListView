package utils;

/**
 * Abstract class to overload to execute specific treatment when
 * you need the right clicked data to process the right click
 * event of an entry of your custom contextual menu.
 * @author Alex6092
 */
public abstract class ParametrizedAction {
	/**
	 * executeAction is the callback the GenericEditableTable will
	 * call with the right clicked data as parameter.
	 * @param o : the object right clicked in the table.
	 */
	public abstract void executeAction(Object o);
}
