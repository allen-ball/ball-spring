package ball.spring.mysqld.autoconfigure;
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
import ball.spring.mysqld.EntityManagerFactoryComponent;
import ball.spring.mysqld.MysqldConfiguration;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto {@link Configuration}.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Configuration
@ConditionalOnClass({ MysqldConfiguration.class })
@Import({ EntityManagerFactoryComponent.class, MysqldConfiguration.class })
@NoArgsConstructor @ToString @Log4j2
public class AutoConfiguration {
}
