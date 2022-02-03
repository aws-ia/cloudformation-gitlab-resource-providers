package com.cloudformation.gitlab.project;

import com.cloudformation.gitlab.core.GitLabProjectService;
import com.cloudformation.gitlab.core.GitLabService;
import com.cloudformation.gitlab.core.GitLabServiceException;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.TimeLimiter;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final Map<String,Object> modelMap = translateResourceModelToMap(model);

        GitLabProjectService gitLabService = initGitLabService(model.getServer(),model.getToken());
        try {
            Project project = gitLabService.create(modelMap);
            model.setId(project.getId());
        } catch (GitLabServiceException e){
            logger.log("Error");
            return failure(model,HandlerErrorCode.InternalFailure);
        }
        return success(model);
    }
}
