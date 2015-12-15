package com.luckia.biller.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.luckia.biller.core.serialization.NotSerializable;

@Entity
@Table(name = "S_ENTITY_HISTORY")
public class EntityHistory {

	@Id
	@Column(name = "ID", length = 36)
	private String id;

	@Column(name = "ENTITY_ID", nullable = false, length = 36)
	private String entityId;

	@Column(name = "ENTITY_CLASS", nullable = false, columnDefinition = "VARCHAR(256)")
	@Convert(converter = ClassConverter.class)
	@NotSerializable
	private Class<?> entityClass;

	@Column(name = "ENTITY_DATE", nullable = false)
	private Date entityDate;

	@Column(name = "ENTITY_BEFORE", nullable = true, columnDefinition = "TEXT")
	private String entityBefore;

	@Column(name = "ENTITY_AFTER", nullable = false, columnDefinition = "TEXT")
	private String entityAfter;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	public Date getEntityDate() {
		return entityDate;
	}

	public void setEntityDate(Date entityDate) {
		this.entityDate = entityDate;
	}

	public String getEntityBefore() {
		return entityBefore;
	}

	public void setEntityBefore(String entityBefore) {
		this.entityBefore = entityBefore;
	}

	public String getEntityAfter() {
		return entityAfter;
	}

	public void setEntityAfter(String entityAfter) {
		this.entityAfter = entityAfter;
	}
}
