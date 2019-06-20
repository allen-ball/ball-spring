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
import lombok.NoArgsConstructor;
import lombok.ToString;
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
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Configuration
@ConditionalOnProperty(name = "mysqld.home", havingValue = EMPTY)
@NoArgsConstructor @ToString
public class MysqldConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();

    @Value("${mysqld.home}")
    private File home;

    @Value("${mysqld.defaults.file:${mysqld.home}/my.cnf}")
    private File defaults;

    @Value("${mysqld.datadir:${mysqld.home}/data}")
    private File datadir;

    @Value("${mysqld.port}")
    private Integer port;

    @Value("${mysqld.socket:${mysqld.home}/socket}")
    private File socket;

    @Value("${logging.path}/mysqld.log")
    private File console;

    private Process mysqld = null;

    @PostConstruct
    public void init() { }

    @Bean
    public Process mysqld() throws IOException {
        synchronized (this) {
            if (mysqld == null) {
                Files.createDirectories(home.toPath());
                Files.createDirectories(datadir.toPath().getParent());
                Files.createDirectories(console.toPath().getParent());

                String defaultsArg = "--no-defaults";

                if (defaults.exists()) {
                    defaultsArg =
                        "--defaults-file=" + defaults.getAbsolutePath();
                }

                String datadirArg = "--datadir=" + datadir.getAbsolutePath();
                String socketArg = "--socket=" + socket.getAbsolutePath();
                String portArg = "--port=" + port;

                if (! datadir.exists()) {
                    try {
                        new ProcessBuilder("mysqld",
                                           defaultsArg, datadirArg,
                                           "--initialize-insecure")
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
                                           defaultsArg, datadirArg,
                                           socketArg, portArg)
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
                                           defaultsArg, socketArg,
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
}
