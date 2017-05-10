package org.mikado.imc.mobility;

/**
 * Abstract factory for NodeClassLoaders
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class NodeClassLoaderFactory {
	/**
	 * @return a new NodeClassLoader
	 */
	NodeClassLoader createNodeClassLoader() {
		return new NodeClassLoader();
	}
}
