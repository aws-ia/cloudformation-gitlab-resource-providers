package com.gitlab.aws.cfn.resources.projects.member.group;

import com.gitlab.aws.cfn.resources.shared.AbstractResourceCrudlLiveTest;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.Pager;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.GroupParams;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProjectSharedGroup;
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
public class GroupAccessToProjectCrudlLiveTest extends AbstractResourceCrudlLiveTest<GroupAccessToProjectResourceHandler,ProjectSharedGroup, Pair<Integer,Integer>, ResourceModel,CallbackContext,TypeConfigurationModel> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GroupAccessToProjectCrudlLiveTest.class);

    Group newGroup = null;
    Project newProject = null;

    @Override
    protected TypeConfigurationModel newTypeConfiguration() {
        return TypeConfigurationModel.builder()
                .gitLabAccess(GitLabAccess.builder().accessToken(getAccessTokenForTests()).build())
                .build();
    }

    protected ResourceModel newModelForCreate() {
        return ResourceModel.builder().projectId(newProject.getId()).groupId(newGroup.getId()).accessLevel("Developer").build();
    }

    @Override
    protected HandlerWrapper newHandlerWrapper() {
        return new HandlerWrapper();
    }

    @Test @Order(10)
    public void testCreate() throws Exception {
        Pager<Group> groups = gitlab.getGroupApi().getGroups(5);
        if (groups.current().isEmpty()) throw new IllegalStateException("Test requires at least one group already defined. (GitLab does not allow creating top-level groups.)");

        GroupParams params = new GroupParams()
                .withName(TEST_PREFIX+"-group-" + TEST_ID)
                .withPath(TEST_PREFIX+"-path-" + TEST_ID)
                .withParentId(groups.current().iterator().next().getId());
        newGroup = gitlab.getGroupApi().createGroup(params);

        newProject = gitlab.getProjectApi().createProject(TEST_PREFIX+"-project-" + TEST_ID);

        super.testCreate();

        assertThat(model.getMembershipId()).matches(s -> s.contains("" + newProject.getId()));
        assertThat(model.getMembershipId()).matches(s -> s.contains("" + newGroup.getId()));
        assertThat(model.getAccessLevel()).isEqualTo("Developer");

        assertThat(gitlab.getProjectApi().getProject(model.getProjectId()).getSharedWithGroups())
                .filteredOn(share -> model.getGroupId().equals(share.getGroupId()))
                .hasSize(1)
                .allMatch(share -> share.getGroupAccessLevel().equals(AccessLevel.DEVELOPER));
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

        assertThat(gitlab.getProjectApi().getProject(model.getProjectId()).getSharedWithGroups())
                .filteredOn(share -> model.getGroupId().equals(share.getGroupId()))
                .hasSize(1)
                .allMatch(share -> share.getGroupAccessLevel().equals(AccessLevel.REPORTER));
    }

    @Override @AfterAll
    public void tearDown() {
        try {
            if (newProject!=null) gitlab.getProjectApi().deleteProject(newProject.getId());
            if (newGroup!=null) gitlab.getGroupApi().deleteGroup(newGroup.getId());
        } catch (GitLabApiException e) {
            LOG.error("Error during cleanup (ignoring, probably test failed and that is more interesting): "+e, e);
        }
    }

}
