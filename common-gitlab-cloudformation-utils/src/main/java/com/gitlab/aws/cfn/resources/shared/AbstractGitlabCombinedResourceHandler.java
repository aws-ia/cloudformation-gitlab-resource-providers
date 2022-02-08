package com.gitlab.aws.cfn.resources.shared;

import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApi;

public abstract class AbstractGitlabCombinedResourceHandler<ResourceModel, CallbackContext, TypeConfigurationModel, This extends AbstractGitlabCombinedResourceHandler<ResourceModel, CallbackContext, TypeConfigurationModel, This>>
        extends AbstractCombinedResourceHandler<ResourceModel, CallbackContext, TypeConfigurationModel, This> {

    public final static String DEFAULT_URL = "https://gitlab.com/";

    protected GitLabApi gitlab;

    @Override
    protected void doInit() throws Exception {
        if (gitlab==null) {
            setGitLabApi(newGitLabApiFromTypeConfiguration(typeConfiguration));
        }
    }
    
    protected abstract GitLabApi newGitLabApiFromTypeConfiguration(TypeConfigurationModel typeModel);

    protected String firstNonBlank(String ...args) {
        for (String arg: args) {
            if (StringUtils.isNotBlank(arg)) return arg;
        }
        return null;
    }

    public void setGitLabApi(GitLabApi gitLabApi) {
        this.gitlab = gitLabApi;
    }

}
