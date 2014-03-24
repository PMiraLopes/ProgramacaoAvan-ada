package ist.meic.pa;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
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

		objectInstance = this.createInstance(object.getClass());
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

				if (objectFields.containsKey(name)) {
					objectHistory.add(objectInstance);
				
					objectInstance = this.createInstance(objectFields.get(name).getType());
					objectFields = this.getFields();
					
				} else {
					System.err.println("No such field: " + name);
				}
				
			} else if(userInput.equals("m")) {
				
				String name = scanner.next();
				String value = scanner.next();
				
				this.fieldTypeModifier(name, value);
				
			} else if(userInput.equals("c")) {
				
			}
		}
	}

	/**
	 * Create an instance of class
	 * 
	 * @param clazz The class object
	 * @return An instance of class
	 */
	private Object createInstance(Class<?> clazz) {

		Object objectInstance = null;

		try {
			objectInstance = clazz.newInstance();
		} catch (InstantiationException e) {
			System.err.println("The specified class object cannot be instantiated: " + e.getMessage());
		} catch (IllegalAccessException e) {
			System.err.println("Illegal access: " + e.getMessage());
		}
		return objectInstance;
	}
	
	/**
	 * Returns the field with the name passed as argument
	 * and makes it accessible.
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
			} catch (IllegalArgumentException e) {
				System.err.println("Illegal argument: " + e.getMessage());
			} catch (IllegalAccessException e) {
				System.err.println("Illegal access: " + e.getMessage());
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
			
			if(!field.getType().isPrimitive()) { //If not primitive type, use constructor by reflection
				field.set(objectInstance, field.getType().getConstructor(String.class).newInstance(value));
				
			} else { //Otherwise use enumerate to convert to the correct type
				field.set(objectInstance, Type.valueOf(field.getType().getSimpleName().toUpperCase()).convertType(value));
			}
		} catch (SecurityException e) {
			System.err.println("Access denied: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			System.err.println("Illegal argument: " + e.getMessage());
		} catch (IllegalAccessException e) {
			System.err.println("Illegal access: " + e.getMessage());
		} catch (InstantiationException e) {
			System.err.println("The specified class object cannot be instantiated: " + e.getMessage());
		} catch (InvocationTargetException e) {
			System.err.println("Invalid invocation target: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			System.err.println("No such method: " + e.getMessage());
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