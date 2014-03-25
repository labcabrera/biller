/*******************************************************************************
 * Copyright (c) 2004, 2012 Kotasoft S.L.
 * All rights reserved. This program and the accompanying materials
 * may only be used prior written consent of Kotasoft S.L.
 ******************************************************************************/
package com.luckia.biller.core.model;

import java.util.Date;

public interface Auditable {

	Date getCreated();

	Date getDeleted();

	Date getModified();

	User getModifiedBy();

	void setCreated(Date value);

	void setDeleted(Date value);

	void setModified(Date value);

	void setModifiedBy(User value);

}
