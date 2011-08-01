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
package com.thebuzzmedia.simple.generator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify if a {@link String} field on a POJO should be
 * encoded automatically (using the given type of encoding) before being written
 * to the generated text output.
 * <p/>
 * All encoding is done using UTF-8.
 * <p/>
 * Annotating non-{@link String} fields with this has no effect.
 * 
 * @author Riyad Kalla (software@thebuzzmedia.com)
 * @since 2.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Encode {
	/**
	 * Enum used to specify the different types of encoding that can be
	 * automatically performed on a <code>String</code> field by Simple
	 * Generator.
	 */
	public enum Type {
		/**
		 * The <code>String</code> will be URL-encoded.
		 */
		URL,
		/**
		 * The <code>String</code> will be Base64-encoded.
		 */
		BASE64,
		/**
		 * The <code>String</code> will be Base64-encoded with URL-safe
		 * characters per <a
		 * href="http://www.faqs.org/rfcs/rfc3548.html">Section 4 of
		 * RFC-3548</a>.
		 */
		URL_SAFE_BASE64
	}

	Type value();
}