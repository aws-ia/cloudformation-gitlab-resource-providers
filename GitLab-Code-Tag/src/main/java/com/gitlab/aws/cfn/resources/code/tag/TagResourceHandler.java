package com.gitlab.aws.cfn.resources.code.tag;

import com.gitlab.aws.cfn.resources.shared.AbstractGitlabCombinedResourceHandler;
import com.gitlab.aws.cfn.resources.shared.GitLabUtils;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Tag;
import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class TagResourceHandler extends AbstractGitlabCombinedResourceHandler<TagResourceHandler,Tag,Pair<Integer,String>, ResourceModel,CallbackContext,TypeConfigurationModel> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TagResourceHandler.class);

    public static class BaseHandlerAdapter extends BaseHandler<CallbackContext,TypeConfigurationModel> implements BaseHandlerAdapterDefault<TagResourceHandler, ResourceModel,CallbackContext,TypeConfigurationModel> {
        @Override public ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, Logger logger, TypeConfigurationModel typeConfiguration) {
            return BaseHandlerAdapterDefault.super.handleRequest(proxy, request, callbackContext, logger, typeConfiguration);
        }

        @Override
        public TagResourceHandler newCombinedHandler() {
            return new TagResourceHandler();
        }
    }

    @Override
    protected GitLabApi newGitLabApiFromTypeConfiguration(TypeConfigurationModel typeModel) {
        return new GitLabApi(firstNonBlank(typeModel.getGitLabAccess().getUrl(), DEFAULT_URL), typeModel.getGitLabAccess().getAccessToken());
    }

    @Override
    public TagHelper newHelper() {
        return new TagHelper();
    }

    public class TagHelper extends Helper {
        @Override
        public Pair<Integer,String> getId(ResourceModel model) {
            return GitLabUtils.pair(model.getProjectId(), model.getName());
        }

        @Override
        public Optional<Tag> findExistingItemWithNonNullId(Pair<Integer,String> id) throws Exception {
            return gitlab.getTagsApi().getOptionalTag(id.getLeft(), id.getRight());
        }

        @Override
        public List<Tag> readExistingItems() throws GitLabApiException {
            if (model==null || model.getProjectId()==null) return Collections.emptyList();
            return gitlab.getTagsApi().getTags(model.getProjectId());
        }

        @Override
        public void deleteItem(Tag item) throws GitLabApiException {
            gitlab.getTagsApi().deleteTag(model.getProjectId(), item.getName());
        }

        @Override
        public ResourceModel modelFromItem(Tag item) {
            ResourceModel m = new ResourceModel();
            m.setName(item.getName());
            m.setProjectId(model.getProjectId());
            m.setRef(item.getCommit().getId());
            m.setMessage(item.getMessage());
            m.setTagId(model.getProjectId()+"-"+item.getName());
            return m;
        }

        @Override
        public Tag createItem() throws GitLabApiException {
            return gitlab.getTagsApi().createTag(model.getProjectId(), model.getName(), model.getRef(), model.getMessage(), (String)null);
        }

        @Override
        public void updateItem(Tag existingItem, List<String> updates) throws GitLabApiException {
            if (!Objects.equals(model.getMessage(), existingItem.getMessage())) {
                updates.add("Message");
            }
            if (!Objects.equals(model.getRef(), existingItem.getCommit().getId())) {
                updates.add("Ref");
            }
            if (!updates.isEmpty()) {
                // there is no update API
                deleteItem(existingItem);
                createItem();
            }
        }
    }

}
