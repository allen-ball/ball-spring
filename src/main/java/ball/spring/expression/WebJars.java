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
    private WebJarAssetLocator locator = new WebJarAssetLocator();

    /**
     * Method to convert a WebJar resource path to its corresponding CDN
     * (URI) path.
     *
     * @param   scheme          The {@code URI} scheme.
     * @param   path            The (possibly partial) local path,
     *
     * @return  The CDN URI if one may be constructed; {@code path}
     *          otherwise.
     */
    public String cdn(String scheme, String path) {
        String uri = null;
        String groupId = (path != null) ? groupId(path) : null;

        if (groupId != null) {
            uri =
                String.format("%s://cdn.jsdelivr.net/webjars/%s/%s",
                              (scheme != null) ? scheme : "http",
                              groupId, getLocalPath(path));
        }

        return (uri != null) ? uri : path;
    }

    private String getLocalPath(String path) {
        return locator.getFullPath(path).replaceAll("^.*/webjars/", "");
    }

    private String groupId(String path) {
        return locator.groupId(locator.getFullPath(path));
    }
}
