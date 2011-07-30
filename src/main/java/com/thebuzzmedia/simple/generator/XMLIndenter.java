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
 * Class used to implement the default indenter for the XML file format.
 * 
 * @author Riyad Kalla (software@thebuzzmedia.com)
 * @since 1.1
 */
public class XMLIndenter extends AbstractIndenter {
	/**
	 * Singleton reference to this indenter.
	 * <p/>
	 * Since the design of {@link IIndenter}s is stateless, there is only 1
	 * instance of this class ever needed.
	 */
	public static final IIndenter INSTANCE = new XMLIndenter();
	
	public XMLIndenter() {
		super(DEFAULT_MODE);
	}

	public XMLIndenter(Mode mode) throws IllegalArgumentException {
		super(mode, DEFAULT_INDENT_MULTIPLE);
	}

	public XMLIndenter(Mode mode, int indentMultiple)
			throws IllegalArgumentException {
		super(mode, indentMultiple);
	}

	protected char[] getIndentImpl(Type type, Position position, int level) {
		char[] indent = null;

		switch (type) {
		case OBJECT_OPEN:
			switch (position) {
			case BEFORE:
				// No indentation for root element, sub objects get it.
				if (level > 0)
					indent = createIndent(level, true);
				else
					indent = EMPTY_INDENT;
				break;

			case AFTER:
				indent = EMPTY_INDENT;
				break;
			}
			break;

		case OBJECT_CLOSE:
			switch (position) {
			case BEFORE:
				indent = createIndent(level, true);
				break;

			case AFTER:
				indent = EMPTY_INDENT;
				break;
			}
			break;

		case LIST_OPEN:
			switch (position) {
			case BEFORE:
				indent = createIndent(level, true);
				break;

			case AFTER:
				indent = EMPTY_INDENT;
				break;
			}
			break;

		case LIST_CLOSE:
			switch (position) {
			case BEFORE:
				indent = createIndent(level, true);
				break;

			case AFTER:
				indent = EMPTY_INDENT;
				break;
			}
			break;

		case LIST_ITEM:
			switch (position) {
			case BEFORE:
				indent = EMPTY_INDENT;
				break;

			case AFTER:
				indent = EMPTY_INDENT;
				break;
			}
			break;

		case VALUE:
			switch (position) {
			case BEFORE:
				indent = createIndent(level, true);
				break;

			case AFTER:
				indent = EMPTY_INDENT;
				break;
			}
			break;
		}

		return indent;
	}
}