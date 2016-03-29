/*
 * Copyright (c) 2011 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf;

import com.google.eclipse.protobuf.scoping.IFileUriResolver;
import com.google.eclipse.protobuf.scoping.StandaloneFileUriResolver;

/**
 * Use this class to register components to be used when in standalone mode, i.e.,
 * for a command-line tool or JUnit test.
 */
public class ProtobufStandaloneRuntimeModule extends ProtobufRuntimeModule {

	/**
	 * Bind a different FileUriResolver in standalone mode, we cannot use
	 * the default implementation from the UiModule because it uses various
	 * Eclipse workbench concepts.
	 */
	public Class<? extends IFileUriResolver> bindFileUriResolver() {
		return StandaloneFileUriResolver.class;
	}
}
