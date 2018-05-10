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
oc project openshift
oc import-image -n openshift openshift/redhat-openjdk-18:1.3 --from=registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift:latest --confirm
```

### Create project
```
oc new-project dev
oc new-project qa
oc new-project ops

oc policy add-role-to-user admin admin -n dev
oc policy add-role-to-user admin admin -n qa
oc policy add-role-to-user admin admin -n ops
```

### Create app
```
oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/spring.json -n dev
```

### Create pipeline
```
oc create -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/pipeline.yaml -n ops

oc policy add-role-to-user edit system:serviceaccount:ops:jenkins -n dev
oc policy add-role-to-user edit system:serviceaccount:ops:jenkins -n qa

oc policy add-role-to-group system:image-puller system:serviceaccounts:ops -n dev
oc policy add-role-to-group system:image-puller system:serviceaccounts:ops -n qa
oc policy add-role-to-group system:image-puller system:serviceaccounts:qa -n dev

oc create deploymentconfig sample-spring --image=docker-registry.default.svc:5000/dev/sample-spring:qa -n qa
```

### Start Build
```
oc start-build sample-spring-pipeline -n ops
```

### Github Webhook url
```
Payload URL: https://<host:port>/oapi/v1/namespaces/<namespace>/buildconfigs/<name>/webhooks/<secret>/github
Content Type: application/json
Secret: (leave blank)
```
