package com.gitlab.aws.cfn.resources.shared;

import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApi;

public abstract class AbstractGitlabCombinedResourceHandler<
            This extends AbstractGitlabCombinedResourceHandler<This, ItemT, IdT, ResourceModelT, CallbackContextT, TypeConfigurationModelT>,
            ItemT, IdT, ResourceModelT, CallbackContextT, TypeConfigurationModelT>
        extends AbstractCombinedResourceHandler<This, ItemT, IdT, ResourceModelT, CallbackContextT, TypeConfigurationModelT> {

    public final static String DEFAULT_URL = "https://gitlab.com/";

    protected GitLabApi gitlab;

    @Override
    protected void doInit() throws Exception {
        if (gitlab==null) {
            setGitLabApi(newGitLabApiFromTypeConfiguration(typeConfiguration));
        }
    }
    
    protected abstract GitLabApi newGitLabApiFromTypeConfiguration(TypeConfigurationModelT typeModel);

    protected String firstNonBlank(String ...args) {
        for (String arg: args) {
            if (StringUtils.isNotBlank(arg)) return arg;
        }
        return null;
    }

    protected <T> T firstNonNull(T ...args) {
        for (T arg: args) {
            if (arg!=null) return arg;
        }
        return null;
    }

    public void setGitLabApi(GitLabApi gitLabApi) {
        this.gitlab = gitLabApi;
    }

}
