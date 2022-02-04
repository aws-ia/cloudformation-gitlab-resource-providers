package com.cloudformation.gitlab.project;

import com.cloudformation.gitlab.core.GitLabProjectService;
import org.gitlab4j.api.models.Project;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext, TypeConfigurationModel> {
    @Override
    public abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            Logger logger,
            TypeConfigurationModel tcm);

    // GitLab Service for API
    private GitLabProjectService gitLabService;

    protected GitLabProjectService initGitLabService(String url, String token){
        gitLabService = new GitLabProjectService(url, token);
        return gitLabService;
    }

    protected ProgressEvent<ResourceModel, CallbackContext> success(ResourceModel model){
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> success(List<ResourceModel> models){
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> failure(ResourceModel model, HandlerErrorCode errorCode){
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.FAILED)
                .errorCode(errorCode)
                .build();
    }

    protected ResourceModel translateProjectToResourceModel(Project project){
        return ResourceModel.builder()
                .name(project.getName())
                .id(project.getId())
                .build();
    }

    protected Map<String, Object> translateResourceModelToMap(ResourceModel model){
        Map<String, Object> modelMap = new HashMap<>();
        if (!Objects.isNull(model.getName())){
            modelMap.put("name", model.getName());
        }
        if (!Objects.isNull(model.getId())){
            modelMap.put("id", model.getId());
        }
        return modelMap;
    }

}
