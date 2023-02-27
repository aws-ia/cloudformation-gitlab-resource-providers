#!/bin/bash
#
# Set up any prerequisites needed for cfn test
#
mkdir -p inputs
cat example_inputs/inputs_1_create.json | sed "s/GITLAB_PARENT_GROUP_ID/${GITLAB_PARENT_GROUP_ID}/g"  > inputs/inputs_1_create.json
cat test/integ-template.yml | sed "s/GITLAB_PARENT_GROUP_ID/${GITLAB_PARENT_GROUP_ID}/g"  > test/integ.yml

