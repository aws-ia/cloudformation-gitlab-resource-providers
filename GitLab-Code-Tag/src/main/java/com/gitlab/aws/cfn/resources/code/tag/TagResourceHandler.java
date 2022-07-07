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

    // if ref is a branch name, should we update the ref if e.g. a message is changed?  probably not i think.
    // (ref is no longer updateable in the json, for this reason!)
    public static final boolean ALLOW_REF_UPDATES = false;

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
            if (Objects.equals(model.getName(), m.getName()) && model.getRef()!=null) {
                // only set the reference if we are the same tag as the requested model; otherwise we don't know if the reference was a branch
                m.setRef(model.getRef());
            }
            m.setTagId(model.getProjectId()+"-"+item.getName());
            m.setMessage(item.getMessage());
            m.setCommitId(item.getCommit().getId());
            return m;
        }

        @Override
        public Tag createItem() throws GitLabApiException {
            if (model.getRef()==null) throw new IllegalArgumentException("Ref cannot be null when creating");
            return gitlab.getTagsApi().createTag(model.getProjectId(), model.getName(), model.getRef(), model.getMessage(), (String)null);
        }

        @Override
        public void updateItem(Tag existingItem, List<String> updates) throws GitLabApiException {
            if (!Objects.equals(model.getMessage(), existingItem.getMessage())) {
                updates.add("Message");
            }
            if (ALLOW_REF_UPDATES && model.getRef()!=null && !Objects.equals(model.getRef(), existingItem.getCommit().getId()) && !Objects.equals(model.getCommitId(), existingItem.getCommit().getId())) {
                // we could repoint the ref, if it has changed; but what guarantees are there that other model fields are set
                // also for now, ref is set in the json definition model as not updateable anyway
                updates.add("Ref");
            }
            if (!updates.isEmpty()) {
                String oldRef = model.getRef();
                if (model.getCommitId()!=null) {
                    // can we rely on commit ID being remembered??
                    model.setRef(model.getCommitId());
                }

                // there is no update API
                deleteItem(existingItem);
                createItem();

                model.setRef(oldRef);
            }
        }
    }

}
