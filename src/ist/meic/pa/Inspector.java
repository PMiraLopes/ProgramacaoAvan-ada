package ist.meic.pa;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Inspector {

	private Scanner scanner;
	private String userInput;

	private Object objectInstance;
	private ArrayList<Object> objectHistory;
	private HashMap<String,Field> objectFields;

	public Inspector(){}

	public void inspect(Object object) {

		scanner = new Scanner(System.in);
		objectHistory = new ArrayList<Object>();

		objectInstance = object;
		objectFields = this.getFields();

		while(true) {

			this.printObjectFeatures();

			/*** User interaction ***/
			System.err.print("\n> ");

			userInput = scanner.next();

			if(userInput.equals("q")) {
				return;

			} else if(userInput.equals("i")) {

				String name = scanner.next();
				Object temp = this.getFieldValue(name);
				
				if(temp != null) {
					objectHistory.add(objectInstance);
					
					objectInstance = temp;
					objectFields = this.getFields();
					
				} else {
					System.err.println("Cannot instantiate class object: " + name);
				}
				
			} else if(userInput.equals("m")) {

				String name = scanner.next();
				String value = scanner.next();

				this.fieldTypeModifier(name, value);

			} else if(userInput.equals("c")) {
				
				String name = scanner.next();
				
				String arg = scanner.nextLine();	
				String[] args = arg.split(" ");
			
				this.methodInvoke(name, args);
			}
		}
	}

	/**
	 * Gets the object representing the field value.
	 * 
	 * @param name The field name
	 * @return A field value object
	 */
	private Object getFieldValue(String name) {

		Object object = null;
		
		//Check if the field exists
		if (objectFields.containsKey(name)) {

			try {
				object = objectFields.get(name).get(objectInstance);
			} catch (Exception e) {
				System.err.println("Cannot get field value: " + e.getMessage());
			} 
			
		} else {
			System.err.println("No such field: " + name);
		}
		
		return object;
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
	 * @param clazz The class object
	 * @return All class fields
	 */
	private HashMap<String,Field> getFields() {

		HashMap<String,Field> fields = new HashMap<String,Field>();

		for(Class<?> c = objectInstance.getClass(); c != null; c = c.getSuperclass()) {
			for(Field f : c.getDeclaredFields()){
				if(!f.isAccessible())
					f.setAccessible(true);
				fields.put(f.getName(),f);
			}
		}
		return fields;
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

		System.err.println("-------------");

		//Print object fields
		for(Field field : objectFields.values()) {	
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
	}

	/**
	 * Modifies the value of the field named fieldName so that it becomes value
	 * 
	 * @param fieldName The object field name to be re-valued
	 * @param value The new object field value
	 */
	private void fieldTypeModifier(String fieldName, String value) {

		Field field = null;

		try {
			field = this.getField(fieldName);

			if(!field.getType().isPrimitive()) { //If not primitive type, use constructor by reflection (assuming string argument only constructor)
				field.set(objectInstance, field.getType().getConstructor(String.class).newInstance(value));

			} else { //Otherwise use enumerate to convert to the correct type
				field.set(objectInstance, Type.valueOf(field.getType().getSimpleName().toUpperCase()).convertType(value));
			}
		} catch (Exception e) {
			System.err.println("Class object field cannot be modified: " + e.getLocalizedMessage());
		} 
	}
	
	/**
	 * Invokes the method with the name and arguments passed
	 * as function arguments.
	 * 
	 * @param name The method name
	 * @param args The method arguments
	 */
	private void methodInvoke(String name, String[] args) {
		
		Method[] methods = objectInstance.getClass().getMethods();
		
		for(int i = 0, j = 0; i < methods.length; i++) {
			if(methods[i].getName().equals(name)) {
				if(methods[i].getParameterTypes().length == args.length - 1) {
					j++;
					System.err.println(j + ": " + methods[i]);
				} 
			}
		}	
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