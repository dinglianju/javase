package org.log_slf4j.log4j;


import org.slf4j.LoggerFactory;

public class LogTest {
	private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LogTest.class);
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LogTest.class);
	
	public static void main(String[] args) {
		LOGGER.info("......info");
		LOGGER.debug("......debug");
		LOGGER.warn("........warn");
		LOGGER.error("........error");
		LOGGER.trace("......trace");
		
		int id = 1;
		String symbol = "one";
		if(logger.isDebugEnabled()) {
			logger.debug("log4j processing trade with id: " + id + " and symbol: " + symbol);
		}
		//在slf4j中不需要字符串连接，不会导致暂时不需要的字符串消耗
		LOGGER.debug("slf4j Processing trade with id: {} and symbol : {} ", id, symbol);
	
		System.out.println("LogTest system.out");
	}

}
