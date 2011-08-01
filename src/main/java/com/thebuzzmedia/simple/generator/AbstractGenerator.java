package com.thebuzzmedia.simple.generator;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.thebuzzmedia.common.io.CharArrayInput;
import com.thebuzzmedia.common.io.IInput;
import com.thebuzzmedia.common.util.ArrayUtils;
import com.thebuzzmedia.common.util.Base64;
import com.thebuzzmedia.simple.generator.IIndenter.Position;
import com.thebuzzmedia.simple.generator.IIndenter.Type;
import com.thebuzzmedia.simple.generator.annotation.Encode;
import com.thebuzzmedia.simple.generator.annotation.Recursable;

/**
 * Class used to provide the base implementation of a reflection-based
 * {@link IGenerator}.
 * <p/>
 * This class is intended to provide a base implementation for all generators by
 * providing the reflection and type-inference work necessary to pull names and
 * values out of an object and then delegates to simple stubbed out methods that
 * any implementor can provide logic for.
 * 
 * @author Riyad Kalla (software@thebuzzmedia.com)
 * @since 1.1
 */
public abstract class AbstractGenerator implements IGenerator {
	private static final int DEFAULT_BUFFER_SIZE = 256;
	private static final float BUFFER_GROWTH_FACTOR = 1.5f;

	private static final char[] BOOLEAN_TRUE = new char[] { 't', 'r', 'u', 'e' };
	private static final char[] BOOLEAN_FALSE = new char[] { 'f', 'a', 'l',
			's', 'e' };

	private int level;
	private IIndenter indenter;

	private int length;
	private char[] buffer;
	private int initialBufferSize;

	private boolean fieldCachePersisted;
	private Map<Class<?>, Field[]> fieldCache;

	public AbstractGenerator() {
		this(CompactIndenter.INSTANCE);
	}

	public AbstractGenerator(IIndenter indenter)
			throws IllegalArgumentException {
		this(indenter, DEFAULT_BUFFER_SIZE);
	}

	public AbstractGenerator(IIndenter indenter, int initialBufferSize)
			throws IllegalArgumentException {
		setIndenter(indenter);

		if (initialBufferSize < 0)
			throw new IllegalArgumentException("initialBufferSize ["
					+ initialBufferSize + "] must be >= 0");

		this.initialBufferSize = initialBufferSize;

		fieldCache = new HashMap<Class<?>, Field[]>(256);
		fieldCachePersisted = true;
	}

	public void reset() {
		level = 0;
		length = 0;

		// Clear the field cache if we don't want it persisted.
		if (!fieldCachePersisted)
			fieldCache.clear();

		// Create a new char[] buffer to hold our generated content.
		buffer = new char[initialBufferSize];
	}

	/**
	 * Used to determine if the internal {@link Field}[] cache is kept between
	 * calls to {@link #generate(Object)} or if it is cleared each time a
	 * generation is done.
	 * <p/>
	 * If you are generating representations of many of the same kinds of
	 * objects, keeping the cache will offer the bigger performance advantage.
	 * If every time you perform a generation step the objects are different,
	 * clearing the cache between calls to {@link #generate(Object)} is fine.
	 * <p/>
	 * To improve performance during generation, this class caches the
	 * {@link Field}[] retrieved from a generated object for re-use each time an
	 * object of the same type is processed; this avoids the cost of the
	 * reflection call into the object to get its fields for the same class
	 * type.
	 * <p/>
	 * Given the structured/repetitive nature of generation, keeping a cached
	 * copy of the {@link Field}[] during generation, but especially between
	 * subsequent calls to {@link #generate(Object)} can be offer a big win in
	 * performance.
	 * 
	 * @return <code>true</code> if the field cache is kept between generate
	 *         calls or <code>false</code> if it is cleared each time.
	 */
	public boolean isFieldCachePersisted() {
		return fieldCachePersisted;
	}

