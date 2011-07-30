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
 * Class used to provide a universal "compact" indenter that only ever returns
 * {@link IIndenter#EMPTY_INDENT} for every call to
 * {@link #getIndent(Type, Position, int)}.
 * <p/>
 * This indenter can be used to generate a compact representation of any file
 * format whose syntax doesn't include particular formatting.
 * 
 * @author Riyad Kalla (software@thebuzzmedia.com)
 * @since 1.1
 */
public class CompactIndenter extends AbstractIndenter {
	/**
	 * Singleton reference to this indenter.
	 * <p/>
	 * Since the design of {@link IIndenter}s is stateless, there is only 1
	 * instance of this class ever needed.
	 */
	public static final IIndenter INSTANCE = new CompactIndenter();

	/**
	 * Overridden to avoid the use of the cache all together and just always
	 * return {@link IIndenter#EMPTY_INDENT}.
	 */
	@Override
	public char[] getIndent(Type type, Position position, int level)
			throws IllegalArgumentException {
		return EMPTY_INDENT;
	}

	/**
	 * Overridden to do the same as {@link #getIndent(Type, Position, int)} even
	 * though this method will never be called, because the overridden
	 * {@link #getIndent(Type, Position, int)} in this class will never delegate
	 * to it.
	 */
	protected char[] getIndentImpl(Type type, Position position, int level)
			throws IllegalArgumentException {
		return EMPTY_INDENT;
	}
}