package com.luckia.biller.core.model.lis;

import java.io.Serializable;
import java.util.Date;

/**
 * Representa la clave compuesta de la entidad {@link LisTerminalRecord}
 */
@SuppressWarnings("serial")
public class LisTerminalRecordPK implements Serializable {

	private String terminalCode;
	private Date date;

	public String getTerminalCode() {
		return terminalCode;
	}

	public void setTerminalCode(String terminalCode) {
		this.terminalCode = terminalCode;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((terminalCode == null) ? 0 : terminalCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LisTerminalRecordPK other = (LisTerminalRecordPK) obj;
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		}
		else if (!date.equals(other.date)) {
			return false;
		}
		if (terminalCode == null) {
			if (other.terminalCode != null) {
				return false;
			}
		}
		else if (!terminalCode.equals(other.terminalCode)) {
			return false;
		}
		return true;
	}

}
