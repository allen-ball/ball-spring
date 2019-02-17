/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package ball.spring.mysqld;

import lombok.NoArgsConstructor;
import lombok.ToString;
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
 * <p>{@injected.fields}</p>
 *
 * @see MysqldConfiguration
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@Component
@ConditionalOnBean(name = { "mysqld" })
@NoArgsConstructor @ToString
public class MysqldComponent {
    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired private Process mysqld;

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
}
