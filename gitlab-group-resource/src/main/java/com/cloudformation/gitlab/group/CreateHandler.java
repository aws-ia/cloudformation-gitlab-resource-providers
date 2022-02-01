package com.cloudformation.gitlab.group;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.GroupParams;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        logger.log("CreateHandler.model: " + model);

        GitLabApi gitLabApi = new GitLabApi(model.getHostURL(), model.getAccessToken());

        GroupParams gp = new GroupParams()
                .withName(model.getGroupName())
                .withPath(model.getPath())
                .withParentId(model.getParentId());
        Group newGroup;
        try {
            newGroup = gitLabApi.getGroupApi().createGroup(gp);
            model.setUID(String.valueOf(newGroup.getId()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
