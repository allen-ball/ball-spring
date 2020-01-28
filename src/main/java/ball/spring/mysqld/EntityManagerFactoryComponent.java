/*
 * $Id$
 *
 * Copyright 2018 - 2020 Allen D. Ball.  All rights reserved.
 */
package ball.spring.mysqld;

import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * {@link EntityManagerFactoryDependsOnPostProcessor} {@link Component}
 * implementation.  See
 * {@link.uri https://docs.spring.io/spring-boot/docs/current/reference/html/howto-data-access.html#howto-configure-a-component-that-is-used-by-JPA target=newtab Configure a Component that is Used by JPA}.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Component
@ConditionalOnProperty(name = "mysqld.home", havingValue = EMPTY)
@ToString
public class EntityManagerFactoryComponent
             extends EntityManagerFactoryDependsOnPostProcessor {
    @Autowired private Process mysqld;

    /**
     * Sole constructor.
     */
    public EntityManagerFactoryComponent() { super("mysqld"); }
}
