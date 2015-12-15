package com.luckia.biller.core.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.luckia.biller.core.model.UserActivity;
import com.luckia.biller.core.model.UserActivityType;

/**
 * Anotation used to register user activity data.
 * 
 * @see UserActivity
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RegisterActivity {

	UserActivityType type();

}
