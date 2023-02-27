#!/bin/bash
#
# Set up any prerequisites needed for cfn test
#
mkdir -p inputs
cat example_inputs/inputs_1_create.json | sed "s/GITLAB_PARENT_GROUP_ID/${GITLAB_PARENT_GROUP_ID}/g" | sed "s/GITLAB_SHARED_USER_ID/${GITLAB_SHARED_USER_ID}/g" | sed "s/GITLAB_SHARED_USER_NAME/${GITLAB_SHARED_USER_NAME}/g" > inputs/inputs_1_create.json
cat example_inputs/inputs_1_update.json | sed "s/GITLAB_PARENT_GROUP_ID/${GITLAB_PARENT_GROUP_ID}/g" | sed "s/GITLAB_SHARED_USER_ID/${GITLAB_SHARED_USER_ID}/g" | sed "s/GITLAB_SHARED_USER_NAME/${GITLAB_SHARED_USER_NAME}/g" > inputs/inputs_1_update.json
cat test/integ-template.yml | sed "s/GITLAB_PARENT_GROUP_ID/${GITLAB_PARENT_GROUP_ID}/g" | sed "s/GITLAB_SHARED_USER_ID/${GITLAB_SHARED_USER_ID}/g" | sed "s/GITLAB_SHARED_USER_NAME/${GITLAB_SHARED_USER_NAME}/g" > test/integ.yml

