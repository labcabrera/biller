package com.luckia.biller.core.lis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Anotacion para indicar que un un EntityManager accede a la base de datos de LIS en
 * lugar de la base de datos de la aplicacion.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@BindingAnnotation
public @interface Lis {

}
