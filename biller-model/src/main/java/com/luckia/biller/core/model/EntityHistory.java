package com.luckia.biller.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.luckia.biller.core.serialization.NotSerializable;

import lombok.Data;

@Entity
@Table(name = "S_ENTITY_HISTORY")
@Data
@SuppressWarnings("serial")
public class EntityHistory implements Serializable {

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

}
