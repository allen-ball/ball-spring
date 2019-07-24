/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package ball.spring;

import java.util.NoSuchElementException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
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
public abstract class HTML5Controller {
    private static final Logger LOGGER = LogManager.getLogger();

    @Value("${stylesheets:}")
    private String[] stylesheets = null;

    @Value("${style:#{null}}")
    private String style = null;

    @Value("${scripts:}")
    private String[] scripts = null;

    @Autowired
    private SpringResourceTemplateResolver resolver = null;

    @PostConstruct
    public void init() { resolver.setUseDecoupledLogic(true); }

    @PreDestroy
    public void destroy() { }

    @ModelAttribute("stylesheets")
    public String[] stylesheets() { return stylesheets; }

    @ModelAttribute("style")
    public String style() { return style; }

    @ModelAttribute("scripts")
    public String[] scripts() { return scripts; }

    @ResponseBody
    @RequestMapping(value = "/webjarsjs",
                    produces = "application/javascript")
    public String wbejarsjs() {
        return RequireJS.getSetupJavaScript("/webjars/");
    }

    /* org.springframework.web.servlet.RequestToViewNameTranslator */
    public String getViewName(/* HttpServletRequest request */) {
        return getClass().getPackage().getName().replaceAll("[.]", "-");
    }

    @ExceptionHandler
    @ResponseStatus(value = NOT_FOUND)
    public String handleNOT_FOUND(Model model,
                                  NoSuchElementException exception) {
        populate(model, exception);

        return getViewName();
    }

    @ExceptionHandler
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    public String handleINTERNAL_SERVER_ERROR(Model model,
                                              Exception exception) {
        populate(model, exception);

        return getViewName();
    }

    private void populate(Model model, Exception exception) {
        model.addAttribute("exception", exception);
    }
}
