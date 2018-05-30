# sample-spring

## Docker
```bash
docker pull nalbam/sample-spring:latest # 301MB
docker pull nalbam/sample-spring:alpine #  88MB
docker pull nalbam/sample-spring:slim   # 107MB
```

## Openshift

### Create Project
```bash
oc new-project ops
oc new-project dev
oc new-project qa

oc policy add-role-to-user admin developer -n ops
oc policy add-role-to-user admin developer -n dev
oc policy add-role-to-user admin developer -n qa
```

### s2i-spring
```bash
oc import-image spring --from=docker.io/nalbam/s2i-spring --confirm -n ops
```

### Create Catalog
```bash
oc create -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/deploy.json -n ops
oc create -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/pipeline.json -n ops
```

### Create ConfigMap
```bash
oc create configmap sample-spring \
    --from-literal=PROFILE=dev \
    --from-literal=SLACK_WEBHOOK=https://hooks.slack.com/services/web/hook/token \
    --from-literal=SLACK_CHANNEL=sandbox \
    --from-literal=MESSAGE=UP \
    -n dev

oc create configmap sample-spring \
    --from-literal=PROFILE=qa \
    --from-literal=SLACK_WEBHOOK=https://hooks.slack.com/services/web/hook/token \
    --from-literal=SLACK_CHANNEL=sandbox \
    --from-literal=MESSAGE=UP \
    -n qa
```

### Create Applications
```bash
oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/deploy.json -n dev
oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/deploy.json -n qa
```

### Create Pipeline
```bash
oc new-app jenkins-ephemeral -n ops

oc policy add-role-to-user edit system:serviceaccount:ops:jenkins -n dev
oc policy add-role-to-user edit system:serviceaccount:ops:jenkins -n qa

oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/pipeline.json \
           -p SOURCE_REPOSITORY_URL=https://github.com/nalbam/sample-spring \
           -p JENKINS_URL=https://jenkins-ops.apps.nalbam.com \
           -p SLACK_WEBHOOK_URL=https://hooks.slack.com/services/web/hook/token \
           -p MAVEN_MIRROR_URL=http://nexus.ops.svc:8081/repository/maven-all-public/ \
           -p SONAR_HOST_URL=http://sonarqube.ops.svc:9000 \
           -n ops
```

### Start Build
```bash
oc start-build sample-spring-pipeline -n ops
```

### Cleanup
```bash
oc delete project ops dev qa
```
