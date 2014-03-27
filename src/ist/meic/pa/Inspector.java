package ist.meic.pa;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

public class Inspector {

	private Scanner scanner;
	private String userInput;

	private Object objectInstance;
	private ArrayList<Object> objectHistory;
	private HashMap<String,ArrayList<String>> typeMap;

	public Inspector(){}

	public void inspect(Object object) {

		this.populateTypeMap();

		System.err.println("---------------------------------------------");
		System.err.println("--     OBJECT INSPECTOR ('h' for help)     --");
		System.err.println("---------------------------------------------\n");

		scanner = new Scanner(System.in);
		objectHistory = new ArrayList<Object>();

		objectInstance = object;
		objectHistory.add(objectInstance);

		while(true) {

			try {
				System.err.println("------ CURRENT OBJECT INSPECTION ------\n");
				this.printObjectFeatures(objectInstance);

			} catch (Exception e) {
				System.err.println("Can not print object " + objectInstance.getClass() + " features. " + e.getMessage() + "\n");
			}

			/*** User interaction ***/
			System.err.print("> ");

			userInput = scanner.next();

			if(userInput.equals("q")) {
				System.err.println("Successfully exited!");
				return;

			} else if(userInput.equals("i")) {

				String name = scanner.next();
				Object tempObject = null;

				try {

					tempObject = this.getField(objectInstance, name, true).get(objectInstance);

					objectInstance = tempObject;
					
					if(!objectHistory.contains(objectInstance)) {
						objectHistory.add(objectInstance);
					}	
					
				} catch (Exception e) {
					System.err.println("Can not instantiate class object " + name + ". " + e.getMessage() + "\n");
				}

			} else if(userInput.equals("m")) {

				String name = scanner.next();

				String userInput = scanner.nextLine().trim();
				String[] value = userInput.split(" ");	

				Field field = null;

				try {
					field = this.getField(objectInstance, name, false);
					field.set(objectInstance, this.parseInput(value).get(0));

				} catch (Exception e) {
					System.err.println("Can not change field value " + name + ". " + e.getMessage() + "\n");
				} 

			} else if(userInput.equals("c")) {

				String name = scanner.next();

				String userInput = scanner.nextLine().trim();
				String[] args = userInput.split(" ");
				
				try {
					this.methodInvoke(name, args);

				} catch (Exception e) {
					System.err.println("Can not invoke method " + name + ". " + e.getMessage() + "\n");
				} 

			} else if(userInput.equals("w")) {

				try {
					this.printObjectHistory();
				} catch (Exception e) {
					System.err.println("Can not print " + objectInstance.getClass() + " history. " + e.getMessage() + "\n");
				}
				
			} else if (userInput.equals("h")) {
				this.printHelp();
			}
		}
	}

	/**
	 * Populate hashMap with matching patterns
	 */
	private void populateTypeMap() {

		typeMap = new HashMap<String, ArrayList<String>>();

		typeMap.put("FLOAT", new ArrayList<String>(Arrays.asList("f", "F")));
		typeMap.put("FLOATP", new ArrayList<String>(Arrays.asList(".")));
		typeMap.put("DOUBLE",new ArrayList<String>(Arrays.asList("d", "D")));
		typeMap.put("LONG", new ArrayList<String>(Arrays.asList("l", "L")));
		typeMap.put("SHORT", new ArrayList<String>(Arrays.asList("s", "S")));
		typeMap.put("STRING", new ArrayList<String>(Arrays.asList("\"")));
		typeMap.put("BYTE", new ArrayList<String>(Arrays.asList("[")));
		typeMap.put("CHAR", new ArrayList<String>(Arrays.asList("'")));
	}

	/**
	 * Returns the field with the name passed as argument
	 * and makes it accessible.
	 * 
	 * @param name The field name we are looking for
	 * @param object The object containing the desired field
	 * @param read The read flag to distinguish if field is being read or written
	 * 
	 * @return The field
	 * 
	 * @throws NoSuchFieldException 
	 * @throws NullPointerException
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private Field getField(Object object, String name, Boolean read) throws NullPointerException,
	IllegalArgumentException, IllegalAccessException, NoSuchFieldException {

		Field field = null;

		for(Class<?> c = object.getClass(); c != null; c = c.getSuperclass()) {
			for(Field f : c.getDeclaredFields()){
				if(f.getName().equals(name) && !Modifier.isStatic(f.getModifiers())) {
					f.setAccessible(true);
					field = f;
				}
			}
		}

		if(field == null) {
			throw new NoSuchFieldException("No such field.");

		} else if(field.get(object) == null && read == true) {
			throw new NullPointerException("Null field value.");
		}

		return field;
	}

	/**
	 * Returns all the class fields and makes them accessible.
	 * 
	 * @return All class fields as an arraylist
	 */
	private ArrayList<Field> getFields(Object object) {

		ArrayList<Field> fields = new ArrayList<Field>();

		for(Class<?> c = object.getClass(); c != null; c = c.getSuperclass()) {
			for(Field f : c.getDeclaredFields()){
				if(!Modifier.isStatic(f.getModifiers())) {
					f.setAccessible(true);
					fields.add(f);		
				}
			}
		}
		return fields;
	}

