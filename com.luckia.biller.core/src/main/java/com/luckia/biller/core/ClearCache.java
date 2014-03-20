package com.luckia.biller.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Dado que la aplicación trabaja con varios {@link javax.persistence.EntityManager} a traves de un {@link java.lang.ThreadLocal} es
 * necesario controlar las entidades que han sido modificadas. Un ejemplo es el siguiente:
 * <ul>
 * <li>Se recibe una peticion sobre un objeto desde el thread A</li>
 * <li>Se genera un EntityManager asociado el Thread A</li>
 * <li>Desde el Thread B se hace un merge del objeto</li>
 * <li>Al recibir una nueva peticion sobre el objeto desde el thread A, al tenerlo cacheado en la sesion devolvera la instancia que tenia
 * antes de ser modificada de forma errónea.
 * </ul>
 * Para evitar este problema forzamos a hacer un <code>entityManager.clear()</code> para asegurarnos de que no estamos devolviendo un
 * resultado erroneamente cacheado en la unidad de trabajo del entityManager.
 * 
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ClearCache {

}
