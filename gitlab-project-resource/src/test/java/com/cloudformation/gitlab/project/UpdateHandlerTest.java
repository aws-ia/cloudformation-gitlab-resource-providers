package com.cloudformation.gitlab.project;

import com.cloudformation.gitlab.core.GitLabProjectService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    // API connection tests
    @Test
    public void handleRequest_ConnectionSuccess() {
        final Credentials credentials = new Credentials("https://gitlab.com","glpat-5YPGKq-7gtk5R3GA6stH");
        GitLabProjectService service = new GitLabProjectService(credentials.getHostUrl(), credentials.getAuthToken());
        Assertions.assertTrue(service.verifyConnection());
    }

    @Test
    public void handleRequest_ConnectionFailure() {
        final Credentials credentials = new Credentials("https://gitlab.com","incorrect token");
        GitLabProjectService service = new GitLabProjectService(credentials.getHostUrl(), credentials.getAuthToken());
        Assertions.assertFalse(service.verifyConnection());
    }


    // project update tests
    @Test
    public void handleRequest_ProjectUpdatedSuccess(){
        final UpdateHandler handler = new UpdateHandler();

        Instant instant = Instant.now();
        final ResourceModel model = ResourceModel.builder()
                .name("test-project-" + instant.toEpochMilli())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final Credentials credentials = new Credentials("https://gitlab.com","glpat-5YPGKq-7gtk5R3GA6stH");
        final TypeConfigurationModel tcm = TypeConfigurationModel.builder().gitLabAuthentication(credentials).build();

        // create the project
        prepare(request, tcm);

        //update the project
        final ResourceModel newModel = ResourceModel.builder()
                .id(model.getId())
                .name(model.getName() + "-changed")
                .build();

        final ResourceHandlerRequest<ResourceModel> newRequest = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(newModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, newRequest, null, logger, tcm);

        // delete the project created
        cleanup(newRequest,tcm);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(newRequest.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }


    @Test
    public void handleRequest_ProjectNotUpdatedFailure(){
        final UpdateHandler handler = new UpdateHandler();

        Instant instant = Instant.now();
        final ResourceModel model = ResourceModel.builder()
                .name("test-project-" + instant.toEpochMilli())
                .build();

        final Credentials credentials = new Credentials("https://gitlab.com","glpat-5YPGKq-7gtk5R3GA6stH");
        final TypeConfigurationModel tcm = TypeConfigurationModel.builder().gitLabAuthentication(credentials).build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        // create the project
        prepare(request, tcm);

        //update the project
        final ResourceModel newModel = ResourceModel.builder()
                .id(model.getId())
                .name(model.getName() + " - changed") // spaces not allowed
                .build();

        final ResourceHandlerRequest<ResourceModel> newRequest = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(newModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, newRequest, null, logger, tcm);

        // delete the project created
        cleanup(newRequest, tcm);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(newRequest.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InternalFailure);
    }

    public void prepare(ResourceHandlerRequest<ResourceModel> request, TypeConfigurationModel tcm){
        CreateHandler handler = new CreateHandler();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger, tcm);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    }

    public void cleanup(ResourceHandlerRequest<ResourceModel> request, TypeConfigurationModel tcm){
        DeleteHandler handler = new DeleteHandler();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger, tcm);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    }
}
