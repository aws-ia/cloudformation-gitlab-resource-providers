package com.cloudformation.gitlab.project;

import com.cloudformation.gitlab.core.GitLabProjectService;
import com.cloudformation.gitlab.core.GitLabServiceException;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class UpdateHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        GitLabProjectService gitLabService = initGitLabService(model.getServer(),model.getToken());
        final Map<String,Object> modelMap = translateResourceModelToMap(model);

        try {
            gitLabService.update(modelMap);
        } catch (GitLabServiceException e){
            logger.log("Error: " + e.getMessage());
            return failure(model,HandlerErrorCode.InternalFailure);
        }
        return success(model);
    }
}
