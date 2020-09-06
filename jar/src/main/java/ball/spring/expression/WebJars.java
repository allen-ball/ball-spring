package ball.spring.expression;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.webjars.WebJarAssetLocator;

/**
 * {@link WebJars} Thymeleaf dialect implementation.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@NoArgsConstructor @ToString @Log4j2
public class WebJars {
    @CompileTimeCheck
    private static final Pattern PATTERN =
        Pattern.compile("(?is)"
                        + "(?<prefix>.*" + WebJarAssetLocator.WEBJARS_PATH_PREFIX + ")"
                        + "(?<path>/.*)");

    private WebJarAssetLocator locator = new WebJarAssetLocator();

    /**
     * Method to convert a WebJar resource path to its corresponding CDN
     * (URI) path.  Typical Thymeleaf usage:
     * {@code @{${#webjars.cdn(#request.scheme, path)}}}.
     *
     * @param   scheme          The {@code URI} scheme.
     * @param   path            The (possibly partial) path.
     *
     * @return  The CDN URI if one may be constructed; {@code path}
     *          otherwise.
     */
    public String cdn(String scheme, String path) {
        String uri = null;

        try {
            String resource = locator.getFullPath(path);
            String groupId = locator.groupId(resource);
            Matcher matcher = PATTERN.matcher(resource);

            if (matcher.matches()) {
                uri =
                    String.format("%s://cdn.jsdelivr.net/webjars/%s%s",
                                  (scheme != null) ? scheme : "http",
                                  groupId, matcher.group("path"));
            }
        } catch (Exception exception) {
        }

        return (uri != null) ? uri : path;
    }

    /**
     * See {@link WebJarAssetLocator#getWebJars()}.
     *
     * @return  Result of {@link WebJarAssetLocator#getWebJars()} call.
     */
    public Map<String,String> getWebJars() { return locator.getWebJars(); }
}
