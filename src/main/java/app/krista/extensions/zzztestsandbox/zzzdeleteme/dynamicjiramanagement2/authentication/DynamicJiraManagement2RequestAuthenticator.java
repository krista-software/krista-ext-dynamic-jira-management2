package app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.authentication;

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.DynamicJiraManagement2Attributes;
import app.krista.extensions.zzztestsandbox.zzzdeleteme.dynamicjiramanagement2.integration.JiraAttributeStore;
import app.krista.model.field.NamedField;
import app.krista.model.field.NamedValuedField;
import com.github.scribejava.core.oauth.OAuth20Service;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Dual-mode request authenticator: when AuthType is "ApiToken" the invoker itself IS the
 * authenticated account (github/PAT pattern, no interactive flow). When AuthType is "OAuth" this is
 * a verbatim-adapted port of the Gmail extension's GmailRequestAuthenticator (Krista Pattern C) - the
 * only things that vary from the reference port are the class/package names and the application path
 * (see {@link CallbackUrls}).
 *
 * Built with plain `new` inside the extension class's constructor - never @Service/@Inject (HK2 cannot
 * bind the bare invokerId String this class needs).
 */
public class DynamicJiraManagement2RequestAuthenticator implements RequestAuthenticator {

    static final String CALLBACK_PATH = "/" + DynamicJiraManagement2Attributes.APP_PATH + "/callback";

    private static final String STATE = "state";
    private static final String HASH = "#";
    private static final String AUTH_CONTEXT_ID = "authContextId";
    private static final String AUTHORIZE_URL_EXTRA_PARAMS = "&audience=api.atlassian.com&prompt=consent";
    private static final String FAILED_TO_AUTHENTICATE_USER = "Failed to authenticate user ";
    private static final String FAILED_TO_GET_AUTHORIZE_RESPONSE = "Failed to get authorize response ";

    private final JiraAttributeStore attributeStore;
    private final DynamicJiraManagement2Attributes attributes;
    private final String invokerId;

    public DynamicJiraManagement2RequestAuthenticator(JiraAttributeStore attributeStore,
                                                        DynamicJiraManagement2Attributes attributes,
                                                        String invokerId) {
        this.attributeStore = attributeStore;
        this.attributes = attributes;
        this.invokerId = invokerId;
    }

    @Override
    public String getScheme() {
        return attributes.isOAuth() ? null : "PAT";
    }

    @Override
    public Set<String> getSupportedProtocols() {
        return attributes.isOAuth() ? Set.of() : Set.of("http");
    }

    @Override
    public String getAuthenticatedAccountId(ProtoRequest protoRequest) {
        if (!attributes.isOAuth()) {
            return invokerId;
        }
        try {
            HttpRequest httpRequest = (HttpRequest) protoRequest;
            String path = httpRequest.getUri().getPath();
            if (path.equals(CALLBACK_PATH)) {
                httpRequest.bufferBody();
                MultivaluedMap<String, String> queryParameters = httpRequest.getQueryParameters();
                String state = String.valueOf(queryParameters.get(STATE).getFirst());
                String[] parts = state.split(HASH);
                return parts[0];
            }
            return invokerId;
        } catch (IOException cause) {
            throw new RuntimeException(FAILED_TO_AUTHENTICATE_USER + cause.getMessage(), cause.getCause());
        }
    }

    @Override
    public boolean setServiceAuthorization(String authorization) {
        return false;
    }

    @Override
    public Map<String, NamedField> getAttributeFields() {
        return Collections.emptyMap();
    }

    @Override
    public ProtoResponse getMustAuthenticateResponse(MustAuthenticateException exception, ProtoRequest request) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException exception) {
        return new AuthorizationResponse(null, List.of());
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException exception, ProtoRequest request) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException cause) {
        if (!attributes.isOAuth()) {
            return new AuthorizationResponse(null, List.of());
        }
        try {
            String userId = (String) cause.getDetails().get(0).getValue();
            Optional<NamedValuedField> authContextIdField = cause.getDetails().stream()
                    .filter(f -> Objects.equals(f.getName(), AUTH_CONTEXT_ID)).findFirst();

            DynamicJiraManagement2Attributes effectiveAttributes;
            String state;
            if (authContextIdField.isPresent()) {
                String authContextId = (String) authContextIdField.get().getValue();
                effectiveAttributes = attributeStore.load(authContextId);
                state = userId + HASH + authContextId;
            } else {
                effectiveAttributes = attributes;
                state = userId;
            }

            OAuth20Service oAuth20Service = effectiveAttributes.getOAuth20Service();
            String url = oAuth20Service.getAuthorizationUrl(state);
            return new AuthorizationResponse(url + AUTHORIZE_URL_EXTRA_PARAMS, List.of());
        } catch (RuntimeException runtimeException) {
            throw new RuntimeException(FAILED_TO_GET_AUTHORIZE_RESPONSE, runtimeException);
        }
    }
}
