package ball.spring.jig;
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
import java.util.NoSuchElementException;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Bean {@link RestController} implementation.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@RestController
@RequestMapping(value = { "/jig/bean/" })
@ResponseBody
@NoArgsConstructor @ToString @Log4j2
public class BeanRestController implements ApplicationContextAware {
    private ApplicationContext context = null;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    @RequestMapping(method = { GET }, value = { "{name}.json" },
                    produces = APPLICATION_JSON_VALUE)
    public Object json(@PathVariable String name) throws Exception {
        return context.getBean(name);
    }

    @RequestMapping(method = { GET }, value = { "{name}.xml" },
                    produces = APPLICATION_XML_VALUE)
    public Object xml(@PathVariable String name) throws Exception {
        return context.getBean(name);
    }

    @ExceptionHandler({
                           NoSuchBeanDefinitionException.class,
                               NoSuchElementException.class
                     })
    @ResponseStatus(value = NOT_FOUND, reason = "Resource not found")
    public void handleNOT_FOUND() { }
}
