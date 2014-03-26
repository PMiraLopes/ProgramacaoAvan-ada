package ist.meic.pa;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Scanner;

public class Inspector {

	private Scanner scanner;
	private String userInput;

	private Object objectInstance;
	private ArrayList<Object> objectHistory;
	private ArrayList<Field> objectFields;
	private ArrayList<Method> objectMethods;

	public Inspector(){}

	public void inspect(Object object) {

		System.err.println("----- OBJECT INSPECTOR ('h' for help) -----\n");
		
		scanner = new Scanner(System.in);
		objectHistory = new ArrayList<Object>();

		objectInstance = object;
		objectFields = this.getFields();
		objectMethods = this.getMethods();

		while(true) {

			this.printObjectFeatures();

			/*** User interaction ***/
			System.err.print("\n> ");

			userInput = scanner.next();

			if(userInput.equals("q")) {
				return;

			} else if(userInput.equals("i")) {

				String name = scanner.next();

				try {
					objectHistory.add(objectInstance);

					objectInstance = this.getField(name).get(objectInstance);
					objectFields = this.getFields();
					objectMethods = this.getMethods();

				} catch (Exception e) {
					System.err.println("Cannot instantiate class object: " + name + "\n");
				}

			} else if(userInput.equals("m")) {

				String name = scanner.next();
				String value = scanner.next();
				
				Field field = getField(name);

				try {
					field.set(objectInstance, this.typeModifier(field.getType(), value));
				} catch (Exception e) {
					System.err.println("Cannot change field value: " + name + "\n");
				} 
				
			} else if(userInput.equals("c")) {

				String userInput = scanner.nextLine().trim();
				String[] args = userInput.split(" ");

				this.methodInvoke(args);
				
			} else if(userInput.equals("w")) {
				
				if(objectHistory.size() > 1) {
					
					for(int i = 0; i < objectHistory.size(); i++) {
						System.err.println((i + 1) + ": " + objectHistory.get(i));
					}
				
					System.err.print("\nInsert the desired object number: ");
					int number = scanner.nextInt() - 1;
				
					try {
						objectInstance = objectHistory.get(number);
						objectFields = this.getFields();
						objectMethods = this.getMethods();
						
					} catch(IndexOutOfBoundsException e) {
						System.err.println("Index number unavailable: " + e.getMessage() + "\n");
					}
					
				} else {
					System.err.println("Not enough objects in object history\n");
				}
				
			} else if (userInput.equals("h")) {
				this.printHelp();
			}
		}
	}

	/**
	 * Returns the field with the name passed as argument
	 * and makes it accessible. If the field doesn't exist
	 * returns null.
	 * 
	 * @param name The field name we are looking for
	 * @return The field
	 */
	private Field getField(String name) {

		Field field = null;

		for(Class<?> c = objectInstance.getClass(); c != null; c = c.getSuperclass()) {
			for(Field f : c.getDeclaredFields()){
				if(f.getName().equals(name)) {
					f.setAccessible(true);
					field = f;
				}
			}
		}
		return field;
	}

	/**
	 * Returns all the class fields and makes them accessible.
	 * 
	 * @return All class fields
	 */
	private ArrayList<Field> getFields() {

		ArrayList<Field> fields = new ArrayList<Field>();

		for(Class<?> c = objectInstance.getClass(); c != null; c = c.getSuperclass()) {
			for(Field f : c.getDeclaredFields()){
				if(!f.isAccessible()) {
					f.setAccessible(true);
				}
				fields.add(f);
			}
		}
		return fields;
	}
	
	/**
	 * Returns all the class methods
	 * 
	 * @return All class methods
	 */
	private ArrayList<Method> getMethods() {

		ArrayList<Method> methods = new ArrayList<Method>();

		for(Class<?> c = objectInstance.getClass(); c != null; c = c.getSuperclass()) {
			for(Method m : c.getDeclaredMethods()){
				if(!m.isAccessible()) {
					m.setAccessible(true);
				}
				methods.add(m);
			}
		}
		return methods;
	}
	
	

	/**
	 * Creates an object with the request value and type 
	 * 
	 * @param expectedType The object expected type
	 * @param value The object value
	 * 
	 * @return A new type of object
	 */
	private Object typeModifier(Class<?> expectedType, String value) {

		Object modifiedObject = null;

		try {
			if(!expectedType.isPrimitive()) { //If not primitive type, use constructor by reflection (assuming string argument only constructor)
				modifiedObject = expectedType.getConstructor(String.class).newInstance(value);
			} else { //Otherwise use enumerate to convert to the correct type
				modifiedObject =  Type.valueOf(expectedType.getSimpleName().toUpperCase()).convertType(value);
			}
		} catch (Exception e) {
			System.err.println("Class object cannot be modified: " + e.getMessage());
		} 

		return modifiedObject;
	}

