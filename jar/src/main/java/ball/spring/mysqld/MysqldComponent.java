package ball.spring.mysqld;
/*-
 * ##########################################################################
 * Reusable Spring Components
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2018 - 2021 Allen D. Ball
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
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * {@code mysqld} {@link Component}.
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
