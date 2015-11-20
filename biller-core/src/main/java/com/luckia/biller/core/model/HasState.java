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
