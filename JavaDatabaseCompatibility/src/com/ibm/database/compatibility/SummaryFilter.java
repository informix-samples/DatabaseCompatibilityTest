package com.ibm.database.compatibility;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
public class SummaryFilter extends Filter<ILoggingEvent> {

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if(event.getMessage().contains("TEST"))
			return FilterReply.ACCEPT;
		else 
			return FilterReply.DENY;
	}

}
