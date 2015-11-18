package com.luckia.biller.core.jpa;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.Session;

/**
 * Implementacion de {@link SessionCustomizer} que registra el generador de UUIDs generados por el sistema.
 */
public class CustomSessionCustomizer implements SessionCustomizer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.persistence.config.SessionCustomizer#customize(org.eclipse.persistence.sessions.Session)
	 */
	@Override
	public void customize(Session session) throws Exception {
		UUIDSequence sequence = new UUIDSequence();
		session.getLogin().addSequence(sequence);
	}
}
