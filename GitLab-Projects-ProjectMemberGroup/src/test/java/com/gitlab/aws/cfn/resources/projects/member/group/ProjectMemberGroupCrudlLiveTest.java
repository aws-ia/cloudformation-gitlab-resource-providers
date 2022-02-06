package com.gitlab.aws.cfn.resources.projects.member.group;

import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.Pager;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.GroupParams;
import org.gitlab4j.api.models.Project;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@Tag("Live")
public class ProjectMemberGroupCrudlLiveTest extends GitLabLiveTestSupport {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProjectMemberGroupCrudlLiveTest.class);

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    GitLabApi gitlab;
    TypeConfigurationModel typeConfiguration;
    ResourceModel model;
    ResourceHandlerRequest<ResourceModel> request;

    final String TEST_ID = UUID.randomUUID().toString();

    Group newGroup = null;
    Project newProject = null;

    @Test @Order(0)
    public void testCreate() throws GitLabApiException {
        gitlab = new GitLabApi("https://gitlab.com", getAccessTokenForTests());

        Pager<Group> groups = gitlab.getGroupApi().getGroups(5);
        if (groups.current().isEmpty()) throw new IllegalStateException("Test requires at least one group already defined. (GitLab does not allow creating top-level groups.)");

        GroupParams params = new GroupParams()
                .withName(TEST_PREFIX+"-group-" + TEST_ID)
                .withPath(TEST_PREFIX+"-path-" + TEST_ID)
                .withParentId(groups.current().iterator().next().getId());
        newGroup = gitlab.getGroupApi().createGroup(params);

        newProject = gitlab.getProjectApi().createProject(TEST_PREFIX+"-project-" + TEST_ID);

        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        typeConfiguration = TypeConfigurationModel.builder()
                .gitLabAccess(GitLabAccess.builder().accessToken(getAccessTokenForTests()).build())
                .build();
        model = ResourceModel.builder().projectId(newProject.getId()).groupId(newGroup.getId()).accessLevel("Developer").build();
        request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = new CreateHandler().handleRequest(proxy, request, null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).describedAs("Create failed; message %s.", response.getMessage()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getMembershipId()).matches(s -> s.contains("" + newProject.getId()));
        assertThat(response.getResourceModel().getMembershipId()).matches(s -> s.contains("" + newGroup.getId()));
        assertThat(response.getResourceModel().getAccessLevel()).isEqualTo("Developer");
        assertThat(response.getErrorCode()).isNull();

        assertThat(gitlab.getProjectApi().getProject(model.getProjectId()).getSharedWithGroups())
                .filteredOn(share -> model.getGroupId().equals(share.getGroupId()))
                .hasSize(1)
                .allMatch(share -> share.getGroupAccessLevel().equals(AccessLevel.DEVELOPER));
    }

    @Test @Order(1)
    public void testRead() {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        ProgressEvent<ResourceModel, CallbackContext> response = new ReadHandler().handleRequest(proxy, request, null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getMembershipId()).isEqualTo(model.getMembershipId());
    }

    @Test @Order(2)
    public void testList() {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        // no op
        ProgressEvent<ResourceModel, CallbackContext> response = new ListHandler().handleRequest(proxy, request, null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModels()).anyMatch(m -> m.getMembershipId().equals(model.getMembershipId()));
    }

    @Test @Order(3)
    public void testUpdateNoChange() {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        // no op
        ProgressEvent<ResourceModel, CallbackContext> response = new UpdateHandler().handleRequest(proxy, request, null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getMembershipId()).isEqualTo(model.getMembershipId());
    }

    @Test @Order(4)
    public void testUpdateChangeAccessLevel() throws GitLabApiException {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        model.setAccessLevel("Reporter");
        ProgressEvent<ResourceModel, CallbackContext> response = new UpdateHandler().handleRequest(proxy, request, null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getMembershipId()).isEqualTo(model.getMembershipId());
        assertThat(response.getResourceModel().getAccessLevel()).isEqualTo("Reporter");

        assertThat(gitlab.getProjectApi().getProject(model.getProjectId()).getSharedWithGroups())
                .filteredOn(share -> model.getGroupId().equals(share.getGroupId()))
                .hasSize(1)
                .allSatisfy(share -> share.getGroupAccessLevel().equals(AccessLevel.REPORTER));
    }

    @Test @Order(5)
    public void testDelete() throws GitLabApiException {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        ProgressEvent<ResourceModel, CallbackContext> response = new DeleteHandler().handleRequest(proxy, request, null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getMembershipId()).isEqualTo(model.getMembershipId());

        assertThat(gitlab.getProjectApi().getProject(model.getProjectId()).getSharedWithGroups())
                .noneMatch(share -> model.getGroupId().equals(share.getGroupId()));
    }

    @AfterAll
    public void tearDown() {
        try {
            if (newProject!=null) gitlab.getProjectApi().deleteProject(newProject.getId());
            if (newGroup!=null) gitlab.getGroupApi().deleteGroup(newGroup.getId());
        } catch (GitLabApiException e) {
            LOG.error("Error during cleanup (ignoring, probably test failed and that is more interesting): "+e, e);
        }
    }


    public static void main(String[] args) throws GitLabApiException {
        int MAX = 10;

        // clean up
        GitLabApi gitlab = new GitLabApi("https://gitlab.com", getAccessTokenForTests());
        for (Group x : gitlab.getGroupApi().getGroups(MAX).current()) {
            if (x.getName().startsWith(TEST_PREFIX)) {
                LOG.info("Deleting leaked test item: "+x.getName()+" "+x);
                gitlab.getGroupApi().deleteGroup(x.getId());
            }
        }
        for (Project x : gitlab.getProjectApi().getOwnedProjects(MAX).current()) {
            if (x.getName().startsWith(TEST_PREFIX)) {
                LOG.info("Deleting leaked test item: "+x.getName()+" "+x);
                gitlab.getProjectApi().deleteProject(x.getId());
            }
        }
    }
}
