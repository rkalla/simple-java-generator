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

import java.io.OutputStream;

import com.thebuzzmedia.common.io.IInput;

/**
 * Interface used to describe a class that is capable of generating an
 * arbitrary, textual representation of an {@link Object} via reflection.
 * 
 * @author Riyad Kalla (software@thebuzzmedia.com)
 * @since 1.1
 */
public interface IGenerator {
	/**
	 * Used to reset the state of the generator. This should be called
	 * automatically by {@link #generate(Object)} implementations right before a
	 * new generation is performed.
	 */
	public void reset();

	/**
	 * Used to get the indenter currently set on this generator.
	 * <p/>
	 * The {@link IIndenter} is used during generation to create customizable
	 * indentations in the content as it is generated.
	 * 
	 * @return the indenter currently set on this generator.
	 */
	public IIndenter getIndenter();

	/**
	 * Used to set the indenter for this generator to use during generation.
	 * 
	 * @param indenter
	 *            The indenter that this generator will use.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>indenter</code> is <code>null</code>.
	 */
	public void setIndenter(IIndenter indenter) throws IllegalArgumentException;

	/**
	 * Used to generate a textual representation of the given object using
	 * reflection.
	 * <p/>
	 * This method returns an {@link IInput} that allows direct access to the
	 * underlying <code>char[]</code> buffer built up during the generation as
	 * well as index and length pointers. This allows the caller a much more
	 * optimized access to the generated value as well as avoiding the creation
	 * of a large <code>String</code> if it is unneeded (e.g. if the character
	 * data just needs to be streamed back to the user via an
	 * {@link OutputStream}).
	 * 
	 * @param object
	 *            The object to be reflected on during generation.
	 * 
	 * @return a light weight wrapper around the underlying <code>char[]</code>
	 *         buffer containing the generated textual representation of the
	 *         given object.
	 */
	public IInput<char[], char[]> generate(Object object);
}