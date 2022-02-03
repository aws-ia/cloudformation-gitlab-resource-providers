package com.cloudformation.gitlab.project;

import com.cloudformation.gitlab.core.GitLabProjectService;
import org.junit.jupiter.api.Assertions;
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
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

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
        // no name supplied, so no project manipulation attempt but successful connection is still tested
        final ResourceModel model = ResourceModel.builder()
                .server("https://gitlab.com")
                .token("glpat-5YPGKq-7gtk5R3GA6stH")
                .build();

        GitLabProjectService service = new GitLabProjectService(model.getServer(), model.getToken());
        Assertions.assertTrue(service.verifyConnection());

    }

    @Test
    public void handleRequest_ApiNoServerFailure() {
        final ResourceModel model = ResourceModel.builder()
                .token("glpat-5YPGKq-7gtk5R3GA6stH")
                .build();
        testApiFailure(model);
    }

    @Test
    public void handleRequest_ApiNoTokenFailure() {
        final ResourceModel model = ResourceModel.builder()
                .server("https://gitlab.com")
                .build();
        testApiFailure(model);
    }

    @Test
    public void handleRequest_IncorrectDetailsFailure() {
        final ResourceModel model = ResourceModel.builder()
                .server("jfhwbeiufbweuf.hsb")
                .token("glpgerat-gegreg-gggerewrgergegreg")
                .build();
        testApiFailure(model);
    }

    public void testApiFailure(ResourceModel model) {
        try{
            GitLabProjectService service = new GitLabProjectService(model.getServer(), model.getToken());
            Assertions.assertFalse(service.verifyConnection());
        } catch (Exception e){
            logger.log("Error creating service: " + e);
        }
    }

    // project creation tests
    @Test
    public void handleRequest_ProjectCreatedSuccess() {
        final CreateHandler handler = new CreateHandler();

        Instant instant = Instant.now();
        final ResourceModel model = ResourceModel.builder()
                .name("test-project-" + instant.toEpochMilli())
                .server("https://gitlab.com")
                .token("glpat-5YPGKq-7gtk5R3GA6stH")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        // delete the project created
        cleanup(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_ProjectExistsFailure() {
        final CreateHandler handler = new CreateHandler();

        Instant instant = Instant.now();
        final ResourceModel model = ResourceModel.builder()
                .name("test-project-" + instant.toEpochMilli())
                .server("https://gitlab.com")
                .token("glpat-5YPGKq-7gtk5R3GA6stH")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        // create project - OK
        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        // try to create again - failure
        final ProgressEvent<ResourceModel, CallbackContext> next_response
                = handler.handleRequest(proxy, request, null, logger);

        // delete the project created
        cleanup(request);

        assertThat(next_response).isNotNull();
        assertThat(next_response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(next_response.getCallbackContext()).isNull();
        assertThat(next_response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(next_response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(next_response.getResourceModels()).isNull();
        assertThat(next_response.getMessage()).isNull();
        assertThat(next_response.getErrorCode()).isEqualTo(HandlerErrorCode.InternalFailure);
    }

    public void cleanup(ResourceHandlerRequest<ResourceModel> request){
        DeleteHandler handler = new DeleteHandler();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    }
}

