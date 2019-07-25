/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package ball.spring;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.webjars.RequireJS;

import static lombok.AccessLevel.PROTECTED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * UI {@link org.springframework.stereotype.Controller} abstract base class
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@NoArgsConstructor(access = PROTECTED) @ToString
public abstract class HTML5Controller implements ErrorController {
    private static final Logger LOGGER = LogManager.getLogger();

    @Value("${server.error.path:${error.path:/error}}")
    private String errorPath = null;

    @Autowired
    private ApplicationContext context = null;

    @Autowired
    private SpringResourceTemplateResolver resolver = null;

    private ConcurrentSkipListMap<String,Model> viewDefaultModelMap =
        new ConcurrentSkipListMap<>();

    @PostConstruct
    public void init() { resolver.setUseDecoupledLogic(true); }

    @PreDestroy
    public void destroy() { }

    @ModelAttribute
    public void addAttributes(Model model) {
        Model defaults =
            viewDefaultModelMap.computeIfAbsent(getViewName(),
                                                k -> computeDefaultModel(k));

        model.mergeAttributes(defaults.asMap());
    }

    private Model computeDefaultModel(String viewName) {
        BindingAwareModelMap model = new BindingAwareModelMap();

        try {
            Resource[] resources =
                context.getResources(resolver.getPrefix()
                                     + viewName + ".properties");

            new PropertiesFactory(resources).getObject()
                .entrySet()
                .forEach(t -> model.put(t.getKey().toString(), t.getValue()));
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }

        return model;
    }

    @ResponseBody
    @RequestMapping(value = "/webjarsjs",
                    produces = "application/javascript")
    public String wbejarsjs() {
        return RequireJS.getSetupJavaScript("/webjars/");
    }

    @RequestMapping(value = "${server.error.path:${error.path:/error}}")
    public String error() { return getViewName(); }

    /* org.springframework.web.servlet.RequestToViewNameTranslator */
    public String getViewName(/* HttpServletRequest request */) {
        return getClass().getPackage().getName().replaceAll("[.]", "-");
    }

    @ExceptionHandler
    @ResponseStatus(value = NOT_FOUND)
    public String handleNOT_FOUND(Model model,
                                  NoSuchElementException exception) {
        return handle(model, exception);
    }

    @ExceptionHandler
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    public String handle(Model model, Exception exception) {
        addAttributes(model);

        model.addAttribute("exception", exception);

        return getViewName();
    }

    @Override
    public String getErrorPath() { return errorPath; }

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
