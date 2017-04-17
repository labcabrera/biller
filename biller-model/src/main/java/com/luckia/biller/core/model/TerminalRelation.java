package com.luckia.biller.core.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.luckia.biller.core.serialization.NotSerializable;

import lombok.Data;

/**
 * Entidad que establece la relación entre un establecimiento y un terminal de apuestas.
 */
@Entity
@Table(name = "B_TERMINAL_RELATION")
@NamedQueries({
		@NamedQuery(name = "TerminalRelation.selectAll", query = "select t from TerminalRelation t order by t.code") })
@Data
@SuppressWarnings("serial")
public class TerminalRelation
		implements Serializable, Auditable, Mergeable<TerminalRelation> {

	@Id
	@Column(name = "ID")
	@GeneratedValue
	private Long id;

	@Column(name = "CODE", length = 64, unique = true)
	private String code;

	@Column(name = "IS_MASTER")
	private Boolean isMaster;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = true)
	@JoinColumn(name = "STORE_ID", referencedColumnName = "ID", nullable = true)
	@NotSerializable
	private Store store;

	@Column(name = "COMMENTS", columnDefinition = "TEXT")
	protected String comments;

	@Embedded
	protected AuditData auditData;

	@Override
	public void merge(TerminalRelation entity) {
		if (entity != null) {
			this.code = entity.code;
			this.comments = entity.comments;
			this.isMaster = entity.isMaster;
			this.store = entity.store;
		}
	}
}
