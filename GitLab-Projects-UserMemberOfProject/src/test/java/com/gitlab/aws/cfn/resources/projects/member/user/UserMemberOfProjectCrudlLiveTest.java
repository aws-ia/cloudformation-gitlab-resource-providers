package com.gitlab.aws.cfn.resources.projects.member.user;

import com.gitlab.aws.cfn.resources.shared.AbstractResourceCrudlLiveTest;
import org.apache.commons.lang3.tuple.Pair;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Member;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@Tag("Live")
public class UserMemberOfProjectCrudlLiveTest extends AbstractResourceCrudlLiveTest
        <UserMemberOfProjectResourceHandler, Member, Pair<Integer,Integer>, ResourceModel,CallbackContext,TypeConfigurationModel> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(UserMemberOfProjectCrudlLiveTest.class);

    final static Integer USER_ID = Integer.parseInt(getEnvOrFile("user_id", "gitlab user ID corresponding to access_token"));
    final static Integer USER_ID_TO_ADD = Integer.parseInt(getEnvOrFile("user_id_to_add", "gitlab user ID to add to group (must exist as cannot create user via API, and must not be the group owner)"));
    final static String USERNAME_TO_ADD = getEnvOrFile("username_to_add", "gitlab username to add to group (should match user_id)");

    Project newProject = null;

    @Override
    protected TypeConfigurationModel newTypeConfiguration() {
        return TypeConfigurationModel.builder()
                .gitLabAccess(GitLabAccess.builder().accessToken(getAccessTokenForTests()).build())
                .build();
    }

    protected ResourceModel newModelForCreate() throws Exception {
        if (newProject==null) {
            newProject = gitlab.getProjectApi().createProject(TEST_PREFIX+"-project-" + TEST_ID);
        }

        return ResourceModel.builder().projectId(newProject.getId()).userId(USER_ID_TO_ADD).accessLevel("Developer").build();
    }

    @Override
    protected HandlerWrapper newHandlerWrapper() {
        return new HandlerWrapper();
    }

    @Test @Order(10)
    public void testCreate() throws Exception {
        super.testCreate();

        assertThat(model.getMembershipId()).matches(s -> s.contains("" + newProject.getId()));
        assertThat(model.getMembershipId()).matches(s -> s.contains("" + USER_ID_TO_ADD));
        assertThat(model.getAccessLevel()).isEqualTo("Developer");
        assertThat(model.getUsername()).isEqualTo(USERNAME_TO_ADD);

        assertThat(gitlab.getProjectApi().getMembers(model.getProjectId()))
                .filteredOn(member -> member.getId().equals(USER_ID_TO_ADD))
                .hasSize(1)
                .allMatch(member -> member.getAccessLevel().equals(AccessLevel.DEVELOPER));
    }

    @Test @Order(41)
    public void testUpdateChangeAccessLevel() throws GitLabApiException {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        model.setAccessLevel("Reporter");
        ProgressEvent<ResourceModel, CallbackContext> response = new UpdateHandler().handleRequest(proxy, newRequestObject(), null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getMembershipId()).isEqualTo(model.getMembershipId());
        assertThat(response.getResourceModel().getAccessLevel()).isEqualTo("Reporter");

        assertThat(gitlab.getProjectApi().getMembers(model.getProjectId()))
                .filteredOn(member -> member.getId().equals(USER_ID_TO_ADD))
                .hasSize(1)
                .allMatch(member -> member.getAccessLevel().equals(AccessLevel.REPORTER));
    }

    @Test @Order(100)
    public void testCreateWithUsername() throws GitLabApiException {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        model = ResourceModel.builder().projectId(newProject.getId()).username(USERNAME_TO_ADD).accessLevel("Developer").build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = new CreateHandler().handleRequest(proxy, newRequestObject(), null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).describedAs("Create failed; code %s, message %s.", response.getErrorCode(), response.getMessage()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getUserId()).isEqualTo(USER_ID_TO_ADD);
        assertThat(response.getResourceModel().getMembershipId()).matches(s -> s.contains("" + newProject.getId()));
        assertThat(response.getResourceModel().getMembershipId()).matches(s -> s.contains("" + USER_ID_TO_ADD));
        assertThat(response.getResourceModel().getAccessLevel()).isEqualTo("Developer");

        assertThat(gitlab.getProjectApi().getMembers(model.getProjectId()))
                .filteredOn(member -> member.getId().equals(USER_ID_TO_ADD))
                .hasSize(1)
                .allMatch(member -> member.getAccessLevel().equals(AccessLevel.DEVELOPER));
    }

    @Test @Order(110)
    public void testDeleteUsername() throws GitLabApiException {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        ProgressEvent<ResourceModel, CallbackContext> response = new DeleteHandler().handleRequest(proxy, newRequestObject(), null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        // will typically have 1 owner afterwards
        assertThat(gitlab.getProjectApi().getMembers(model.getProjectId()))
                .allMatch(member -> member.getId().equals(USER_ID))
                .hasSize(1);
    }

    @Test @Order(200)
    public void testCreateWithBadUsername() throws GitLabApiException {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        model = ResourceModel.builder().projectId(newProject.getId()).username("wrong_username").userId(USER_ID_TO_ADD).accessLevel("Developer").build();

        LOG.info("Expecting ERROR as part of this test (following error can probably be ignored)");
        final ProgressEvent<ResourceModel, CallbackContext> response
                = new CreateHandler().handleRequest(proxy, newRequestObject(), null, logger, typeConfiguration);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
    }

    @Override @AfterAll
    public void tearDown() {
        try {
            if (newProject!=null) gitlab.getProjectApi().deleteProject(newProject.getId());
        } catch (GitLabApiException e) {
            LOG.error("Error during cleanup (ignoring, probably test failed and that is more interesting): "+e, e);
        }
    }

}
