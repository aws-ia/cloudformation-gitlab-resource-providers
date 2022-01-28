package com.cloudformation.gitlab.project;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Objects;

public class CreateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        // create project using cli
        GitLabApi gitLabApi;
        try{
            if (Objects.isNull(model.getServer())){
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .resourceModel(model)
                        .status(OperationStatus.SUCCESS)
                        .build();
            } else if (!Objects.isNull(model.getToken())){
                gitLabApi = new GitLabApi(model.getServer(), model.getToken());
            } else {
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .resourceModel(model)
                        .status(OperationStatus.SUCCESS)
                        .build();
            }
            if (!Objects.isNull(model.getName())){
                Project projectSpec = new Project()
                        .withName(model.getName())
                        .withDescription("Test description.")
                        .withIssuesEnabled(true)
                        .withMergeRequestsEnabled(true)
                        .withWikiEnabled(true)
                        .withSnippetsEnabled(true)
                        .withPublic(true);
                Project newProject = gitLabApi.getProjectApi().createProject(projectSpec);
                model.setID(newProject.getId());
            }

        } catch (Exception e){
            logger.log("There was an error: " + e);
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.SUCCESS)
                    .build();
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