	/**
	 * Invokes the method with the name and arguments passed
	 * as function arguments.
	 * 
	 * @param args The method name and arguments
	 */
	private void methodInvoke(String[] args) {

		ArrayList<Method> methods = this.getMethods();
		ArrayList<Method> matchedMethods = new ArrayList<Method>();
		Object[] convertedArgs = new Object[args.length - 1];

		String methodName = args[0];

		//Search for matching methods
		for(int i = 0; i < methods.size(); i++) {
			
			Method method = methods.get(i);
			method.setAccessible(true);
			
			if(method.getName().equals(methodName)) {
				if(method.getParameterTypes().length == args.length - 1) {
					matchedMethods.add(method);
					System.err.println((i + 1) + ": " + method);			
				} 
			}
		}	

		if(matchedMethods.size() == 0){
			System.err.println("The method " + methodName + " does not exist\n");
			return;
		}

		System.err.print("\nInsert the desired method number: ");

		int number = scanner.nextInt() - 1;
		
		try {
			Class<?>[] methodParameters = matchedMethods.get(number).getParameterTypes();

			//Convert method paremeters to the desired type
			for(int i = 0; i < methodParameters.length; i++){
				convertedArgs[i] = this.typeModifier(methodParameters[i], args[i + 1]);		
			}
		
			Object returnValue = matchedMethods.get(number).invoke(objectInstance, convertedArgs);
			System.err.println("Invocation result: " + returnValue + "\n");

		} catch(IndexOutOfBoundsException e) {
			System.err.println("Index number unavailable: " + e.getMessage() + "\n");			
		} catch(Exception e) {
			System.err.println("Cannot invoke method: " + e.getMessage() + "\n");
		}
	}
	
	/**
	 * Prints object features: name, fields, methods
	 */
	private void printObjectFeatures() {

		//Print object name and class instance
		if(objectInstance.toString().equals("")) {
			System.err.println("Instance of " + objectInstance.getClass());
		} else {
			System.err.println(objectInstance + " is an instance of " + objectInstance.getClass());
		}	

		System.err.println("\n-----FIELDS-----");

		//Print object fields
		for(Field field : objectFields) {	
			try {
				String fieldsOutput = field.getType() + " " + field.getName() + " = " + field.get(objectInstance);

				if(field.getModifiers() == 0) {	//No modifier
					System.err.println(fieldsOutput);
				} else {
					System.err.println(Modifier.toString(field.getModifiers()) + " " + fieldsOutput);
				}
			} catch (Exception e) {
				System.err.println(e.getLocalizedMessage());
			} 
		}
		
		System.err.println("\n-----METHODS-----");
		
		//Print object methods
		for(Method method: objectMethods) {
			System.err.println(method.toString());
		}
	}
	
	/**
	 * Prints the available command list
	 */
	private void printHelp() {
		
		System.err.println("----- AVAILABLE COMMANDS -----");
		System.err.println(" q ------------- quit");
		System.err.println(" i name -------- inspect name");
		System.err.println(" m name value -- modifies value of name");
		System.err.println(" c name values - calls name method");
		System.err.println(" w ------------- get the inspected object list");
		System.err.println(" h ------------- help");	
		System.err.println("------------------------------\n");
	}

	/**
	 * Enumerates every Java primitive type.
	 * 
	 * Contains an abstract method overrided by each Type in order 
	 * to convert a specific argument to the corresponding type.
	 */
	public enum Type {

		BOOLEAN {
			@Override
			public Object convertType(String value) {
				return Boolean.parseBoolean(value);
			}
		},	

		INT {
			@Override
			public Object convertType(String value) {
				return Integer.parseInt(value);			
			}
		},

		FLOAT {
			@Override
			public Object convertType(String value) {
				return Float.parseFloat(value);
			}	
		},

		DOUBLE {
			@Override
			public Object convertType(String value) {
				return Double.parseDouble(value);
			}	
		},

		LONG {
			@Override
			public Object convertType(String value) {
				return Long.parseLong(value);
			}
		},

		SHORT {
			@Override
			public Object convertType(String value) {
				return Short.parseShort(value);
			}	
		},

		CHAR {
			@Override
			public Object convertType(String value) {
				return value.charAt(1); //First index is for "'"
			}	
		},

		BYTE {
			@Override
			public Object convertType(String value) {
				return Byte.parseByte(value);
			}
		};

		public abstract Object convertType(String value);
	}
}