package ball.spring.dialect;
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
import ball.spring.expression.WebJars;
import java.util.Set;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;

import static java.util.stream.Collectors.toSet;

/**
 * {@link WebJars} Thymeleaf dialect expression factory.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@NoArgsConstructor @ToString @Log4j2
public class WebJarsExpressionFactory implements IExpressionObjectFactory {
    private static final String WEBJARS = "webjars";

    private static final Set<String> ALL_NAMES =
        Stream.of(WEBJARS).collect(toSet());

    @Override
    public Set<String> getAllExpressionObjectNames() { return ALL_NAMES; }

    @Override
    public Object buildObject(IExpressionContext context, String name) {
        Object object = null;

        if (WEBJARS.equals(name)) {
            object = new WebJars();
        }

        return object;
    }

    @Override
    public boolean isCacheable(String name) {
        return name != null && ALL_NAMES.contains(name);
    }
}
