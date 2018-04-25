# sample-spring

## Docker
```
FROM openjdk:8
EXPOSE 8080

docker pull nalbam/sample-spring:latest (301MB)
docker pull nalbam/sample-spring:alpine (88MB)
docker pull nalbam/sample-spring:slim   (107MB)
```

## Openshift
### Creating a project
```
oc new-project sample --display-name="Sample" --description="Sample Project"
oc project sample
```

### Creating new apps
```
oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/spring.json
```
