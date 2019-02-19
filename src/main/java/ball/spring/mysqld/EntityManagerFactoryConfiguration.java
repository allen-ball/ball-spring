/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package ball.spring.mysqld;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Configuration;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * {@link EntityManagerFactoryDependsOnPostProcessor} {@link Configuration}
 * implementation.  See
 * {@link.uri https://docs.spring.io/spring-boot/docs/current/reference/html/howto-data-access.html#howto-configure-a-component-that-is-used-by-JPA target=newtab Configure a Component that is Used by JPA}.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@Configuration
@ConditionalOnProperty(name = "mysqld.home", havingValue = EMPTY)
public class EntityManagerFactoryConfiguration
             extends EntityManagerFactoryDependsOnPostProcessor {
    @Autowired private Process mysqld;

    /**
     * Sole constructor.
     */
    public EntityManagerFactoryConfiguration() { super("mysqld"); }

    @Override
    public String toString() { return super.toString(); }
}
