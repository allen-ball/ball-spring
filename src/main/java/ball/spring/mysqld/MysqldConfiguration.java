package ball.spring.mysqld;
/*-
 * ##########################################################################
 * Reusable Spring Components
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2018 - 2020 Allen D. Ball
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ##########################################################################
 */
import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
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
@NoArgsConstructor @ToString @Log4j2
public class MysqldConfiguration {
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

    @Value("${logging.file.path}/mysqld.log")
    private File console;

    private volatile Process mysqld = null;

    @PostConstruct
    public void init() { }

    @Bean
    public Process mysqld() throws IOException {
        if (mysqld == null) {
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
                    } else {
                        throw new IllegalStateException("mysqld datadir does not exist");
                    }
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
