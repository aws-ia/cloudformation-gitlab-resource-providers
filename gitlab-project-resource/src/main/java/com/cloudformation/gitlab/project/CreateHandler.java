package com.cloudformation.gitlab.project;

import com.cloudformation.gitlab.core.GitLabProjectService;
import com.cloudformation.gitlab.core.GitLabServiceException;
import org.gitlab4j.api.models.Project;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Map;

public class CreateHandler extends BaseHandlerStd {

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
            Project project = gitLabService.create(modelMap);
            model.setId(project.getId());
        } catch (GitLabServiceException e){
            logger.log("Error: "+e);
            return failure(model,HandlerErrorCode.InternalFailure);
        }
        return success(model);
    }
}