	/**
	 * Used to set if the internal {@link Field}[] cache should be kept after
	 * calls to {@link #generate(Object)} or cleared each time a generation is
	 * done.
	 * <p/>
	 * To improve performance during generation, this class caches the
	 * {@link Field}[] retrieved from a generated object for re-use each time an
	 * object of the same type is processed; this avoids the cost of the
	 * reflection call into the object to get its fields for the same class
	 * type.
	 * <p/>
	 * Given the structured/repetitive nature of generation, keeping a cached
	 * copy of the {@link Field}[] during generation, but especially between
	 * subsequent calls to {@link #generate(Object)} can be offer a big win in
	 * performance.
	 * 
	 * @param fieldCachePersisted
	 *            <code>true</code> if the internal {@link Field}[] cache should
	 *            be kept after each call to {@link #generate(Object)} or
	 *            <code>false</code> if the cache should be manually cleared
	 *            each time.
	 */
	public void setFieldCachePersisted(boolean fieldCachePersisted) {
		this.fieldCachePersisted = fieldCachePersisted;
	}

	public IIndenter getIndenter() {
		return indenter;
	}

	public void setIndenter(IIndenter indenter) throws IllegalArgumentException {
		if (indenter == null)
			throw new IllegalArgumentException("indenter cannot be null");

		this.indenter = indenter;
	}

	public IInput<char[], char[]> generate(Object object) {
		// Reset the generator's state
		reset();

		// Ensure there is work to be done.
		if (object != null) {
			Class<?> type = object.getClass();
			writeDispatcher(typeToName(type), type, null, object, false);
		}

		return new CharArrayInput(buffer, 0, length);
	}

	protected AbstractGenerator append(char c) {
		buffer = ArrayUtils.ensureCapacity(buffer, length + 1,
				BUFFER_GROWTH_FACTOR);
		buffer[length++] = c;

		return this;
	}

	protected AbstractGenerator append(char[] text) {
		if (text != null && text.length > 0) {
			buffer = ArrayUtils.ensureCapacity(buffer, length + text.length,
					BUFFER_GROWTH_FACTOR);
			System.arraycopy(text, 0, buffer, length, text.length);
			length += text.length;
		}

		return this;
	}

	protected AbstractGenerator append(String text) {
		if (text != null) {
			int l = text.length();

			if (l > 0) {
				buffer = ArrayUtils.ensureCapacity(buffer, length + l,
						BUFFER_GROWTH_FACTOR);
				text.getChars(0, l, buffer, length);
				length += l;
			}
		}

		return this;
	}

	protected AbstractGenerator append(Boolean value) {
		if (value != null) {
			if (value.booleanValue())
				append(BOOLEAN_TRUE);
			else
				append(BOOLEAN_FALSE);
		}

		return this;
	}

	protected AbstractGenerator append(Number value) {
		if (value != null) {
			String text = null;

			/*
			 * This is done instead of Number.toString() to avoid the 4 or 5
			 * levels of redirection that Number.toString() invokes, bouncing
			 * between abstract class toString methods to String.valueOf back to
			 * the concrete wrapper classes toString(val) methods.
			 * 
			 * We short circuit all that bouncing and go directly to the end
			 * method call below.
			 */
			if (value instanceof Integer)
				text = Integer.toString(((Integer) value).intValue());
			else if (value instanceof Double)
				text = Double.toString(((Double) value).doubleValue());
			else if (value instanceof Long)
				text = Long.toString(((Long) value).longValue());
			else if (value instanceof Float)
				text = Float.toString(((Float) value).floatValue());
			else if (value instanceof Byte)
				text = Byte.toString(((Byte) value).byteValue());
			else if (value instanceof Short)
				text = Short.toString(((Short) value).shortValue());

			append(text);
		}

		return this;
	}

	protected abstract void writeObjectOpen(String fieldName, int level,
			boolean inList);

	protected abstract void writeObjectClose(String fieldName, int level,
			boolean inList);

	protected abstract void writeListOpen(String fieldName, int level,
			boolean inList);

	protected abstract void writeListClose(String fieldName, int level,
			boolean inList);

	protected abstract void writeListSeparator();

	protected abstract void writeBoolean(String fieldName, Boolean value,
			int level, boolean inList);

	protected abstract void writeNumber(String fieldName, Number value,
			int level, boolean inList);

	protected abstract void writeString(String fieldName, String value,
			int level, boolean inList);

	private void indent(Type type, Position position) {
		// Get the indent for the current type and position.
		char[] indent = indenter.getIndent(type, position, level);

		// Only append if the indenter gave us anything non-empty.
		if (indent != null && indent.length > 0)
			append(indent);
	}

