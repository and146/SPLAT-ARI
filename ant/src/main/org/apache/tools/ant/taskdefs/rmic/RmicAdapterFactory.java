/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.tools.ant.taskdefs.rmic;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.ClasspathUtils;

import java.util.Locale;


/**
 * Creates the necessary rmic adapter, given basic criteria.
 *
 * @since 1.4
 */
public final class RmicAdapterFactory {
    /** The error message to be used when the compiler cannot be found. */
    public static final String ERROR_UNKNOWN_COMPILER = "Class not found: ";

    /** The error message to be used when the class is not an rmic adapter. */
    public static final String ERROR_NOT_RMIC_ADAPTER = "Class of unexpected Type: ";

    /** If the compiler has this name use a default compiler. */
    public static final String DEFAULT_COMPILER = "default";

    /** This is a singleton -- can't create instances!! */
    private RmicAdapterFactory() {
    }

    /**
     * Based on the parameter passed in, this method creates the necessary
     * factory desired.
     *
     * <p>The current mapping for rmic names are as follows:</p>
     * <ul><li>sun = SUN's rmic
     * <li>kaffe = Kaffe's rmic
     * <li><i>a fully quallified classname</i> = the name of a rmic
     * adapter
     * <li>weblogic = weblogic compiler
     * <li>forking = Sun's RMIC by forking a new JVM
     * </ul>
     *
     * @param rmicType either the name of the desired rmic, or the
     * full classname of the rmic's adapter.
     * @param task a task to log through.
     * @return the compiler adapter
     * @throws BuildException if the rmic type could not be resolved into
     * a rmic adapter.
     */
    public static RmicAdapter getRmic(String rmicType, Task task)
        throws BuildException {
        //convert to lower case in the English locale,
        String compiler = rmicType.toLowerCase(Locale.ENGLISH);

        //handle default specially by choosing the sun or kaffe compiler
        if (DEFAULT_COMPILER.equals(compiler) || compiler.length() == 0) {
            compiler = KaffeRmic.isAvailable()
                ? KaffeRmic.COMPILER_NAME
                : SunRmic.COMPILER_NAME;
        }
        if (SunRmic.COMPILER_NAME.equals(compiler)) {
            return new SunRmic();
        } else if (KaffeRmic.COMPILER_NAME.equals(compiler)) {
            return new KaffeRmic();
        } else if (WLRmic.COMPILER_NAME.equals(compiler)) {
            return new WLRmic();
        } else if (ForkingSunRmic.COMPILER_NAME.equals(compiler)) {
            return new ForkingSunRmic();
        } else if (XNewRmic.COMPILER_NAME.equals(compiler)) {
            return new XNewRmic();
        }
        //no match? ask for the non-lower-cased type
        return resolveClassName(rmicType);
    }

    /**
     * Tries to resolve the given classname into a rmic adapter.
     * Throws a fit if it can't.
     *
     * @param className The fully qualified classname to be created.
     * @throws BuildException This is the fit that is thrown if className
     * isn't an instance of RmicAdapter.
     */
    private static RmicAdapter resolveClassName(String className)
            throws BuildException {
        return (RmicAdapter) ClasspathUtils.newInstance(className,
                RmicAdapterFactory.class.getClassLoader(), RmicAdapter.class);
    }
}
