package com.gitlab.aws.cfn.resources.projects.accesstoken;

import com.gitlab.aws.api.models.AccessToken;
import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;
import com.gitlab.aws.cfn.resources.shared.GitLabUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.gitlab4j.api.AbstractApi;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class AccessTokenResourceHandler extends AbstractGitlabCombinedResourceHandler<AccessTokenResourceHandler, AccessToken, Pair<Integer,Integer>, ResourceModel,CallbackContext,TypeConfigurationModel> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AccessTokenResourceHandler.class);

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> implements BaseHandlerAdapterDefault<AccessTokenResourceHandler, ResourceModel,CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return BaseHandlerAdapterDefault.super.handleRequest(proxy, request, callbackContext, logger, typeConfiguration);
        }

        @Override
        public AccessTokenResourceHandler newCombinedHandler() {
            return new AccessTokenResourceHandler();
        }
    }

    @Override
    protected GitLabApi newGitLabApiFromTypeConfiguration(TypeConfigurationModel typeModel) {
        return new GitLabApi(firstNonBlank(typeModel.getGitLabAccess().getUrl(), DEFAULT_URL), typeModel.getGitLabAccess().getAccessToken());
    }

    @Override
    public AccessTokenHelper newHelper() {
        return new AccessTokenHelper();
    }

    public class AccessTokenHelper extends Helper {
        @Override
        public Pair<Integer,Integer> getId(ResourceModel model) {
            return GitLabUtils.pair(model.getProjectId(), model.getId());
        }

        @Override
        public Optional<AccessToken> findExistingItemWithNonNullId(Pair<Integer,Integer> id) throws Exception {
            return findExistingItemWithIdDefaultInefficiently(id);
        }

        @Override
        public List<AccessToken> readExistingItems() throws GitLabApiException {
            return getAccessTokens(gitlab,model.getProjectId());
        }

        @Override
        public void deleteItem(AccessToken item) throws GitLabApiException {
            deleteAccessToken(gitlab,model.getProjectId(), item.getId());
        }

        @Override
        public ResourceModel modelFromItem(AccessToken item) {
            ResourceModel m = new ResourceModel();
            m.setId(item.getId());
            m.setName(item.getName());
            m.setProjectId(model.getProjectId());
            m.setScopes(item.getScopes());
            m.setAccessLevel(model.getAccessLevel());
            return m;
        }

        @Override
        public AccessToken createItem() throws GitLabApiException {
            AccessToken accessToken = AccessToken.builder()
                    .name(model.getName())
                    .scopes(model.getScopes())
                    .accessLevel(model.getAccessLevel())
                    .createdAt(Date.from(Instant.now())) // defaults for now
                    .expiresAt(Date.from(Instant.now().plus(30, ChronoUnit.DAYS))) // defaults for now
                    .accessLevel(30) // defaults for now
                    .build();
            return postAccessToken(gitlab, model.getProjectId(), accessToken); //this will have its 'token' property set.
        }

        @Override
        public void updateItem(AccessToken existingItem, List<String> updates) throws GitLabApiException {
           // no-op CFN should prevent any fields from being updated - access token update is not supported
        }
    }

    public static AccessToken postAccessToken(GitLabApi gitlab, Integer projectId, AccessToken accessToken) throws GitLabApiException {
        return new AbstractApi(gitlab) {
            public AccessToken postAccessToken() throws GitLabApiException {
                Response r = post(Response.Status.CREATED, accessToken, "projects", projectId, "access_tokens");
                return r.readEntity(AccessToken.class);
            }
        }.postAccessToken();
    }

    public static List<AccessToken> getAccessTokens(GitLabApi gitlab, Integer projectId) throws GitLabApiException {
        return new AbstractApi(gitlab) {
            public List<AccessToken> getAccessTokens() throws GitLabApiException {
                Response r = get(Response.Status.OK, null, "projects", projectId, "access_tokens");
                List<?> accessTokensRaw = r.readEntity(List.class);
                return ((List<Map<?,?>>)accessTokensRaw).stream().map(AccessToken::of).collect(Collectors.toList());
            }
        }.getAccessTokens();
    }

    public static void deleteAccessToken(GitLabApi gitlab, Integer projectId, Integer accessTokenId) throws GitLabApiException {
        new AbstractApi(gitlab) {
            public void deleteAccessToken(Integer projectId) throws GitLabApiException {
                delete(Response.Status.OK, null, "projects", projectId, "access_tokens", accessTokenId);
            }
        }.deleteAccessToken(projectId);
    }

}
