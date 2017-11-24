package genericview;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import utils.FieldComparator;
import utils.ParametrizedAction;
import utils.ReflectionUtils.DisplayName;
import utils.ReflectionUtils.Editable;

/**
 * This class manage to display any data type without any complementary
 * code from you to manage the data insertion in the table.
 * 
 * @author Alex6092
 * 
 * @param <T> : the data's data type you want to display.
 */
@SuppressWarnings("serial")
public class GenericEditableTable<T extends Object> extends JTable {
	/**
	 * The data type to display.
	 */
	final Class<T> typeParameterClass;
	
	
	/**
	 * Is the table a read only view.
	 */
	private boolean m_isReadOnly = false;
	
	
	/**
	 * The optional contextual menu to display when user right click an entry 
	 * in the table
	 */
	private JPopupMenu m_oMenu = null;
	
	
	/**
	 * The data displayed by the table.
	 */
	private List<T> m_model = null;
	
	/**
	 * Initialise the GenericEditableTable instance with the model and the type
	 * of the template's parameter class.
	 * @param model : the data to display.
	 * @param typeParameterClass : data to display type.
	 */
	public GenericEditableTable(List<T> model, Class<T> typeParameterClass)
	{  
		this(model, typeParameterClass, "");
	}
	
	/**
	 * Initialise the GenericEditableTable instance with the model and the type
	 * of the template's parameter class.
	 * @param model : the data to display.
	 * @param typeParameterClass : data to display type.
	 * @param defaultStrDisplayedIfEmpty : the string to display when there is nothing to display.
	 */
	public GenericEditableTable(List<T> model, Class<T> typeParameterClass, String defaultStrDisplayedIfEmpty)
	{
		this.typeParameterClass = typeParameterClass;
		InitializeGenericEditableTable(model, defaultStrDisplayedIfEmpty);
	}
	
	
	/**
	 * Initialise the GenericEditableTable instance with the parameters get by 
	 * the constructor.
	 * @param model : the data to display.
	 * @param defaultStrDisplayedIfEmpty : the string to display when there is nothing to display.
	 */
	private void InitializeGenericEditableTable(List<T> model, String defaultStrDisplayedIfEmpty)
	{
	    m_model = model;
		
		Vector<String> headers = new Vector<String>();
	    
	    HashSet<Integer> notEditableColumn = new HashSet<Integer>();
	    
	    if(model.size() == 0)
	    	headers.addElement(defaultStrDisplayedIfEmpty);
	    else
	    {	    	
	    	Field[] fields = typeParameterClass.getFields();
	    	Arrays.sort(fields, new FieldComparator());
	    	
	    	int iCol = 0;
	    	
	    	for(Field field : fields)
	    	{
	    		DisplayName name = field.getAnnotation(DisplayName.class);
	    		Editable enabled = field.getAnnotation(Editable.class);
	    		headers.addElement(name != null ? name.name() : field.getName());
	    		if(enabled != null && !enabled.enabled())
	    		{
	    			notEditableColumn.add(iCol);
	    		}
	    		
	    		iCol++;
	    	}
	    }
	    
	    // Manage data "editability" :
	    DefaultTableModel dm = new DefaultTableModel()
		{
			@Override
			public boolean isCellEditable(int row, int column)
			{
				if(m_isReadOnly || notEditableColumn.contains(column))
					return false;
				
				return true;
			}
		};	
		
	    setModel(dm);	
	    
	    // Fill table's data model with data :
	    dm.setDataVector(modelToVector(model), headers);
	    
	    // Update the data model when a data is edited :
	    dm.addTableModelListener(new TableModelListener() {	    	
			@Override
			public void tableChanged(TableModelEvent arg0) {				
				int row = arg0.getFirstRow();
				int col = arg0.getColumn();
				
				if(row != TableModelEvent.HEADER_ROW)
				{
					Object objToUpdate = model.get(row);
					Field[] fields = typeParameterClass.getFields();
					Arrays.sort(fields, new FieldComparator());
					
					List<Field> fieldsToUpdate = new LinkedList<Field>();
					if(col == TableModelEvent.ALL_COLUMNS)
					{
						for(int i = 0; i < fields.length; i++)
							fieldsToUpdate.add(fields[i]);
					}
					else
					{
						fieldsToUpdate.add(fields[col]);
					}

					int iCol = 0;
					for(Field fieldToUpdate : fieldsToUpdate)
					{
						try
						{
							fieldToUpdate.set(objToUpdate, getValueAt(row, fieldsToUpdate.size() > 1 ? iCol : col));
						}
						catch(Exception e)
						{
							System.out.println("An error occurred during the data model update : " + e.getMessage());
							
							int colToRestore;
							if(fieldsToUpdate.size() > 1)
								colToRestore = iCol;
							else
								colToRestore = col;
							
							try {
								setValueAt(fieldToUpdate.get(objToUpdate), row, colToRestore);
							} 
							catch (Exception ex) 
							{
								ex.printStackTrace();
							}
						}
						
						iCol++;
					}
				}
			}
	    	
	    });
	    
	    // Activate sorting :
	    this.setAutoCreateRowSorter(true);
	    
	    // Listen to sorting event to reorder the model :
	    this.getRowSorter().addRowSorterListener(new RowSorterListener() {
            @Override
            public void sorterChanged(RowSorterEvent e) {
            	@SuppressWarnings("unchecked")
				List<SortKey> sortKeys = e.getSource().getSortKeys();
            	
            	Field[] fields = typeParameterClass.getFields();
		    	Arrays.sort(fields, new FieldComparator());
            	
            	for(SortKey sortKey : sortKeys)
            	{
            		m_model.sort(new Comparator<T>(){

						@Override
						public int compare(T o1, T o2) {
							try
							{
								int col = sortKey.getColumn();
								
								if(col < fields.length)
								{
									Field sorter = fields[col];
									String val1 = sorter.get(o1).toString();
									String val2 = sorter.get(o2).toString();
									
									if(sortKey.getSortOrder() == SortOrder.ASCENDING)
										return val1.compareTo(val2);
									else if(sortKey.getSortOrder() == SortOrder.DESCENDING)
										return val2.compareTo(val1);
									else
										return 0;
								}
							}
							catch(Exception ex)
							{
								ex.printStackTrace();
							}
							
							return 0;
						}
            			
            		});
            		
            		break;
            	}
            }
        });
                
	    
	    // Disable column reordering :
	    this.getTableHeader().setReorderingAllowed(false);
	    
	    // Mouse management (right click) :
	    this.addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseReleased(MouseEvent e) {
	            int r = rowAtPoint(e.getPoint());
	            if (r >= 0 && r < getRowCount()) {
	                setRowSelectionInterval(r, r);
	            } else {
	                clearSelection();
	            }

	            int rowindex = getSelectedRow();
	            if (rowindex < 0)
	                return;
	            if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
	                if(m_oMenu != null)
	                	m_oMenu.show(e.getComponent(), e.getX(), e.getY());
	            }
	        }
	    });
	}
	
	
	/**
	 * Set the optional contextual menu to display when user right-click
	 * an entry in the table.
	 * @param menu : your custom contextual menu.
	 */
	public void setPopupMenu(JPopupMenu menu)
	{
		m_oMenu = menu;
	}
	
	
	/**
	 * Change the read-only state of the table
	 * @param isReadOnly : boolean
	 */
	public void setReadOnly(boolean isReadOnly)
	{
		m_isReadOnly = isReadOnly;
	}
	
	
	/**
	 * Internal util to insert the data in the table.
	 * @param model : the data.
	 * @return The formatted data to be inserted in the table data model.
	 */
	private Vector<Vector<Object>> modelToVector(List<T> model) {
	    Vector<Vector<Object>> vector = new Vector<Vector<Object>>();
	    for (int i = 0; i < model.size(); i++) {
			Vector<Object> v = new Vector<Object>();
			  
			Object obj = model.get(i);
			Field[] fields = typeParameterClass.getFields();
			Arrays.sort(fields, new FieldComparator());
			  
			for(Field field : fields)
			{
				try
				{
					field.setAccessible(true);
			    	Object o = field.get(obj);
			    	v.addElement(o);
				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
				}
			}
			  
			vector.addElement(v);
	    }
	    return vector;
	}
	
	
	/**
	 * Execute the parametrized action passed in parameter with the selected 
	 * data in the table as the executeAction's parameter. This allow you to
	 * execute custom action on data when an item of your custom contextual
	 * menu is clicked.
	 * @param action : Your custom ParametrizedAction to execute with the
	 * right clicked data.
	 */
	public void executeActionOnClickedItem(ParametrizedAction action)
	{
		int rowIndex = getSelectedRow();
		
		if(rowIndex >= 0 && rowIndex < m_model.size())
		{
			T obj = m_model.get(rowIndex);
			action.executeAction(obj);
		}
	}
}
