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

import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
           throw new NotSupportedException("A token cannot be retrieved!");
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
            return m;
        }

        @Override
        public AccessToken createItem() throws GitLabApiException {
            AccessToken AccessToken = new AccessToken()
                    .withName(model.getName())
                    .withScopes(model.getScopes())
                    .withCreatedAt(Date.from(Instant.now())) // defaults for now
                    .withExpiresAt(Date.from(Instant.now().plus(30, ChronoUnit.DAYS))) // defaults for now
                    .withAccessLevel(30) // defaults for now
                    ;
            return postAccessToken(gitlab, model.getProjectId(), AccessToken);
        }

        @Override
        public void updateItem(AccessToken existingItem, List<String> updates) throws GitLabApiException {
            throw new NotSupportedException("A token cannot be updated!");
        }
    }

    public static AccessToken postAccessToken(GitLabApi gitlab, Integer projectId, AccessToken AccessToken) throws GitLabApiException {
        return new AbstractApi(gitlab) {
            public AccessToken postAccessToken() throws GitLabApiException {
                Response r = post(Response.Status.CREATED, AccessToken, "projects", projectId, "access_tokens");
                return r.readEntity(AccessToken.class);
            }
        }.postAccessToken();
    }

    public static List<AccessToken> getAccessTokens(GitLabApi gitlab, Integer projectId) throws GitLabApiException {
        return new AbstractApi(gitlab) {
            public List<AccessToken> getAccessTokens() throws GitLabApiException {
                Response r = get(Response.Status.OK, null, "projects", projectId, "access_tokens");
                List<AccessToken> AccessTokens = r.readEntity(List.class);  // TODO check this
                return AccessTokens;
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
