/* ====================================================================
 *
 * The ObjectStyle Group Software License, Version 1.0
 *
 * Copyright (c) 2002 The ObjectStyle Group
 * and individual authors of the software.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        ObjectStyle Group (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "ObjectStyle Group" and "Cayenne"
 *    must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact andrus@objectstyle.org.
 *
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    nor may "ObjectStyle" appear in their names without prior written
 *    permission of the ObjectStyle Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE OBJECTSTYLE GROUP OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the ObjectStyle Group.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 *
 */
package org.objectstyle.woproject.ant;

import java.io.File;
import java.util.ArrayList;

import org.apache.tools.ant.BuildException;

/**
 * Ant task to build WebObjects application. For detailed instructions go to the
 * <a href="../../../../../ant/woapplication.html">manual page</a> .
 *
 *
 * @ant.task category="packaging"
 * 
 * @author Emily Bache
 * @author Andrei Adamchik
 */
public class WOApplication extends WOTask {
	String[] frameworkNames =
		new String[] {
			"JavaWebObjects",
			"JavaWOExtensions",
			"JavaEOAccess",
			"JavaEOControl",
			"JavaFoundation",
			"JavaJDBCAdaptor" };

    protected ArrayList frameworkSets = new ArrayList();
	protected boolean stdFrameworks = true;

	/** 
	 * Creates a path to framework JAR file. The following
	 * assumptions are made (may not always be true): framework 
	 * is located in "Library/Frameworks" subdirectory, 
	 * JAR file is named after the framework, in lowercase.
	 */
	public static String frameworkJar(String name) {
		StringBuffer buf = new StringBuffer("Library/Frameworks/");
		buf.append(name);
		if (!name.endsWith(".framework")) {
			buf.append(".framework");
		}
		buf.append("/Resources/Java/").append(name.toLowerCase()).append(
			".jar");

		return buf.toString();
	}

	/** 
	 * Runs WOApplication task. Main worker method that would validate
	 * all task settings and create a WOApplication.
	 */
	public void execute() throws BuildException {
		validateAttributes();
		createDirectories();
		if (hasClasses()) {
			jarClasses();
		}
		if (hasResources()) {
			copyResources();
		}
		if (hasWs()) {
			copyWsresources();
		}

		// create all needed scripts
		new AppFormat(this).processTemplates();
	}

	/** 
	 * Sets a flag indicating that standard frameworks,
	 * namely JavaWebObjects, JavaWOExtensions, JavaEOAccess, JavaEOControl, 
	 * JavaFoundation, JavaJDBCAdaptor should be automatically 
	 * referenced in deployed application.
	 */
	public void setStdFrameworks(boolean flag) {
		stdFrameworks = flag;
	}

	/**
	 * Returns location where WOApplication is being built up. 
	 * For WebObjects applications this is a <code>.woa</code> directory.
	 */
	protected File taskDir() {
		return getProject().resolveFile(
			destDir + File.separator + name + ".woa");
	}

	protected File contentsDir() {
		return new File(taskDir(), "Contents");
	}

	protected File resourcesDir() {
		return new File(contentsDir(), "Resources");
	}

	protected File wsresourcesDir() {
		return new File(contentsDir(), "WebServerResources");
	}
	
	/**
     * Create a nested FrameworkSet.
     */
    public FrameworkSet createFrameworks() {
        FrameworkSet frameSet = new FrameworkSet();
        frameworkSets.add(frameSet);
        return frameSet;
    }
}