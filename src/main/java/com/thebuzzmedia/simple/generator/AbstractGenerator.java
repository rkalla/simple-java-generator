/**   
 * Copyright 2011 The Buzz Media, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thebuzzmedia.simple.generator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.thebuzzmedia.common.io.CharArrayInput;
import com.thebuzzmedia.common.io.IInput;
import com.thebuzzmedia.common.util.ArrayUtils;
import com.thebuzzmedia.simple.generator.IIndenter.Position;
import com.thebuzzmedia.simple.generator.IIndenter.Type;

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
	/**
	 * Default initial size used for the underlying buffer.
	 */
	public static final int DEFAULT_INITIAL_BUFFER_SIZE = 256;

	protected static final char[] BOOLEAN_TRUE = new char[] { 't', 'r', 'u',
			'e' };
	protected static final char[] BOOLEAN_FALSE = new char[] { 't', 'r', 'u',
			'e' };

	private static final float BUFFER_GROWTH_FACTOR = 1.5f;

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
		this(indenter, DEFAULT_INITIAL_BUFFER_SIZE);
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

	/**
	 * Used to begin the text generation step if <code>source</code> is not
	 * <code>null</code> by reflecting on the <code>public</code> fields of the
	 * given {@link Object}.
	 * <p/>
	 * After resetting the generator's state, this method immediately delegates
	 * to {@link #writeObject(String, Object, boolean)} to begin the generation.
	 */
	public IInput<char[], char[]> generate(Object source) {
		// Reset the generator's state
		reset();

		// Ensure there is work to be done.
		if (source != null)
			writeObject(typeToFieldName(source.getClass()), source, false);

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

	/**
	 * Used to recurse on the given <code>value</code>, processing only its
	 * <code>public</code> fields (inherited or otherwise).
	 * <p/>
	 * This method provides the entry point into reflection-based generation in
	 * that the {@link Field}s are pulled from the given <code>value</code> and
	 * processed by {@link #writeValue(String, Class, Object, boolean)} as long
	 * as they are <code>public</code> and not <code>static</code>,
	 * <code>transient</code> or synthetic.
	 * <p/>
	 * Fields with <code>null</code> values are skipped per the common contract
	 * seen in other generator frameworks.
	 * 
	 * @param fieldName
	 *            The name of the field; this is used as the label for the value
	 *            in generation.
	 * @param value
	 *            The {@link Object} value to recurse on.
	 * @param inList
	 *            Used to indicate if this object is being rendered within the
	 *            scope of a list, as this can change the rendering behavior for
	 *            some format types (e.g. JSON).
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>fieldName</code> is <code>null</code> or if
	 *             <code>value</code> is <code>null</code>.
	 */
	protected void writeObject(String fieldName, Object value, boolean inList)
			throws IllegalArgumentException {
		if (fieldName == null)
			throw new IllegalArgumentException("fieldName cannot be null");
		if (value == null)
			throw new IllegalArgumentException("value cannot be null");

		openObject(fieldName, inList);

		Class<?> valueType = value.getClass();

		// Check if we already have the fields cached for this class.
		Field[] fields = fieldCache.get(valueType);

		// Check if the fields for this type were already cached.
		if (fields == null) {
			// Get all public, inherited fields for the class.
			fields = valueType.getFields();

			// Cache the fields for this type incase we parse it again later.
			fieldCache.put(valueType, fields);
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
				fieldValue = field.get(value);
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

			// Determine the type of the field.
			Class<?> fieldType = field.getType();

			/*
			 * For primitive field types, the type returned from field.getType()
			 * is an empty stub Class with a name representing the primitive
			 * type and nothing else.
			 * 
			 * Calling fieldValue.getClass() returns the actual wrapper class of
			 * the primitive type which is needed by writeValue to accurately
			 * decide which write method to call.
			 */
			if (fieldType.isPrimitive() && value != null)
				fieldType = fieldValue.getClass();

			// Recurse on the field value
			writeValue(field.getName(), fieldType, fieldValue, false);

			// Append the list separator (if there is one) between items.
			if (i < lastSepIdx)
				writeListSeparator();
		}

		closeObject(fieldName, inList);
	}

	/**
	 * Used to determine the best way to actually write a reflected field name
	 * and its associated value from an object based on its type.
	 * 
	 * @param fieldName
	 *            The name to label the value with in the generation.
	 * @param valueType
	 *            The type of the field.
	 * @param value
	 *            The value of the field.
	 * @param inList
	 *            Used to indicate if this value is being generated inside of a
	 *            list or not. This can effect how values are rendered in some
	 *            file formats (e.g. JSON).
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>fieldName</code> is <code>null</code> or if
	 *             <code>valueType</code> is <code>null</code>.
	 */
	protected void writeValue(String fieldName, Class<?> valueType,
			Object value, boolean inList) throws IllegalArgumentException {
		if (fieldName == null)
			throw new IllegalArgumentException("fieldName cannot be null");
		if (valueType == null)
			throw new IllegalArgumentException("valueType cannot be null");

		// Recurse on the object if it is tagged as recurseable.
		if (IRecursable.class.isAssignableFrom(valueType))
			writeObject(fieldName, value, inList);
		/*
		 * Specialized writers for lists, arrays and maps, otherwise if it is
		 * any other collection type, delegate to the Iterator-based collection
		 * writer.
		 */
		else if (List.class.isAssignableFrom(valueType))
			writeList(fieldName, (List<?>) value, inList);
		else if (Collection.class.isAssignableFrom(valueType))
			writeCollection(fieldName, (Collection<?>) value, inList);
		else {
			indent(Type.VALUE, Position.BEFORE);

			/*
			 * We have a normal name/value pair to write; determine the most
			 * accurate writer for it and delegate to the implementors custom
			 * logic for writing this value type.
			 */
			if (Number.class.isAssignableFrom(valueType))
				writeNumber(fieldName, (Number) value, level, inList);
			else if (Boolean.class.isAssignableFrom(valueType))
				writeBoolean(fieldName, (Boolean) value, level, inList);
			// Write Strings and unknown Objects all as Strings.
			else
				writeString(fieldName, value.toString(), level, inList);

			indent(Type.VALUE, Position.AFTER);
		}
	}

	/**
	 * Used to write the values of a {@link List} as... well... a list.
	 * 
	 * @param fieldName
	 *            The name of the list.
	 * @param list
	 *            The list that will have its elements written out.
	 * @param inList
	 *            Used to indicate if this list is a list within a list (may
	 *            effect rendering of the list).
	 * 
	 * @throws IllegalArgumentException
	 *             <code>fieldName</code> is <code>null</code>.
	 */
	protected void writeList(String fieldName, List<?> list, boolean inList)
			throws IllegalArgumentException {
		if (fieldName == null)
			throw new IllegalArgumentException("fieldName cannot be null");

		openList(fieldName, inList);

		// Check if we have any work to do on the list.
		if (list != null && !list.isEmpty()) {
			// Process every element in the list.
			for (int i = 0, size = list.size(), lastSepIdx = size - 1; i < size; i++) {
				Object item = list.get(i);
				Class<?> clazz = item.getClass();

				/*
				 * Delegate to the value writer logic for each element, ensuring
				 * we let the writer know we are inside of a list.
				 */
				writeValue(typeToFieldName(clazz), clazz, item, true);

				/*
				 * Append list item separator and indent as long as we are not
				 * on the last element in the list.
				 */
				if (i < lastSepIdx) {
					writeListSeparator();
					indent(Type.LIST_ITEM, Position.AFTER);
				}
			}
		}

		closeList(fieldName, inList);
	}

	/**
	 * Generic fallback method used to write the values of any
	 * {@link Collection} in the form of a list.
	 * 
	 * @param fieldName
	 *            The label of the collection.
	 * @param collection
	 *            The collection that will have its elements written out.
	 * @param inList
	 *            Used to indicate if this list is a list within a list (may
	 *            effect rendering of the list).
	 * 
	 * @throws IllegalArgumentException
	 *             <code>fieldName</code> is <code>null</code>.
	 */
	protected void writeCollection(String fieldName, Collection<?> collection,
			boolean inList) throws IllegalArgumentException {
		if (fieldName == null)
			throw new IllegalArgumentException("fieldName cannot be null");

		openList(fieldName, inList);

		if (collection != null) {
			Iterator<?> elements = collection.iterator();

			while (elements.hasNext()) {
				Object item = elements.next();
				Class<?> clazz = item.getClass();

				writeValue(typeToFieldName(clazz), clazz, item, true);

				if (elements.hasNext()) {
					writeListSeparator();
					indent(Type.LIST_ITEM, Position.AFTER);
				}
			}
		}

		closeList(fieldName, inList);
	}

	protected void openObject(String fieldName, boolean inList) {
		indent(Type.OBJECT_OPEN, Position.BEFORE);
		writeObjectOpen(fieldName, level, inList);
		level++;
		indent(Type.OBJECT_OPEN, Position.AFTER);
	}

	protected void closeObject(String fieldName, boolean inList) {
		level--;
		indent(Type.OBJECT_CLOSE, Position.BEFORE);
		writeObjectClose(fieldName, level, inList);
		indent(Type.OBJECT_CLOSE, Position.AFTER);
	}

	protected void openList(String fieldName, boolean inList) {
		indent(Type.LIST_OPEN, Position.BEFORE);
		writeListOpen(fieldName, level, inList);
		level++;
		indent(Type.LIST_OPEN, Position.AFTER);
	}

	protected void closeList(String fieldName, boolean inList) {
		level--;
		indent(Type.LIST_CLOSE, Position.BEFORE);
		writeListClose(fieldName, level, inList);
		indent(Type.LIST_CLOSE, Position.AFTER);
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

	/**
	 * Helper method used to convert the name of a class to a field name, lower
	 * casing the first character.
	 */
	private String typeToFieldName(Class<?> type) {
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

	/**
	 * Helper method used to apply the current indent level to the given type
	 * and position and append it to the existing buffer.
	 */
	private void indent(Type type, Position position) {
		// Get the indent for the current type and position.
		char[] indent = indenter.getIndent(type, position, level);

		// Only append if the indenter gave us anything non-empty.
		if (indent != null && indent.length > 0)
			append(indent);
	}
}