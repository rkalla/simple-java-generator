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

/**
 * Interface used to define an "indenter" or a class in charge of generating the
 * <code>char[]</code> representation of an indent given a specific type and
 * position during a generation sequence.
 * 
 * @author Riyad Kalla (software@thebuzzmedia.com)
 * @since 1.1
 */
public interface IIndenter {
	/**
	 * Indent used to represent an empty (0-character) indent that can be
	 * returned by any implementing classes when needed.
	 */
	public static final char[] EMPTY_INDENT = new char[0];

	/**
	 * Enum used to define the different modes of indentation an
	 * {@link IIndenter} can operate in.
	 */
	public enum Mode {
		/**
		 * Used to indicate that indentations are generated using tab ('\t')
		 * characters.
		 */
		TAB('\t'),
		/**
		 * Used to indicate that the indentations are generated using space
		 * (' ') characters.
		 */
		SPACE(' ');

		private char indentCharacter;

		public char getIndentCharacter() {
			return indentCharacter;
		}

		private Mode(char indentCharacter) {
			this.indentCharacter = indentCharacter;
		}
	}

	/**
	 * Enum used to define the type of indentation that is being requested.
	 */
	public enum Type {
		OBJECT_OPEN, OBJECT_CLOSE, LIST_OPEN, LIST_CLOSE, LIST_ITEM, VALUE
	}

	/**
	 * Enum used to define the fine-grained position of the indentation; being
	 * BEFORE or AFTER the element described by the {@link Type}.
	 */
	public enum Position {
		BEFORE, AFTER
	}

	public Mode getMode();

	public void setMode(Mode mode) throws IllegalArgumentException;

	/**
	 * Used to get the current indent multiplier set on this {@link IIndenter}.
	 * <p/>
	 * An indent multiplier is used to multiply the <code>level</code> of the
	 * requested indent. When indenting with tabs this is typically
	 * <code>1</code>, but when indenting with spaces you may visually prefer
	 * 2-spaces per indent (indentMultiple=2), 4-spaces (indentMultiple=4) or
	 * 8-spaces (indentMultiple=8) for example.
	 * 
	 * @return the current indent multiplier set on this {@link IIndenter}.
	 */
	public int getIndentMultiple();

	/**
	 * Used to set an indent multiplier on this {@link IIndenter}. The
	 * multiplier should always be &gt;= 1.
	 * <p/>
	 * An indent multiplier is used to multiply the <code>level</code> of the
	 * requested indent. When indenting with tabs this is typically
	 * <code>1</code>, but when indenting with spaces you may visually prefer
	 * 2-spaces per indent (indentMultiple=2), 4-spaces (indentMultiple=4) or
	 * 8-spaces (indentMultiple=8) for example.
	 * 
	 * @param indentMultiple
	 *            The indent multiplier to use.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>indentMultiplier</code> is &lt; 1.
	 */
	public void setIndentMultiple(int indentMultiple)
			throws IllegalArgumentException;

	public char[] getIndent(Type type, Position position, int level)
			throws IllegalArgumentException;
}