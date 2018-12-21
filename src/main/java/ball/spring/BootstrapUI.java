/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package ball.spring;

import java.util.NoSuchElementException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Bootstrap UI {@link org.springframework.stereotype.Controller} abstract
 * base class
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
public abstract class BootstrapUI {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * View name for this template.
     */
    protected static final String VIEW = BootstrapUI.class.getSimpleName();

    /**
     * Sole constructor.
     */
    protected BootstrapUI() { }

    @ModelAttribute("template")
    public abstract String template();

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

    @Override
    public String toString() { return super.toString(); }
}
