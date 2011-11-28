package com.googlecode.objectify.impl.translate;

import com.googlecode.objectify.impl.Path;
import com.googlecode.objectify.impl.node.EntityNode;

/**
 * <p>A translator knows how convert between POJO objects and the EntityNode tree structure
 * that Objectify can persist.  In principle, this is similar to how a JSON object mapper works.
 * Each node in the object graph corresponds to a node in the EntityNode graph.</p> 
 * 
 * <p>Translators are composed of other translators; through a chain of these a whole entity
 * object is assembled or disassembled.</p>
 * 
 * @author Jeff Schnitzer <jeff@infohazard.org>
 */
public interface Translator<T>
{
	/**
	 * Loads the content of the specified node, returning the pojo equivalent.
	 * 
	 * @param node is the part of the entity tree we are transforming.
	 * @param ctx holds state information during an entity load.  
	 * @return an assembled pojo corresponding to the node subtree; if null is returned, that is the real value!
	 */
	T load(EntityNode node, LoadContext ctx);
	
	/**
	 * Translates the pojo into an EntityNode format.  Note that all stored values (even terminal properties)
	 * are associated with a node.
	 * 
	 * @param pojo is an object from the pojo entity graph; possibly the whole graph
	 * @param path is the path that the entitynode will be created with - the path to the pojo
	 * @param index is whether the instruction so far is to index or not index property values
	 * @return an EntityNode relevant to the pojo
	 * 
	 * @throws SkipException if this subtree should not be saved.
	 */
	EntityNode save(T pojo, Path path, boolean index, SaveContext ctx) throws SkipException;
}
