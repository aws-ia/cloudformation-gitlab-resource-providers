package com.cloudformation.gitlab.group;

import com.cloudformation.gitlab.core.GitLabService;
import org.gitlab4j.api.GitLabApi;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        GitLabApi gitLabApi = new GitLabApi(model.getHostURL(), model.getAccessToken());
        GitLabService core;
        try {
//            gitLabApi.getGroupApi().updateGroup()
//            gitLabApi.getGroupApi().deleteGroup(Integer.valueOf(model.getUID()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
