/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package ball.spring.mysqld;

import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * {@code mysqld} {@link Component}
 *
 * {@injected.fields}
 *
 * @see MysqldConfiguration
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Component
@ConditionalOnBean(name = { "mysqld" })
@NoArgsConstructor @ToString @Log4j2
public class MysqldComponent {
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
