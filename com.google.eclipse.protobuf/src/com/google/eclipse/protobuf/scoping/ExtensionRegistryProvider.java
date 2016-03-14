/*
 * Copyright (c) 2011 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.scoping;

import java.io.InputStream;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.Platform;

import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Provider of instances of <code>{@link IExtensionRegistry}</code>.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
@Singleton public class ExtensionRegistryProvider implements Provider<IExtensionRegistry> {

	@Override public IExtensionRegistry get() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		if (registry!=null) {
			return registry;
		}
		
		// couldn't get ExtensionRegistry, we are running standalone (without IDE)
		return null;
	}
}
