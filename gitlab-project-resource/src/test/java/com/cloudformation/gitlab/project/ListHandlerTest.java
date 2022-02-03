package com.cloudformation.gitlab.project;

import com.cloudformation.gitlab.core.GitLabProjectService;
import org.gitlab4j.api.models.Project;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {

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

    // list tests
    @Test
    public void handleRequest_ListSuccess() {
        final ListHandler handler = new ListHandler();

        Instant instant = Instant.now();
        final ResourceModel model = ResourceModel.builder()
                .name("test-project-" + instant.toEpochMilli())
                .server("https://gitlab.com")
                .token("glpat-5YPGKq-7gtk5R3GA6stH")
                .build();

        handler.setGitLabApi(model);
        assertThat(handler.fetchAllProjects(model).getStatus()).isEqualTo(OperationStatus.SUCCESS);
        List<Project> projects = handler.getAllProjects();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        assertThat(response.getResourceModels().size()).isEqualTo(projects.size());
    }

    @Test
    public void handleRequest_ListAddOneSuccess() {
        final ListHandler handler = new ListHandler();

        Instant instant = Instant.now();
        final ResourceModel model = ResourceModel.builder()
                .name("test-project-" + instant.toEpochMilli())
                .server("https://gitlab.com")
                .token("glpat-5YPGKq-7gtk5R3GA6stH")
                .build();

        handler.setGitLabApi(model);
        assertThat(handler.fetchAllProjects(model).getStatus()).isEqualTo(OperationStatus.SUCCESS);
        List<Project> projects = handler.getAllProjects();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        assertThat(response.getResourceModels().size()).isEqualTo(projects.size());

        // add a new project
        prepare(request);

        final ProgressEvent<ResourceModel, CallbackContext> nextResponse
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(nextResponse).isNotNull();
        assertThat(nextResponse.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(nextResponse.getCallbackContext()).isNull();
        assertThat(nextResponse.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(nextResponse.getResourceModels()).isNotNull();
        assertThat(nextResponse.getMessage()).isNull();
        assertThat(nextResponse.getErrorCode()).isNull();

        assertThat(nextResponse.getResourceModels().size()).isEqualTo(projects.size()+1);

        // cleanup
        cleanup(request);
    }

    public void prepare(ResourceHandlerRequest<ResourceModel> request){
        CreateHandler handler = new CreateHandler();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    }

    public void cleanup(ResourceHandlerRequest<ResourceModel> request){
        DeleteHandler handler = new DeleteHandler();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
    }
}