	/**
	 * Returns all the class methods
	 * 
	 * @return All class methods as an arraylist
	 */
	private ArrayList<Method> getMethods(Object object) {

		ArrayList<Method> methods = new ArrayList<Method>();

		for(Class<?> c = object.getClass(); c != null; c = c.getSuperclass()) {
			for(Method m : c.getDeclaredMethods()){
				if(!Modifier.isStatic(m.getModifiers())) {
					m.setAccessible(true);
					methods.add(m);
				}
			}
		}
		return methods;
	}

	/**
	 * Invokes the method with the requested name and arguments
	 * 
	 * @param methodName The method named
	 * @param args The method arguments
	 * 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	private void methodInvoke(String methodName, String[] args) throws IllegalArgumentException, SecurityException, InstantiationException, 
	IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		ArrayList<Object> convertedArgsArray = this.parseInput(args);
		Object[] convertedArgs = convertedArgsArray.toArray(new Object[convertedArgsArray.size()]);
	
		Method matchedMethod = this.getMatchedMethod(methodName, convertedArgs);
		Object returnValue = matchedMethod.invoke(objectInstance, convertedArgs);

		System.err.println("\n------ METHOD INVOCATION INSPECTION ------");
		this.printObjectFeatures(returnValue);

		return;
	}

	/**
	 * Gets the method with the name and parameters
	 * passed as arguments.
	 * 
	 * @param methodName The method name
	 * @param convertedParameters The method parameters
	 * 
	 * @return The matched method
	 * 
	 * @throws NoSuchMethodException
	 * @throws InvalidParameterException
	 */
	private Method getMatchedMethod(String methodName, Object[] convertedParameters) throws NoSuchMethodException, InvalidParameterException {

		int highestMatch = 0;
		int highestMatchIndex = 0;

		ArrayList<Method> matchedMethods = new ArrayList<Method>();
		
		//Search for matching methods
		for(Method method: this.getMethods(objectInstance)) {
			
			if(method.getName().equals(methodName)) {
				if(method.getParameterTypes().length == convertedParameters.length) {
					method.setAccessible(true);
					matchedMethods.add(method);	
				}
			}
		}

		if(matchedMethods.size() == 0){
			throw new NoSuchMethodException("No such method or wrong number of parameters.");
		}

		for(int i = 0; i < matchedMethods.size(); i++) {

			Class<?>[] methodParameters = matchedMethods.get(i).getParameterTypes();
			int tempMatch = 0;

			for(int j = 0; j < methodParameters.length; j++) {
				if(convertedParameters[j].getClass().getSimpleName().toLowerCase().contains(methodParameters[j].getSimpleName().toLowerCase())) {
					tempMatch++;
				}
			}

			if(tempMatch >= highestMatch) {
				highestMatch = tempMatch;
				highestMatchIndex = i;
			}
		}
		
		if(highestMatch != convertedParameters.length) {
			throw new InvalidParameterException("Invalid parameter types.");
		}

		return matchedMethods.get(highestMatchIndex);
	}

	/**
	 * Parses the input received as argument and converts
	 * it to the corresponding data types
	 * 
	 * @param input The input to parse
	 * 
	 * @return The parsed input as an arraylist
	 */
	private ArrayList<Object> parseInput(String[] input) {

		ArrayList<Object> convertedInput = new ArrayList<Object>();
		
		//Check if arguments exist (non-arg methods case)
		if(input.length == 1 && input[0].equals("")) {
			return convertedInput;
		}
		
		for(int i = 0; i < input.length; i++) {

			int length = input[i].length();
			boolean match = false;

			//Special null case
			if(input[i].equals("null")) {
				convertedInput.add(null);
				continue;
			}

			for(Entry<String, ArrayList<String>> entry : typeMap.entrySet()) {

				if(entry.getValue().contains(input[i].substring(0, 1))  //Start with a certain string
						|| entry.getValue().contains(String.valueOf(input[i].charAt(0))) && entry.getValue().contains(String.valueOf(input[i].charAt(length - 1)))//Start and terminate with a certain char
						|| entry.getValue().contains(input[i].substring(length - 1))) { //Terminate with a certain string
					Object result = Type.valueOf(entry.getKey()).convertType(input, i);

					if(result != null) { //Check result validity
						convertedInput.add(result);
					}
					match = true;
					break;
				} 
			}

			//Float "." case
			if(input[i].contains(".") && !match) {
				convertedInput.add(Type.FLOATP.convertType(input, i));

			//Otherwise we assume it's an integer	
			} else if (!match){			
				convertedInput.add(Type.INT.convertType(input, i));
			}
		}
		return convertedInput;		
	}

