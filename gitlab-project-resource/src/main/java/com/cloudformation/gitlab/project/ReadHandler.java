package com.cloudformation.gitlab.project;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Objects;

public class ReadHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        ProgressEvent<ResourceModel, CallbackContext> pe;

        setGitLabApi(model);

        // check api connection
        pe = checkApiConnection(model);
        if (!pe.getStatus().equals(OperationStatus.SUCCESS)){
            // api error
            logger.log(String.format("Can't connect to the API with given credentials: %s, authentication token: %s",
                    model.getServer(), model.getToken()));
            return pe;
        }

        // check name supplied
        pe = checkNameSupplied(model);
        if (!pe.getStatus().equals(OperationStatus.SUCCESS)){
            logger.log("Name not supplied");
            return pe;
        }

        // get all projects
        pe = fetchAllProjects(model);
        if (!pe.getStatus().equals(OperationStatus.SUCCESS)){
            logger.log("Project fetching error");
            return pe;
        }

        // check if project already exists
        pe = checkProjectExists(model);
        if (!pe.getStatus().equals(OperationStatus.SUCCESS)){
            logger.log("Project does NOT exist");
            return pe;
        }

        // get project summary
        pe = getProjectSummary(model);
        if (!pe.getStatus().equals(OperationStatus.SUCCESS)){
            logger.log("Error getting summary");
            return pe;
        }

        return pe;
    }
}
