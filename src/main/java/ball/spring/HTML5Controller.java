/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package ball.spring;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.webjars.RequireJS;

import static java.util.stream.Collectors.toMap;
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

    @PostConstruct
    public void init() { resolver.setUseDecoupledLogic(true); }

    @PreDestroy
    public void destroy() { }

    @ModelAttribute
    public void addAttributes(Model model) {
        try {
            Properties properties = new Properties();
            String name = resolver.getPrefix() + getViewName() + ".properties";

            for (Resource resource : context.getResources(name)) {
                PropertiesLoaderUtils.fillProperties(properties, resource);
            }

            Map<String,Object> map =
                properties.entrySet()
                .stream()
                .collect(toMap(k -> k.getKey().toString(),
                               v -> v.getValue()));

            model.mergeAttributes(map);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
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
}
