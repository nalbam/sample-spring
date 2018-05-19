# sample-spring

## Docker
```
docker pull nalbam/sample-spring:latest (301MB)
docker pull nalbam/sample-spring:alpine (88MB)
docker pull nalbam/sample-spring:slim   (107MB)
```

## Openshift
### Prepare openjdk (s2i)
```
oc import-image openshift/redhat-openjdk-18:1.3 -n openshift \
                --from=registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift:latest --confirm

oc create -n ops -f https://raw.githubusercontent.com/nalbam/openshift/master/s2i/openjdk18-basic-s2i.json
```

### Create project
```
oc new-project ops
oc new-project dev
oc new-project qa

oc policy add-role-to-user admin developer -n ops
oc policy add-role-to-user admin developer -n dev
oc policy add-role-to-user admin developer -n qa
```

### Create application
```
oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/deploy.json -n dev \
           -p PROFILE=dev
oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/deploy.json -n qa \
           -p PROFILE=qa
```

### Create pipeline
```
oc new-app jenkins-ephemeral -n ops

oc policy add-role-to-user edit system:serviceaccount:ops:jenkins -n dev
oc policy add-role-to-user edit system:serviceaccount:ops:jenkins -n qa

oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/pipeline.json -n ops \
           -p SOURCE_REPOSITORY_URL=https://github.com/nalbam/sample-spring \
           -p SLACK_WEBHOOK_URL=https://hooks.slack.com/services/web/hook/token
```

### Start Build
```
oc start-build sample-spring-pipeline -n ops
```

### Cleanup
```
oc delete project ops dev qa
```
