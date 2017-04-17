package com.luckia.biller.core.jpa;

import java.util.UUID;
import java.util.Vector;

import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sequencing.Sequence;

/**
 * Implementacion de {@link Sequence} a traves de un UUID generado por la maquina virtual
 * de java.
 */
@SuppressWarnings("serial")
public class UUIDSequence extends Sequence {

	public final static String UUIDSEQUENCE = "system-uuid";

	public UUIDSequence() {
		super(UUIDSEQUENCE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.persistence.sequencing.Sequence#getGeneratedValue(org.eclipse.
	 * persistence.internal.databaseaccess.Accessor,
	 * org.eclipse.persistence.internal.sessions.AbstractSession, java.lang.String)
	 */
	@Override
	public Object getGeneratedValue(Accessor accessor, AbstractSession writeSession,
			String seqName) {
		return UUID.randomUUID().toString().toUpperCase();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.persistence.sequencing.Sequence#getGeneratedVector(org.eclipse.
	 * persistence.internal.databaseaccess.Accessor,
	 * org.eclipse.persistence.internal.sessions.AbstractSession, java.lang.String, int)
	 */
	@Override
	public Vector<?> getGeneratedVector(Accessor accessor, AbstractSession writeSession,
			String seqName, int size) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.persistence.sequencing.Sequence#onConnect()
	 */
	@Override
	public void onConnect() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.persistence.sequencing.Sequence#onDisconnect()
	 */
	@Override
	public void onDisconnect() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.persistence.sequencing.Sequence#shouldAcquireValueAfterInsert()
	 */
	@Override
	public boolean shouldAcquireValueAfterInsert() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.persistence.sequencing.Sequence#shouldUseTransaction()
	 */
	@Override
	public boolean shouldUseTransaction() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.persistence.sequencing.Sequence#shouldUsePreallocation()
	 */
	@Override
	public boolean shouldUsePreallocation() {
		return false;
	}
}
