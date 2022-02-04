package com.cloudformation.gitlab.project;

import com.cloudformation.gitlab.core.GitLabProjectService;
import com.cloudformation.gitlab.core.GitLabServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Map;

public class UpdateHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger,
        final TypeConfigurationModel tcm) {

        final ResourceModel model = request.getDesiredResourceState();
        final Map<String,Object> modelMap = translateResourceModelToMap(model);

        Credentials creds = tcm.getGitLabAuthentication();
        final GitLabProjectService gitLabService = initGitLabService(creds.getHostUrl(),creds.getAuthToken());

        try {
            gitLabService.update(modelMap);
        } catch (GitLabServiceException e){
            logger.log("Error: " + e.getMessage());
            return failure(model,HandlerErrorCode.InternalFailure);
        }
        return success(model);
    }
}
