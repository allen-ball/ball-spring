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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

    @Autowired
    private SpringResourceTemplateResolver resolver = null;

    @PostConstruct
    public void init() { resolver.setUseDecoupledLogic(true); }

    @PreDestroy
    public void destroy() { }

    /**
     * Method to get the name of the corresponding template.
     *
     * @return  The template name.
     */
    protected String template() {
        return getClass().getPackage().getName().replaceAll("[.]", "-");
    }

    @ResponseBody
    @RequestMapping(value = "/js/webjars.js",
                    produces = "application/javascript")
    public String wbejarsjs() {
        return RequireJS.getSetupJavaScript("/webjars/");
    }

    @ExceptionHandler
    @ResponseStatus(value = NOT_FOUND)
    public String handleNOT_FOUND(Model model,
                                  NoSuchElementException exception) {
        populate(model, exception);

        return template();
    }

    @ExceptionHandler
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    public String handleINTERNAL_SERVER_ERROR(Model model,
                                              Exception exception) {
        populate(model, exception);

        return template();
    }

    private void populate(Model model, Exception exception) {
        model.addAttribute("exception", exception);
    }
}
