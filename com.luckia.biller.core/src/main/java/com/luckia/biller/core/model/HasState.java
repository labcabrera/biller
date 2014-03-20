/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

/**
 * Interfaz que generaliza las entidades que tienen diferentes estados. Un ejemplo serian los estados de una factura: borrador, confirmada,
 * enviada, cancelada, etc.
 * 
 * @see StateDefinition
 */
public interface HasState {

	State getCurrentState();

	void setCurrentState(State value);

}
