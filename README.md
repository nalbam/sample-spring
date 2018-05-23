# sample-spring

## Docker
```bash
docker pull nalbam/sample-spring:latest # 301MB
docker pull nalbam/sample-spring:alpine #  88MB
docker pull nalbam/sample-spring:slim   # 107MB
```

## Openshift
### Prepare Openjdk (s2i)
```bash
oc import-image openshift/redhat-openjdk-18:1.3 -n openshift \
                --from=registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift:latest \
                --confirm

oc create -f https://raw.githubusercontent.com/nalbam/openshift/master/s2i/openjdk18-basic-s2i.json \
          -n ops
```

### Create Project
```bash
oc new-project ops
oc new-project dev
oc new-project qa

oc policy add-role-to-user admin developer -n ops
oc policy add-role-to-user admin developer -n dev
oc policy add-role-to-user admin developer -n qa
```

### Create ConfigMap for Applications
```bash
oc create configmap sample-spring -n dev \
    --from-literal=PROFILE=dev \
    --from-literal=SLACK_WEBHOOK=https://hooks.slack.com/services/web/hook/token \
    --from-literal=SLACK_CHANNEL=sandbox \
    --from-literal=MESSAGE=UP

oc create configmap sample-spring -n qa \
    --from-literal=PROFILE=qa \
    --from-literal=SLACK_WEBHOOK=https://hooks.slack.com/services/web/hook/token \
    --from-literal=SLACK_CHANNEL=sandbox \
    --from-literal=MESSAGE=UP
```

### Create Applications
```bash
oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/deploy.json -n dev
oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/deploy.json -n qa
```

### Create ConfigMap for Pipeline
```bash
oc create configmap pipeline -n ops \
    --from-literal=JENKINS_URL=https://jenkins-ops.nalbam.com \
    --from-literal=MAVEN_MIRROR_URL=http://nexus.ops.svc:8081/repository/maven-public/ \
    --from-literal=SONAR_HOST_URL=http://sonarqube.ops.svc:9000 \
    --from-literal=SLACK_WEBHOOK_URL=https://hooks.slack.com/services/web/hook/token
```

### Create Pipeline
```bash
oc new-app jenkins-ephemeral -n ops

oc policy add-role-to-user edit system:serviceaccount:ops:jenkins -n dev
oc policy add-role-to-user edit system:serviceaccount:ops:jenkins -n qa

oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/pipeline.json -n ops \
           -p SOURCE_REPOSITORY_URL=https://github.com/nalbam/sample-spring \
           -p MAVEN_MIRROR_URL=http://nexus.ops.svc:8081/repository/maven-public/
```

### Start Build
```bash
oc start-build sample-spring-pipeline -n ops
```

### Cleanup
```bash
oc delete project ops dev qa
```
