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

import com.thebuzzmedia.simple.generator.AbstractGenerator;
import com.thebuzzmedia.simple.generator.IGenerator;

/**
 * Marker annotation used by implementations of {@link AbstractGenerator} to
 * determine if a simple POJO object should be recursed on and its member values
 * included in the generation process.
 * <p/>
 * As "un-dynamic" as using a marker annotation seems in the world of
 * reflect-everything, this solves the problem of a POJO containing object types
 * that Simple Generator doesn't know how to translate, resulting in empty,
 * unknown or unwanted values in the generated content unless additional/complex
 * checking logic is added by the implementor in their own custom
 * {@link IGenerator} implementation.
 * <p/>
 * More specifically, lets say your POJO carried a reference to a significantly
 * complex object from another framework; if Simple Generator relied blindly on
 * reflection, it might automatically include output from those member
 * variables, recursing into them outputting each of their fields in the
 * generated output.
 * <p/>
 * By utilizing an explicit annotation, you tell Simple Generator which objects
 * it should reflect into and process and which ones it should just toString and
 * leave alone.
 * <p/>
 * Given that the charge of this project is <strong>simple</strong> generation,
 * it was decided that an "<em>excluded unless included</em>" approach to
 * reflection-based generation would be taken.
 * <p/>
 * A nice side effect of this is better performance during generation.
 * 
 * @author Riyad Kalla (software@thebuzzmedia.com)
 * @since 2.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Recursable {
	// no-op
}