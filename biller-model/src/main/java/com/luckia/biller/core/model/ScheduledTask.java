package com.luckia.biller.core.model;

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

@Entity
@Table(name = "S_SCHEDULED_TASK")
@NamedQueries({ @NamedQuery(name = "ScheduledTask.selectEnabled", query = "select e from ScheduledTask e where e.enabled = true") })
public class ScheduledTask implements Mergeable<ScheduledTask> {

	@Id
	@Column(name = "ID")
	@GeneratedValue
	private Long id;

	@Column(name = "NAME", length = 128, nullable = false)
	private String name;

	@Column(name = "CLASSNAME", nullable = false, columnDefinition = "TEXT")
	@Convert(converter = ClassConverter.class)
	private Class<?> executorClass;

	@Column(name = "CRON_EXP", length = 64)
	private String cronExpression;

	@Column(name = "ENABLED", nullable = false)
	private Boolean enabled;

	@ElementCollection(fetch = FetchType.LAZY)
	@MapKeyColumn(name = "PARAM_KEY", length = 516)
	@Column(name = "PARAM_VALUE", length = 64)
	@CollectionTable(name = "S_SCHEDULED_TASK_PARAM", joinColumns = @JoinColumn(name = "TASK_ID") )
	private Map<String, String> params = new HashMap<String, String>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public Class<?> getExecutorClass() {
		return executorClass;
	}

	public void setExecutorClass(Class<?> executorClass) {
		this.executorClass = executorClass;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScheduledTask [id=").append(id).append(", name=").append(name);
		builder.append(", className=").append(executorClass != null ? executorClass.getName() : "<null>");
		builder.append(", cronExpression=").append(cronExpression).append("]");
		return builder.toString();
	}

	@Override
	public void merge(ScheduledTask t) {
		this.name = t.name;
		this.cronExpression = t.cronExpression;
		this.executorClass = t.executorClass;
		this.enabled = t.enabled;
	}
}
