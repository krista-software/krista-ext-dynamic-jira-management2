package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.api;

import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * JAX-RS application hosting the OAuth callback (Pattern C, secondary auth option). Mounted at
 * rest/jiraoauth/... (the platform default jaxrsId "rest" is used, so no @Extension(jaxrsId=...)
 * override is needed - see DynamicJiraManagement2Attributes.APP_PATH).
 */
@Service
@ApplicationPath("/jiraoauth")
@ContractsProvided(Application.class)
public final class JiraCallbackApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(JiraAuthCallbackResource.class);
    }
}