	/**
	 * Prints object features: name, fields, methods
	 * 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private void printObjectFeatures(Object object) throws IllegalArgumentException, IllegalAccessException {

		//Print object name and class instance
		if(object.toString().equals("")) {
			System.err.println("Instance of " + objectInstance.getClass());
		} else {
			System.err.println(objectInstance + " is an instance of " + object.getClass());
		}	

		System.err.println("\n------ FIELDS ------");

		//Print object fields
		for(Field field : this.getFields(object)) {	

			String fieldsOutput = field.getType() + " " + field.getName() + " = " + field.get(object);

			if(field.getModifiers() == 0) {	//No modifier
				System.err.println(fieldsOutput);
			} else {
				System.err.println(Modifier.toString(field.getModifiers()) + " " + fieldsOutput);
			}
		}

		System.err.println("\n------ METHODS ------");

		//Print object methods
		for(Method method: this.getMethods(object)) {
			System.err.println(method.toString());
		}
		System.err.println("");
	}

	/**
	 * Prints object history
	 * 
	 * @throws Exception, NumberFormatException 
	 */
	private void printObjectHistory() throws Exception, NumberFormatException {

		if(objectHistory.size() > 0) {

			for(int i = 0; i < objectHistory.size(); i++) {
				System.err.println((i + 1) + ": " + objectHistory.get(i) + " - " + objectHistory.get(i).getClass());
			}

			System.err.print("\nInsert the desired object number: ");
			int number = Integer.parseInt(scanner.next()) - 1;

			objectInstance = objectHistory.get(number);

		} else {
			throw new Exception("Not enough objects in object history");
		}	
	}

	/**
	 * Prints the available command list
	 */
	private void printHelp() {

		System.err.println("---------------- AVAILABLE COMMANDS --------------");
		System.err.println(" q ............. quit");
		System.err.println(" i name ........ inspect name");
		System.err.println(" m name value .. modifies field value named name");
		System.err.println(" c name values . calls method with values");
		System.err.println(" w ............. gets the inspected object history");
		System.err.println("--------------------------------------------------\n");
	}

	/**
	 * Enumerates Java types.
	 * 
	 * Contains an abstract method overrided by each Type in order 
	 * to convert a specific argument to the corresponding object.
	 */
	public enum Type {

		INT {
			@Override
			public Object convertType(String[] input, int index) {
				return Integer.parseInt(input[index]);			
			}
		},

		FLOAT {
			@Override
			public Object convertType(String[] input, int index) {
				String stringResult = input[index].substring(0, input[index].length() - 1);
				return Float.parseFloat(stringResult);
			}	
		},

		FLOATP {
			@Override
			public Object convertType(String[] input, int index) {
				return Float.parseFloat(input[index]);
			}	
		},

		DOUBLE {
			@Override
			public Object convertType(String[] input, int index) {
				String stringResult = input[index].substring(0, input[index].length() - 1);
				return Double.parseDouble(stringResult);
			}	
		},

		LONG {
			@Override
			public Object convertType(String[] input, int index) {
				String stringResult = input[index].substring(0, input[index].length() - 1);
				return Long.parseLong(stringResult);
			}
		},

		SHORT {
			@Override
			public Object convertType(String[] input, int index) {
				String stringResult = input[index].substring(0, input[index].length() - 1);
				return Short.parseShort(stringResult);
			}	
		},

		STRING {
			@Override
			public Object convertType(String[] input, int index) {

				String stringResult = "";
				boolean terminates = false;

				//Re-check for starting substring to avoid consuming the same input
				if(input[index].startsWith("\"")) { 

					for(int i = index; i < input.length; i++) {

						if(input[i].endsWith("\"")) {
							stringResult += " " + input[i];
							terminates = true;
							break;
						}

						stringResult += " " + input[i];
					}
					return ((terminates) ? stringResult.trim().replace("\"", "") : null);		
				}
				else {
					return null;
				}
			}
		},

		CHAR {
			@Override
			public Object convertType(String[] input, int index) {			
				CharSequence charResult = input[index].subSequence(1, input[index].length() - 1);			
				return charResult.charAt(0);
			}	
		},

		BYTE {
			@Override
			public Object convertType(String[] input, int index) {
				String stringResult = input[index].subSequence(1, input[index].length() - 1).toString();
				return Byte.parseByte(stringResult);
			}
		};

		public abstract Object convertType(String[] input, int index);
	}
}