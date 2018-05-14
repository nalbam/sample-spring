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
oc import-image -n openshift openshift/redhat-openjdk-18:1.3 --from=registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift:latest --confirm
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

### Create app
```
oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/deploy.json -n dev
oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/deploy.json -n qa
```

### Create pipeline
```
oc new-app jenkins-ephemeral -n ops

oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/pipeline.json \
           -p SOURCE_REPOSITORY_URL=https://github.com/nalbam/sample-spring
           -n ops

oc policy add-role-to-user edit system:serviceaccount:ops:jenkins -n dev
oc policy add-role-to-user edit system:serviceaccount:ops:jenkins -n qa

oc policy add-role-to-group system:image-puller system:serviceaccounts:ops -n dev
oc policy add-role-to-group system:image-puller system:serviceaccounts:ops -n qa
oc policy add-role-to-group system:image-puller system:serviceaccounts:qa -n dev
```

### Start Build
```
oc start-build sample-spring-pipeline -n ops
```

### Cleanup
```
oc delete project ops dev qa
```
