package com.nhl.bootique.factory;

/**
 * An object that has access to a tree of factories configuration data, and acts
 * as a factory of custom factories that are initialized from this
 * configuration.
 */
public interface FactoryConfigurationService {

	/**
	 * Creates and returns a custom factory instance with its state initialized
	 * based on internal configuration. "prefix" argument defines
	 * sub-configuration location in the config tree.
	 * 
	 * 
	 * @param type
	 *            a type of factory to create.
	 * @param prefix
	 *            defines sub-configuration location in the config tree. Use
	 *            empty string to access root config.
	 * @return a fully initialized factory of tghe specified type.
	 * 
	 */
	<T> T factory(Class<T> type, String prefix);
}
