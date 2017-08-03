import java.lang.reflect.Array;
import java.util.ListIterator;

import javax.script.*;
import org.renjin.script.*;
import org.renjin.sexp.*;

import net.imagej.ImageJ;
import net.imagej.table.*;



public class TryRenjin {
	
  public static void main(String[] args) throws Exception {
    TableTest();
  }
  
  static ImageJ ij;
  
  public static void TableTest() {
	  ij  = new ImageJ();
	  // first, we create a table
      GenericTable table = createTable();

	  // after creating a table, you can show it to the user
	  ij.ui().show("Population of largest towns", table);

	  // now we will analyse the content of the table
	  analyseTable(table);
	  tabletoRdata(table,"xy4.RData");
	  
  }
  
  /**
	 * This function shows how to create a table with information
	 * about the largest towns in the world.
	 *
	 * @return a table with strings and numbers
	 */
	private static GenericTable createTable()
	{
		// we create two columns
		GenericColumn nameColumn = new GenericColumn("Town");
		DoubleColumn populationColumn = new DoubleColumn("Population");

		// we fill the columns with information about the largest towns in the world.
		nameColumn.add("Karachi");
		populationColumn.add(23500000.0);

		nameColumn.add("Bejing");
		populationColumn.add(21516000.0);

		nameColumn.add("Sao Paolo");
		populationColumn.add(21292893.0);

		// but actually, the largest town is Shanghai,
		// so let's add it at the beginning of the table.
		nameColumn.add(0, "Shanghai");
		populationColumn.add(0, 24256800.0);

		// After filling the columns, you can create a table
		GenericTable table = new DefaultGenericTable();

		// and add the columns to that table
		table.add(nameColumn);
		table.add(populationColumn);

		return table;
	}

	/**
	 * This function shows how to read out information from tables,
	 * such as
	 * - the header of a column
	 * - an entry from the table
	 *
	 * @param table A table with two columns, Town and Population
	 */
	private static void analyseTable(GenericTable table)
	{
		// read out the header of the second column
		String header = table.get(1).getHeader();

		ij.log().info("The header of the second column is: " + header);

		// get a certain column
		DoubleColumn populationColumn = (DoubleColumn)table.get("Population");

		// get a value from the first line in the column
		double populationOfLargestTown = populationColumn.get(0);

		ij.log().info("The population of the largest town is: " + populationOfLargestTown);
	}
	
	/**
	 * This function converts the generic table to a r dataframe inside a RData file
	 * Complicated by the type conversions required
	 * Results table would be easier if all numerical values are used
	 *
	 * @param table A table with two columns, Town and Population
	 * @throws ScriptException 
	 */
	private static void tabletoRdata(GenericTable table, String filename){
		
		// create the script engine
		RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
		ScriptEngine engine = factory.getScriptEngine();
		
		int rows = table.getRowCount();
		int cols = table.getColumnCount();
		
		engine.put("rows", rows);
		engine.put("cols", cols);
		
		try {
			engine.eval("names <- vector(length=cols)");
		} catch (ScriptException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//add column titles to a vector in R
		for(int i=0;i<cols;i++) {
		   ij.log().info("Column Type: " + table.get(i).getType().getName());
		   ij.log().info("Column Name: " + table.get(i).getHeader());
		   engine.put("i", i);
		   engine.put("name", table.get(i).getHeader());
		   
		   try {
			engine.eval("names[i+1] <- name");
		   } catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		   }
		}
			
		//allocate data for the columns - can we use reflection here to create these arrays
		// Class<?> c = Class.forName("java.lang.Double"); 
		// or interate over cols and rows
		String[] namedat = new String[rows];
		Double[] popdat = new Double[rows];
				
		engine.put("namecol", table.get(0).toArray(namedat));
		engine.put("popcol", table.get(1).toArray(popdat));
		engine.put("filename", filename);

		try {
			engine.eval("df <- data.frame(namecol,as.numeric(popcol))");
			engine.eval("colnames(df) <- names");
			engine.eval("save(df, file = filename)");
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.print("Written RData Files");
	
	}
}