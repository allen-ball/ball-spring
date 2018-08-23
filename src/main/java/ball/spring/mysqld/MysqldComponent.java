/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package ball.spring.mysqld;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * {@code mysqld} {@link Component}
 *
 * @see MysqldConfiguration
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@Component
@ConditionalOnBean(name = { "mysqld" })
public class MysqldComponent {
    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired private Process mysqld;

    /**
     * Sole constructor.
     */
    public MysqldComponent() { super(); }

    @Scheduled(fixedRate = 15 * 1000)
    public void run() {
        if (mysqld != null) {
            if (mysqld.isAlive()) {
                try {
                    mysqld.waitFor(15, SECONDS);
                } catch (InterruptedException exception) {
                }
            } else {
                throw new IllegalStateException("mysqld is not running");
            }
        }
    }

    @Override
    public String toString() { return super.toString(); }
}