	private String typeToName(Class<?> type) {
		String name = null;

		if (type != null) {
			name = type.getName();

			// Check if this is a sub-class
			int i = name.lastIndexOf('$');

			// If not, then get the last package index.
			if (i == -1)
				i = name.lastIndexOf('.');

			char[] chars = new char[name.length() - (i + 1)];
			name.getChars(i + 1, name.length(), chars, 0);

			char c = chars[0];

			// Lowercase the first char if necessary
			if (c > 64 && c < 91)
				chars[0] = (char) (c + 32);

			// Create new name String
			name = new String(chars);
		}

		return name;
	}

	private void openObject(String fieldName, boolean inList) {
		indent(Type.OBJECT_OPEN, Position.BEFORE);
		writeObjectOpen(fieldName, level, inList);
		level++;
		indent(Type.OBJECT_OPEN, Position.AFTER);
	}

	private void closeObject(String fieldName, boolean inList) {
		level--;
		indent(Type.OBJECT_CLOSE, Position.BEFORE);
		writeObjectClose(fieldName, level, inList);
		indent(Type.OBJECT_CLOSE, Position.AFTER);
	}

	private void openList(String fieldName, boolean inList) {
		indent(Type.LIST_OPEN, Position.BEFORE);
		writeListOpen(fieldName, level, inList);
		level++;
		indent(Type.LIST_OPEN, Position.AFTER);
	}

	private void closeList(String fieldName, boolean inList) {
		level--;
		indent(Type.LIST_CLOSE, Position.BEFORE);
		writeListClose(fieldName, level, inList);
		indent(Type.LIST_CLOSE, Position.AFTER);
	}

	/**
	 * For anyone reading through this code and curious about the organization
	 * of this class, here is an outline.
	 * <p/>
	 * This class more or less follows the requirements outlined by the JSON
	 * format; more specifically, that any value can be an array structure, an
	 * object structure or a simple name/value pair. These values can be pulled
	 * from any array or object themselves.
	 * <p/>
	 * Given that "anything can contain anything else" structure,
	 * writeDispatcher is the central method that all other "discovery" methods
	 * call into with their array elements or reflected field values and this
	 * method provides all the logic necessary to decide how that object should
	 * be handled.
	 * <p/>
	 * This method will dispatch to 3 primary methods: writeObject,
	 * writeArray/List/Collection or writeValue if the value that comes in is an
	 * object, some list structure or a simple name/value respectively.
	 * <p/>
	 * Simple Gen only supports recursing on objects annotated with
	 * {@link Recursable} so any other object (that isn't a list structure or
	 * simple data type) will have its toString() value written as a String with
	 * a call to writeValue.
	 * 
	 * @param name
	 *            The name of the object, field or list.
	 * @param type
	 *            The type of the value.
	 * @param encoding
	 *            The Encode annotation used on the field, if there was one.
	 * @param value
	 *            The actual value of the object, field or list.
	 * @param listItem
	 *            Indicates if the item is directly contained with a list, some
	 *            formats like JSON, will change how they are rendered if this
	 *            is true.
	 */
	private void writeDispatcher(String name, Class<?> type, Encode encoding,
			Object value, boolean listItem) {
		if (type.isAnnotationPresent(Recursable.class))
			writeObject(name, type, value, listItem);
		else if (type.isArray())
			writeArray(name, type, value, listItem);
		else if (List.class.isAssignableFrom(type))
			writeList(name, type, (List<?>) value, listItem);
		else if (Collection.class.isAssignableFrom(type))
			writeCollection(name, type, (Collection<?>) value, listItem);
		else
			writeValue(name, type, encoding, value, listItem);
	}

