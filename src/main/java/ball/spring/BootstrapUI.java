/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package ball.spring;

import java.util.NoSuchElementException;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.webjars.RequireJS;

import static lombok.AccessLevel.PROTECTED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Bootstrap UI {@link org.springframework.stereotype.Controller} abstract
 * base class
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@NoArgsConstructor(access = PROTECTED) @ToString
public abstract class BootstrapUI {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * View name for this template.
     */
    protected static final String VIEW = BootstrapUI.class.getSimpleName();

    @ModelAttribute("template")
    public abstract String template();

    @ModelAttribute("javascript")
    public String javascript() {
        return RequireJS.getSetupJavaScript("/webjars/");
    }

    @ResponseBody
    @RequestMapping(value = "/webjars.js", produces = "application/javascript")
    public String wbejarsjs() { return javascript(); }

    @ExceptionHandler
    @ResponseStatus(value = NOT_FOUND)
    public String handleNOT_FOUND(Model model,
                                  NoSuchElementException exception) {
        populate(model, exception);

        return VIEW;
    }

    @ExceptionHandler
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    public String handleINTERNAL_SERVER_ERROR(Model model,
                                              Exception exception) {
        populate(model, exception);

        return VIEW;
    }

    private void populate(Model model, Exception exception) {
        model.addAttribute("template", template());
        model.addAttribute("exception", exception);
    }
}
