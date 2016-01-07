package com.luckia.biller.core.services.mail;

import org.apache.commons.lang3.StringUtils;

import com.luckia.biller.core.model.common.Message;

public class MailMessageConverter {

	private final Message<?> message;

	public MailMessageConverter(Message<?> message) {
		this.message = message;
	}

	public String getBody() {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isNotBlank(message.getMessage())) {
			sb.append(message.getMessage()).append("<br>");
		}
		if (StringUtils.isNotBlank(message.getCode())) {
			sb.append("Code: ").append(message.getCode()).append("<br>");
		}
		if (message.getErrors() != null && !message.getErrors().isEmpty()) {
			sb.append("Errors").append("<br>");
			sb.append("<ul>");
			for (String i : message.getErrors()) {
				sb.append("<li>").append(i).append("</li>");
			}
			sb.append("</ul>");
		}
		return sb.toString();
	}
}
