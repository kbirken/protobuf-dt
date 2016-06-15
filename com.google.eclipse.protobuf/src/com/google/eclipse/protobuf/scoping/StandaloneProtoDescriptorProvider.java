package com.google.eclipse.protobuf.scoping;

import java.net.URL;

import org.eclipse.emf.common.util.URI;

import com.google.inject.Singleton;

@Singleton
public class StandaloneProtoDescriptorProvider extends ProtoDescriptorProvider {

	/**
	 * Get descriptor info for "descriptor.proto" when in standalone mode.<p/>
	 * 
	 * In standalone, we do not have the Eclipse IDE workspace and thus platform:
	 * URL will not work. We are using the classloader to detect the file and
	 * then create a FileURI instead ("file:/...").
	 */
	@Override
	protected ProtoDescriptorInfo defaultDescriptorInfo() {
		URL url = getClass().getClassLoader().getResource("descriptor.proto");
		System.out.println("StandaloneProtoDescriptorProvider - URL      : " + url);
		
		URI location = URI.createURI(url.toString());
		System.out.println("StandaloneProtoDescriptorProvider - URI      : " + location);

		System.out.println("StandaloneProtoDescriptorProvider - ext form : " + url.toExternalForm());

		return new ProtoDescriptorInfo(ProtoDescriptorProvider.PROTO_DESCRIPTOR_URI, location);
	}

}
