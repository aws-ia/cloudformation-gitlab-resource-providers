#!/bin/bash
#
# Set up any prerequisites needed for cfn test
#
mkdir -p inputs
cat example_inputs/inputs_1_create.json | sed "s/GITLAB_PROJECT_ID/${GITLAB_PROJECT_ID}/g" | sed "s/GITLAB_REF/${GITLAB_REF}/g" > inputs/inputs_1_create.json
cat test/integ-template.yml | sed "s/GITLAB_PROJECT_ID/${GITLAB_PROJECT_ID}/g" | sed "s/GITLAB_REF/${GITLAB_REF}/g" > test/integ.yml

