package org.log_slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
	final Logger logger = LoggerFactory.getLogger(App.class);

    private void test() {
        logger.info("这是一条日志信息 - {}", "mafly");
    }

    public static void main(String[] args) {
        App app = new App();
        app.test();
        
        System.out.println("Hello World!");
    }
}
