/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package ball.spring.mysqld;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * {@code mysqld} {@link Configuration}.  A {@code mysqld} process is
 * started if the {@code mysqld.home} application property is set.  In
 * addition, a port must be specified with the {@code mysqld.port} property.
 *
 * <p>{@injected.fields}</p>
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@Configuration
@ConditionalOnProperty(name = "mysqld.home", havingValue = EMPTY)
public class MysqldConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();

    @Value("${mysqld.home}")
    private File home;

    @Value("${mysqld.datadir:${mysqld.home}/data}")
    private File datadir;

    @Value("${mysqld.port}")
    private Integer port;

    @Value("${mysqld.socket:${mysqld.home}/socket}")
    private File socket;

    @Value("${logging.path}/mysqld.log")
    private File console;

    private Process mysqld = null;

    /**
     * Sole constructor.
     */
    public MysqldConfiguration() { }

    @PostConstruct
    public void init() { }

    @Bean
    public Process mysqld() throws IOException {
        synchronized (this) {
            if (mysqld == null) {
                Files.createDirectories(home.toPath());
                Files.createDirectories(datadir.toPath().getParent());
                Files.createDirectories(console.toPath().getParent());

                if (! datadir.exists()) {
                    try {
                        new ProcessBuilder("mysqld",
                                           "--initialize-insecure",
                                           "--explicit-defaults-for-timestamp",
                                           "--datadir=" + datadir.getAbsolutePath())
                            .directory(home)
                            .inheritIO()
                            .redirectOutput(Redirect.to(console))
                            .redirectErrorStream(true)
                            .start()
                            .waitFor();
                    } catch (InterruptedException exception) {
                    }
                }

                if (datadir.exists()) {
                    socket.delete();

                    mysqld =
                        new ProcessBuilder("mysqld",
                                           "--general-log",
                                           "--log-output=TABLE",
                                           "--explicit-defaults-for-timestamp",
                                           "--datadir=" + datadir.getAbsolutePath(),
                                           "--port=" + port,
                                           "--socket=" + socket.getAbsolutePath())
                        .directory(home)
                        .inheritIO()
                        .redirectOutput(Redirect.appendTo(console))
                        .redirectErrorStream(true)
                        .start();

                    while (! socket.exists()) {
                        try {
                            mysqld.waitFor(15, SECONDS);
                        } catch (InterruptedException exception) {
                        }

                        if (mysqld.isAlive()) {
                            continue;
                        } else {
                            throw new IllegalStateException("mysqld not started");
                        }
                    }

                    try {
                        new ProcessBuilder("mysql_upgrade",
                                           "--socket=" + socket.getAbsolutePath(),
                                           "--user=root")
                            .directory(home)
                            .inheritIO()
                            .redirectOutput(Redirect.appendTo(console))
                            .redirectErrorStream(true)
                            .start()
                            .waitFor();
                    } catch (InterruptedException exception) {
                    }
                } else {
                    throw new IllegalStateException("mysqld datadir does not exist");
                }
            }
        }

        return mysqld;
    }

    @PreDestroy
    public void destroy() {
        if (mysqld != null) {
            try {
                for (int i = 0; i < 8; i+= 1) {
                    if (mysqld.isAlive()) {
                        mysqld.destroy();
                        mysqld.waitFor(15, SECONDS);
                    } else {
                        break;
                    }
                }
            } catch (InterruptedException exception) {
            }

            try {
                if (mysqld.isAlive()) {
                    mysqld.destroyForcibly().waitFor(60, SECONDS);
                }
            } catch (InterruptedException exception) {
            }
        }
    }

    @Override
    public String toString() { return super.toString(); }
}
