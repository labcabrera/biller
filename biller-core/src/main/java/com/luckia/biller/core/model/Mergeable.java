package com.luckia.biller.core.model;

/**
 * Interfaz para generaliza la actualizacion de entidades. Esto nos facilita el modo en el que actualizaremos las entidades del modelo y nos
 * permite definir para cada entidad el modo en el que se actualizan los valores. Por ejemplo:
 * 
 * <pre>
 * Mergeable objetoModificado = ...
 * Mergeable entidad = entityManager.find(MyClass.class, id);
 * entidad.merge(objetoModificado);
 * entityManager.merge(entidad);
 * </pre>
 * 
 * De este modo no hacemos directamente <code>entityManager.merge(objetoModificado)</code> ya que no siempre tenemos el control sobre quien
 * ha realizado las modificaciones, ni tampoco tenemos que estar copiando propiedad por propiedad para actualizar la entidad de base de
 * datos.
 * 
 * @param <T>
 */
public interface Mergeable<T> {

	/**
	 * Actualiza los datos de la entidad a partir de otro objeto.
	 * 
	 * @param entity
	 */
	void merge(T entity);

}
