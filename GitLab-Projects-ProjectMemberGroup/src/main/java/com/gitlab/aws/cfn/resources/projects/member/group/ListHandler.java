package com.gitlab.aws.cfn.resources.projects.member.group;

import java.util.stream.Collectors;
import org.gitlab4j.api.models.ProjectSharedGroup;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;

import java.util.List;

public class ListHandler extends BaseHandlerResource {

    @Override
    protected void handleRequest() throws Exception {
        List<ResourceModel> groupShares = gitlab.getProjectApi().getProject(model.getProjectId()).getSharedWithGroups().stream().map(this::newModel).collect(Collectors.toList());

        result = ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(groupShares)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    protected ResourceModel newModel(ProjectSharedGroup g) {
        ResourceModel m = new ResourceModel();
        m.setProjectId(model.getProjectId());
        m.setGroupId(""+g.getGroupId());
        initMembershipId(m);
        return m;
    }

}
