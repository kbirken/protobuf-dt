/*
 * Copyright (c) 2011 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.scoping;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Collections.unmodifiableCollection;
import static org.eclipse.xtext.util.Strings.isEmpty;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.eclipse.protobuf.model.util.INodes;
import com.google.inject.Inject;
import com.google.inject.Singleton;


// define our own @Nullable annotation
@Target({ FIELD })
@Retention(RUNTIME)
@interface Nullable { }

/**
 * Provider of <code>{@link ProtoDescriptor}</code>s.
 *
 * @author Alex Ruiz
 */
@Singleton public class ProtoDescriptorProvider {
  public static final String PROTO_DESCRIPTOR_URI = "google/protobuf/descriptor.proto";
  
  private static final String EXTENSION_ID = "com.google.eclipse.protobuf.descriptorSource";

  @Inject private XtextResourceSet resourceSet;
  @Inject private INodes nodes;
  @Inject @Nullable private IExtensionRegistry registry;

  private Map<String, URI> descriptorInfos;
  private Map<String, ProtoDescriptor> descriptors;
  private String primaryImportUri;

  private final Object lock = new Object();

  public ProtoDescriptor primaryDescriptor() {
    ensureProtoDescriptorsAreCreated();
    return descriptors.get(primaryImportUri);
  }

  public ProtoDescriptor descriptor(String importUri) {
    ensureProtoDescriptorsAreCreated();
    ProtoDescriptor protoDescriptor = descriptors.get(importUri);
    if (protoDescriptor != null) {
      return protoDescriptor;
    }
    // URI could have been resolved
    URI uri = URI.createURI(importUri);
    for (Entry<String, URI> info : descriptorInfos.entrySet()) {
      if (info.getValue().equals(uri)) {
        return descriptor(info.getKey());
      }
    }
    return null;
  }

  private void ensureProtoDescriptorsAreCreated() {
    synchronized (lock) {
      if (descriptors == null) {
        descriptors = newLinkedHashMap();
        ensureProtoDescriptorInfosAreCreated();
        for (Entry<String, URI> entry : descriptorInfos.entrySet()) {
          String importUri = entry.getKey();
          ProtoDescriptor descriptor = new ProtoDescriptor(importUri, entry.getValue(), resourceSet, nodes);
          descriptors.put(importUri, descriptor);
        }
      }
    }
  }

  public Collection<URI> allDescriptorLocations() {
    ensureProtoDescriptorInfosAreCreated();
    return unmodifiableCollection(descriptorInfos.values());
  }

  public URI primaryDescriptorLocation() {
    return descriptorLocation(primaryImportUri);
  }

  public URI descriptorLocation(String importUri) {
    ensureProtoDescriptorInfosAreCreated();
    return descriptorInfos.get(importUri);
  }

  private void ensureProtoDescriptorInfosAreCreated() {
    synchronized (lock) {
      if (descriptorInfos == null) {
        descriptorInfos = newLinkedHashMap();
        add(defaultDescriptorInfo());
        add(descriptorInfoFromExtensionPoint());
      }
    }
  }

  protected ProtoDescriptorInfo defaultDescriptorInfo() {
    URI location = URI.createURI("platform:/plugin/com.google.eclipse.protobuf/descriptor.proto");
    return new ProtoDescriptorInfo(PROTO_DESCRIPTOR_URI, location);
  }

  private ProtoDescriptorInfo descriptorInfoFromExtensionPoint() {
	// registry will be null if not running in the Eclipse IDE
	if (registry == null) {
		return null;
	}
    IConfigurationElement[] config = registry.getConfigurationElementsFor(EXTENSION_ID);
    if (config == null) {
      return null;
    }
    for (IConfigurationElement e : config) {
      ProtoDescriptorInfo info = descriptorInfo(e);
      if (info != null) {
        return info;
      }
    }
    return null;
  }

  private static ProtoDescriptorInfo descriptorInfo(IConfigurationElement e) {
    String importUri = e.getAttribute("importUri");
    if (isEmpty(importUri)) {
      return null;
    }
    URI location = descriptorLocation(e);
    if (location == null) {
      return null;
    }
    return new ProtoDescriptorInfo(importUri, location);
  }

  private static URI descriptorLocation(IConfigurationElement e) {
    String path = e.getAttribute("path");
    if (isEmpty(path)) {
      return null;
    }
    StringBuilder uri = new StringBuilder();
    uri.append("platform:/plugin/").append(e.getContributor().getName()).append("/").append(path);
    return URI.createURI(uri.toString());
  }

  private void add(ProtoDescriptorInfo descriptorInfo) {
    if (descriptorInfo == null) {
      return;
    }
    primaryImportUri = descriptorInfo.importUri;
    descriptorInfos.put(primaryImportUri, descriptorInfo.location);
  }

  protected static class ProtoDescriptorInfo {
    final String importUri;
    final URI location;

    ProtoDescriptorInfo(String importUri, URI location) {
      this.importUri = importUri;
      this.location = location;
    }
  }
}
