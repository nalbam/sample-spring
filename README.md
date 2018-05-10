# sample-spring

## Docker
```
docker pull nalbam/sample-spring:latest (301MB)
docker pull nalbam/sample-spring:alpine (88MB)
docker pull nalbam/sample-spring:slim   (107MB)
```

## Openshift
### Prepare openjdk
```
oc project openshift
oc import-image -n openshift openshift/redhat-openjdk-18:1.3 --from=registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift:latest --confirm
```

### Creating a project
```
oc new-project demo --display-name="Demo" --description="Demo Project"
oc policy add-role-to-user admin admin -n demo
```

### Creating new app
```
oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/spring.json
```

### Build the app
```
oc start-build sample-spring --follow
```

### Creating new pipeline
```
oc create -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/pipeline.yaml
```

### Github Webhook url
```
Payload URL: https://<host:port>/oapi/v1/namespaces/<namespace>/buildconfigs/<name>/webhooks/<secret>/github
Content Type: application/json
Secret: (leave blank)
```
