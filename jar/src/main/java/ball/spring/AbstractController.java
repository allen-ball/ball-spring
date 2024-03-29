package ball.spring;
/*-
 * ##########################################################################
 * Reusable Spring Components
 * %%
 * Copyright (C) 2018 - 2022 Allen D. Ball
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
import ball.spring.dialect.WebJarsDialect;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.ui.Model;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.webjars.RequireJS;

import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.lang3.StringUtils.appendIfMissing;
import static org.apache.commons.lang3.StringUtils.prependIfMissing;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Abstract {@link org.springframework.stereotype.Controller} base class.
 * Implements {@link ErrorController}, implements {@link #getViewName()}
 * (with
 * {@code String.join("-", getClass().getPackage().getName().split(Pattern.quote(".")))}),
 * provides {@link #addDefaultModelAttributesTo(Model)} from corresponding
 * {@code template.model.properties}, and configures
 * {@link SpringResourceTemplateResolver} to use decoupled logic.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 */
@NoArgsConstructor(access = PROTECTED) @ToString @Log4j2
public abstract class AbstractController implements ErrorController {
    @Value("${server.error.path:${error.path:/error}}")
    private String errorPath = null;

    @Autowired
    private ApplicationContext context = null;

    @Autowired
    private SpringTemplateEngine engine = null;

    @Autowired
    private SpringResourceTemplateResolver resolver = null;

    private ConcurrentSkipListMap<String,Properties> viewDefaultAttributesMap = new ConcurrentSkipListMap<>();

    @PostConstruct
    public void init() {
        engine.addDialect(new WebJarsDialect());
        resolver.setUseDecoupledLogic(true);
    }

    @PreDestroy
    public void destroy() { }

    /* org.springframework.web.servlet.RequestToViewNameTranslator */
    public String getViewName(/* HttpServletRequest request */) {
        return String.join("-", getClass().getPackage().getName().split(Pattern.quote(".")));
    }

    @ModelAttribute
    public void addDefaultModelAttributesTo(Model model) {
        BindingAwareModelMap defaults = new BindingAwareModelMap();
        Properties properties =
            viewDefaultAttributesMap.computeIfAbsent(getViewName(), k -> getDefaultAttributesFor(k));

        for (Map.Entry<Object,Object> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();

            while (value != null) {
                String unresolved = value;

                value = context.getEnvironment().resolvePlaceholders(unresolved);

                if (unresolved.equals(value)) {
                    break;
                }
            }

            defaults.put(key, value);
        }

        model.mergeAttributes(defaults.asMap());
    }

    private Properties getDefaultAttributesFor(String name) {
        Properties properties = null;

        try {
            name = prependIfMissing(name, resolver.getPrefix());
            name = removeEnd(name, resolver.getSuffix());
            name = appendIfMissing(name, ".model.properties");

            properties = new PropertiesFactory(context.getResources(name)).getObject();
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }

        return properties;
    }

    /**
     * See {@link RequireJS#getSetupJavaScript(String)}.
     *
     * @return  The set-up javascript.
     */
    @ResponseBody
    @RequestMapping(value = "/webjarsjs", produces = "application/javascript")
    public String webjarsjs() {
        return RequireJS.getSetupJavaScript("/webjars/");
    }

    @RequestMapping(value = "${server.error.path:${error.path:/error}}")
    public String error() { return getViewName(); }

    @ExceptionHandler
    @ResponseStatus(value = NOT_FOUND)
    public String handleNOT_FOUND(Model model, NoSuchElementException exception) {
        return handle(model, exception);
    }

    @ExceptionHandler
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    public String handle(Model model, Exception exception) {
        addDefaultModelAttributesTo(model);

        model.addAttribute("exception", exception);

        return getViewName();
    }

    @ToString
    private class PropertiesFactory extends PropertiesFactoryBean {
        public PropertiesFactory(Resource[] resources) {
            super();

            try {
                setIgnoreResourceNotFound(true);
                setLocations(resources);
                setSingleton(false);

                mergeProperties();
            } catch (Exception exception) {
                throw new ExceptionInInitializerError(exception);
            }
        }
    }
}
