# GenericSwingListView
The automatically generated editable view.

# Why do I use it ?
If you need a fast way to display and allow edition of data. It allows you to do it with few modifications of existing code and no more code to manage the display in the table.

# How do I use it ?
It is pretty simple to use you just need to add annotations to existing class to organize the display of your class fields you want to display. As an example you have the following existing class :
```java
public class ModelTest {
  public String test;
  public int id;    
}
```
If you want the id to be the first column and test to be the second column you just add the annotation @Order to do so.
If you want to define the text in the header of each column the annotation @DisplayName do it for you.
You want the id column not to be editable ? No problem, you just need to add the annotation @Editable as following :
```java
public class ModelTest {
  @Order(value=2)	
  @DisplayName(name="Test")	
  public String test;
  
  @Order(value=1) 
  @DisplayName(name="Id") 
  @Editable(enabled=false)  
  public int id;    
}
```

Your data are now ready to be displayed as you want, all you have to do is to make a list of the data you want to display and construct the table :
```java
// Test data :
List<ModelTest> test = new ArrayList<ModelTest>();
ModelTest t = new ModelTest();
t.id = 30;
t.test = "Test column 1";
test.add(t);	    	    

ModelTest t2 = new ModelTest();
t2.id = 5;	    
t2.test = "Test column 2";
test.add(t2);

// Instanciate the GenericEditableTable :
GenericEditableTable<ModelTest> table = new GenericEditableTable<ModelTest>(test, ModelTest.class, "No element to display");	   

//JTable customisations :
table.setShowGrid(false);   
table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
...
```

You need a custom contextual menu with custom actions on your displayed data ? Just do as following :
```java
// Create the custom contextual menu :
JPopupMenu popup = new JPopupMenu();
JMenuItem oProperties = new JMenuItem("Properties");
oProperties.addActionListener(new ActionListener() {
  @Override			
  public void actionPerformed(ActionEvent evt) {
    table.executeActionOnClickedItem(new ParametrizedAction() {
      @Override					
      public void executeAction(Object o) {
        ModelTest oModel = (ModelTest) o;
        System.out.println("Show properties of item : " + oModel.id);					
      }				
    });			
  }	    
});        
popup.add(oProperties);	    
table.setPopupMenu(popup);	// Set the table custom contextual menu.
```
