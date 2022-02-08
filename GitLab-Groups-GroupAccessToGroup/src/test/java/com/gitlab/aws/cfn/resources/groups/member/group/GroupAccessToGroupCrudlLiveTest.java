package com.gitlab.aws.cfn.resources.groups.member.group;

import com.gitlab.aws.cfn.resources.groups.member.group.GroupAccessToGroupResourceHandler.GroupMember;
import com.gitlab.aws.cfn.resources.shared.AbstractResourceCrudlLiveTest;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.Pager;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.GroupParams;
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
public class GroupAccessToGroupCrudlLiveTest extends AbstractResourceCrudlLiveTest<GroupMember,ResourceModel,CallbackContext,TypeConfigurationModel> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GroupAccessToGroupCrudlLiveTest.class);

    Group newGroupShared = null;
    Group newGroupSharedWith = null;

    @Override
    protected TypeConfigurationModel newTypeConfiguration() {
        return TypeConfigurationModel.builder()
                .gitLabAccess(GitLabAccess.builder().accessToken(getAccessTokenForTests()).build())
                .build();
    }

    protected ResourceModel newModelForCreate() {
        return ResourceModel.builder().sharedGroupId(newGroupShared.getId()).sharedWithGroupId(newGroupSharedWith.getId()).accessLevel("Developer").build();
    }

    @Override
    protected HandlerWrapper newHandlerWrapper() {
        return new HandlerWrapper();
    }

    @Override
    protected GroupMember getRealItem() throws Exception {
        return GroupAccessToGroupResourceHandler.getSharedWithGroups(gitlab, model.getSharedGroupId())
                .stream().filter(g -> g.groupId.equals(model.getSharedWithGroupId())).findAny().get();
    }

    @Override
    protected Optional<GroupMember> getRealItem(GroupMember item) throws Exception {
        return GroupAccessToGroupResourceHandler.getSharedWithGroups(gitlab, model.getSharedGroupId())
                .stream().filter(g -> g.groupId.equals(item.groupId)).findAny();
    }

    @Test @Order(10)
    public void testCreate() throws Exception {
        Pager<Group> groups = gitlab.getGroupApi().getGroups(5);
        if (groups.current().isEmpty()) throw new IllegalStateException("Test requires at least one group already defined. (GitLab does not allow creating top-level groups.)");

        GroupParams params1 = new GroupParams()
                .withName(TEST_PREFIX+"-group-shared-" + TEST_ID)
                .withPath(TEST_PREFIX+"-path-shared-" + TEST_ID)
                .withParentId(groups.current().iterator().next().getId());
        newGroupShared = gitlab.getGroupApi().createGroup(params1);

        GroupParams params2 = new GroupParams()
                .withName(TEST_PREFIX+"-group-shared-with-" + TEST_ID)
                .withPath(TEST_PREFIX+"-path-shared-with-" + TEST_ID)
                .withParentId(groups.current().iterator().next().getId());
        newGroupSharedWith = gitlab.getGroupApi().createGroup(params2);

        super.testCreate();

        assertThat(model.getMembershipId()).matches(s -> s.contains("" + newGroupShared.getId()));
        assertThat(model.getMembershipId()).matches(s -> s.contains("" + newGroupSharedWith.getId()));
        assertThat(model.getAccessLevel()).isEqualTo("Developer");

        assertThat(GroupAccessToGroupResourceHandler.getSharedWithGroups(gitlab, model.getSharedGroupId()))
                .filteredOn(member -> member.groupId.equals(model.getSharedWithGroupId()))
                .hasSize(1)
                .allMatch(member -> member.getAccessLevel().equals(AccessLevel.DEVELOPER));
    }

    @Test @Order(41)
    public void testUpdateChangeAccessLevel() throws GitLabApiException {
        if (model==null) fail("Create test must succeed for this to be meaningful.");

        model.setAccessLevel("Reporter");
        ProgressEvent<ResourceModel, CallbackContext> response = new UpdateHandler().handleRequest(proxy, newRequestObject(), null, logger, typeConfiguration);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).describedAs("Create failed; code %s, message %s.", response.getErrorCode(), response.getMessage()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getMembershipId()).isEqualTo(model.getMembershipId());
        assertThat(response.getResourceModel().getAccessLevel()).isEqualTo("Reporter");

        assertThat(GroupAccessToGroupResourceHandler.getSharedWithGroups(gitlab, model.getSharedGroupId()))
                .filteredOn(member -> member.groupId.equals(model.getSharedWithGroupId()))
                .hasSize(1)
                .allMatch(member -> member.getAccessLevel().equals(AccessLevel.REPORTER));
    }

    @Override
    @AfterAll
    public void tearDown() {
        try {
            if (newGroupShared!=null) gitlab.getGroupApi().deleteGroup(newGroupShared.getId());
            if (newGroupSharedWith!=null) gitlab.getGroupApi().deleteGroup(newGroupSharedWith.getId());
        } catch (GitLabApiException e) {
            LOG.error("Error during cleanup (ignoring, probably test failed and that is more interesting): "+e, e);
        }
    }

}
