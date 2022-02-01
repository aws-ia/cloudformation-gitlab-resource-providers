package com.cloudformation.gitlab.project;

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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateHandler extends BaseHandlerStd {

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
        if (pe.getStatus().equals(OperationStatus.SUCCESS)){
            // if exists, it's bad
            logger.log("Project already exists");
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.AlreadyExists)
                    .build();
        }

        // create new project
        pe = createProject(model);
        if (!pe.getStatus().equals(OperationStatus.SUCCESS)){
            logger.log("Project creation error");
            return pe;
        }

        return pe;
    }
}
