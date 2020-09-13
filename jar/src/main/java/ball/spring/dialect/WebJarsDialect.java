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
import ball.annotation.CompileTimeCheck;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.AntPathMatcher;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.webjars.WebJarAssetLocator;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static org.thymeleaf.templatemode.TemplateMode.HTML;

/**
 * {@link WebJarAssetLocator} Thymeleaf dialect.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@ToString @Log4j2
public class WebJarsDialect extends AbstractProcessorDialect
                            implements IExpressionObjectDialect {
    private static final String NAME = "WebJars Dialect";
    private static final String PREFIX = "webjars";
    private static final int PRECEDENCE = 9999;

    @CompileTimeCheck
    private static final Pattern PATTERN =
        Pattern.compile("(?is)"
                        + "(?<prefix>.*" + WebJarAssetLocator.WEBJARS_PATH_PREFIX + ")"
                        + "(?<path>/.*)");

    private static final String LOCAL_FORMAT = "/webjars%s";
    private static final String CDN_FORMAT =
        "%s://cdn.jsdelivr.net/webjars/%s%s";

    @Getter(lazy = true)
    private final IExpressionObjectFactory expressionObjectFactory =
        new ExpressionObjectFactory();

    /**
     * Sole constructor.
     */
    public WebJarsDialect() { super(NAME, PREFIX, PRECEDENCE); }

    @Override
    public Set<IProcessor> getProcessors(String prefix) {
        Set<IProcessor> set =
            Stream.of("href", "src")
            .map(t -> new PathAttributeTagProcessor(prefix, t))
            .collect(toSet());

        return set;
    }

    private static String path(WebJarAssetLocator locator,
                               boolean useCdn, String scheme, String path) {
        String resource = locator.getFullPath(path);
        Matcher matcher = PATTERN.matcher(resource);

        if (matcher.matches()) {
            if (useCdn) {
                path =
                    String.format(CDN_FORMAT,
                                  (scheme != null) ? scheme : "http",
                                  locator.groupId(resource),
                                  matcher.group("path"));
            } else {
                path =
                    String.format(LOCAL_FORMAT,
                                  matcher.group("path"));
            }
        }

        return path;
    }

    @ToString
    private static class PathAttributeTagProcessor extends AbstractAttributeTagProcessor {
        private final WebJarAssetLocator locator = new WebJarAssetLocator();

        public PathAttributeTagProcessor(String prefix, String name) {
            super(HTML, prefix, null, false, name, true, PRECEDENCE, true);
        }

        @Override
        protected void doProcess(ITemplateContext context,
                                 IProcessableElementTag tag,
                                 AttributeName name, String value,
                                 IElementTagStructureHandler handler) {
            IEngineConfiguration configuration = context.getConfiguration();
            IStandardExpressionParser parser =
                StandardExpressions.getExpressionParser(configuration);
            IStandardExpression expression =
                parser.parseExpression(context, value);
            String path = (String) expression.execute(context);
            String scheme =
                (String)
                parser.parseExpression(context, "${#request.scheme}")
                .execute(context);

            try {
                path = path(locator, false, scheme, path);
            } catch (IllegalArgumentException exception) {
            }

            handler.setAttribute(name.getAttributeName(), path);
        }
    }

    @NoArgsConstructor @ToString
    private static class ExpressionObjectFactory implements IExpressionObjectFactory {
        private final Map<String,Object> map =
            Collections.singletonMap(PREFIX, new WebJars());

        @Override
        public Set<String> getAllExpressionObjectNames() {
            return map.keySet();
        }

        @Override
        public Object buildObject(IExpressionContext context, String name) {
            return (name != null) ? map.get(name) : null;
        }

        @Override
        public boolean isCacheable(String name) {
            return name != null && map.containsKey(name);
        }
    }

    /**
     * {@link WebJars} Thymeleaf dialect expression object implementation.
     */
    @NoArgsConstructor @ToString
    public static class WebJars {
        @CompileTimeCheck
        private static final Pattern PATTERN =
            Pattern.compile("(?i)[\\p{Space},]+");

        private final WebJarAssetLocator locator = new WebJarAssetLocator();
        private final Set<String> assets = locator.listAssets();
        private final AntPathMatcher matcher = new AntPathMatcher();

        /**
         * Method to return WebJar resources matching {@link AntPathMatcher}
         * patterns.
         *
         * @param   patterns        The {@link AntPathMatcher} patterns to
         *                          match.
         *
         * @return  The matching resource paths.
         */
        public Collection<String> assets(String... patterns) {
            return assets(false, null, patterns);
        }

        /**
         * Method to return WebJar resources matching {@link AntPathMatcher}
         * patterns.
         *
         * @param   useCdn          {@code true} to provide a CDN
         *                          {@code URI}; {@code false} for a local
         *                          path.
         * @param   scheme          The {@code URI} scheme.
         * @param   patterns        The {@link AntPathMatcher} patterns to
         *                          match.
         *
         * @return  The matching resource paths.
         */
        public Collection<String> assets(boolean useCdn, String scheme,
                                         String... patterns) {
            Collection<String> collection =
                Stream.of(patterns)
                .flatMap(t -> PATTERN.splitAsStream(t))
                .flatMap(t -> assets.stream().filter(a -> matcher.match(t, a)))
                .map(t -> path(locator, useCdn, scheme, t))
                .collect(toCollection(LinkedHashSet::new));

            return collection;
        }

        /**
         * Method to convert a WebJar resource (partial) path to its
         * corresponding CDN (URI) path.  Typical Thymeleaf usage:
         *
         * {@code @{${#webjars.cdn(#request.scheme, path)}}}.
         *
         * @param   scheme          The {@code URI} scheme.
         * @param   path            The (possibly partial) path.
         *
         * @return  The CDN URI if one may be constructed; {@code path}
         *          otherwise.
         */
        public String cdn(String scheme, String path) {
            try {
                path = path(locator, false, null, path);
            } catch (IllegalArgumentException exception) {
            }

            return path;
        }

        /**
         * See {@link WebJarAssetLocator#getWebJars()}.
         *
         * @return  Result of {@link WebJarAssetLocator#getWebJars()} call.
         */
        public Map<String,String> getJars() { return locator.getWebJars(); }
    }
}
