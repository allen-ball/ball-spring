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
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * {@link EntityManagerFactoryDependsOnPostProcessor} {@link Component}
 * implementation.  See
 * {@link.uri https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-access.configure-a-component-that-is-used-by-jpa target=newtab Configure a Component that is Used by JPA}.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Component
@ConditionalOnProperty(name = "mysqld.home", havingValue = EMPTY)
@ToString @Log4j2
public class EntityManagerFactoryComponent extends EntityManagerFactoryDependsOnPostProcessor {
    @Autowired private Process mysqld;

    /**
     * Sole constructor.
     */
    public EntityManagerFactoryComponent() { super("mysqld"); }
}
