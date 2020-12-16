package ball.spring.jig.autoconfigure;

import ball.spring.jig.BeanRestController;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto {@link Configuration}.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Configuration
@ConditionalOnClass({ BeanRestController.class })
@Import({ BeanRestController.class })
@NoArgsConstructor @ToString @Log4j2
public class AutoConfiguration {
}