	private void writeObject(String name, Class<?> type, Object object,
			boolean listItem) {
		openObject(name, listItem);

		// Check if we already have the fields cached for this class.
		Field[] fields = fieldCache.get(type);

		// Check if the fields for this type were already cached.
		if (fields == null) {
			// Get all public, inherited fields for the class.
			fields = type.getFields();

			// Cache the fields for this type incase we parse it again later.
			fieldCache.put(type, fields);
		}

		// Process the object's fields and values.
		for (int i = 0, lastSepIdx = (fields.length - 1); i < fields.length; i++) {
			Field field = fields[i];

			int mods = field.getModifiers();

			// Skip static, transient or synthetic fields.
			if (Modifier.isStatic(mods) || Modifier.isTransient(mods)
					|| field.isSynthetic())
				continue;

			Object fieldValue = null;

			try {
				// Get the field's value.
				fieldValue = field.get(object);
			} catch (Exception e) {
				e.printStackTrace();
			}

			/*
			 * Either our get(value) call failed above or this field has no
			 * value associated with it.
			 * 
			 * Skip fields with null values per typical generation library
			 * behavior in other well-deployed projects (e.g. GSON). This also
			 * makes the following generation code cleaner.
			 */
			if (fieldValue == null)
				continue;

			/*
			 * Dispatch the writing of the field and its value. Also reset the
			 * "inList" state to false, because even if this parent object was
			 * in a list, the field that we are recursing on is in the scope of
			 * an object and no longer directly in the list.
			 */
			writeDispatcher(field.getName(), field.getType(),
					field.getAnnotation(Encode.class), fieldValue, false);

			// Append the list separator (if there is one) between items.
			if (i < lastSepIdx)
				writeListSeparator();
		}

		closeObject(name, listItem);
	}

	private void writeArray(String name, Class<?> type, Object array,
			boolean listItem) {
		openList(name, listItem);

		// Check if we have any work to do on the list.
		if (array != null) {
			int length = Array.getLength(array);

			for (int i = 0, lastSepIdx = length - 1; i < length; i++) {
				Object item = Array.get(array, i);
				Class<?> itemType = item.getClass();

				writeDispatcher(typeToName(itemType), itemType, null, item,
						true);

				if (i < lastSepIdx) {
					writeListSeparator();
					indent(Type.LIST_ITEM, Position.AFTER);
				}
			}
		}

		closeList(name, listItem);
	}

	private void writeList(String name, Class<?> type, List<?> list,
			boolean listItem) {
		openList(name, listItem);

		// Check if we have any work to do on the list.
		if (list != null && !list.isEmpty()) {
			for (int i = 0, size = list.size(), lastSepIdx = size - 1; i < size; i++) {
				Object item = list.get(i);
				Class<?> itemType = item.getClass();

				writeDispatcher(typeToName(itemType), itemType, null, item,
						true);

				if (i < lastSepIdx) {
					writeListSeparator();
					indent(Type.LIST_ITEM, Position.AFTER);
				}
			}
		}

		closeList(name, listItem);
	}

	private void writeCollection(String name, Class<?> type,
			Collection<?> collection, boolean listItem) {
		openList(name, listItem);

		if (collection != null) {
			Iterator<?> elements = collection.iterator();

			while (elements.hasNext()) {
				Object item = elements.next();
				Class<?> itemType = item.getClass();

				writeDispatcher(typeToName(itemType), itemType, null, item,
						true);

				if (elements.hasNext()) {
					writeListSeparator();
					indent(Type.LIST_ITEM, Position.AFTER);
				}
			}
		}

		closeList(name, listItem);
	}

	private void writeValue(String name, Class<?> type, Encode encoding,
			Object value, boolean listItem) {
		/*
		 * For primitive field types, the type returned from field.getType() is
		 * an empty stub Class with a name representing the primitive type and
		 * nothing else.
		 * 
		 * Calling fieldValue.getClass() returns the actual wrapper class of the
		 * primitive type which is needed by writeValue to accurately decide
		 * which write method to call.
		 */
		if (type.isPrimitive() && value != null)
			type = value.getClass();

		indent(Type.VALUE, Position.BEFORE);

		if (Boolean.class.isAssignableFrom(type))
			writeBoolean(name, (Boolean) value, level, listItem);
		else if (Number.class.isAssignableFrom(type))
			writeNumber(name, (Number) value, level, listItem);
		else {
			String text = null;

			if (value instanceof String) {
				text = (String) value;

				if (encoding != null) {
					switch (encoding.value()) {
					case URL:
						try {
							text = URLEncoder.encode(text, "UTF-8");
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;

					case BASE64:
						try {
							text = Base64.encodeBytes(text.getBytes("UTF-8"));
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;

					case URL_SAFE_BASE64:
						try {
							text = Base64.encodeBytes(text.getBytes("UTF-8"),
									Base64.URL_SAFE);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					}
				}
			} else
				text = value.toString();

			writeString(name, text, level, listItem);
		}

		indent(Type.VALUE, Position.AFTER);
	}
}