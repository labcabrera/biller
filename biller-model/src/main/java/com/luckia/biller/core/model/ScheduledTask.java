package com.luckia.biller.core.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.luckia.biller.core.serialization.NotSerializable;

import lombok.Data;

@Entity
@Table(name = "S_SCHEDULED_TASK")
@NamedQueries({
		@NamedQuery(name = "ScheduledTask.selectEnabled", query = "select e from ScheduledTask e where e.enabled = true") })
@Data
@SuppressWarnings("serial")
public class ScheduledTask implements Mergeable<ScheduledTask>, Serializable {

	@Id
	@Column(name = "ID")
	@GeneratedValue
	private Long id;

	@Column(name = "NAME", length = 128, nullable = false)
	private String name;

	@Column(name = "CLASSNAME", nullable = false, columnDefinition = "TEXT")
	@Convert(converter = ClassConverter.class)
	@NotSerializable
	private Class<?> executorClass;

	@Column(name = "CRON_EXP", length = 64)
	private String cronExpression;

	@Column(name = "ENABLED", nullable = false)
	private Boolean enabled;

	@ElementCollection(fetch = FetchType.LAZY)
	@MapKeyColumn(name = "PARAM_KEY", length = 516)
	@Column(name = "PARAM_VALUE", length = 64)
	@CollectionTable(name = "S_SCHEDULED_TASK_PARAM", joinColumns = @JoinColumn(name = "TASK_ID"))
	private Map<String, String> params = new HashMap<String, String>();

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScheduledTask [id=").append(id).append(", name=").append(name);
		builder.append(", className=")
				.append(executorClass != null ? executorClass.getName() : "<null>");
		builder.append(", cronExpression=").append(cronExpression).append("]");
		return builder.toString();
	}

	@Override
	public void merge(ScheduledTask t) {
		this.name = t.name;
		this.cronExpression = t.cronExpression;
		this.enabled = t.enabled;
	}
}
