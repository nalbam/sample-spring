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

### Create ConfigMap
```json
{
    "kind": "ConfigMap",
    "apiVersion": "v1",
    "metadata": {
        "name": "sample-spring"
    },
    "data": {
        "PROFILE": "dev",
        "SLACK_WEBHOOK": "https://hooks.slack.com/services/web/hook/token",
        "SLACK_CHANNEL": "sandbox",
        "MESSAGE": "UP"
    }
}
```

### Create Application
```bash
oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/deploy.json -n dev
oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/deploy.json -n qa
```

### Create Pipeline
```bash
oc new-app jenkins-ephemeral -n ops

oc policy add-role-to-user edit system:serviceaccount:ops:jenkins -n dev
oc policy add-role-to-user edit system:serviceaccount:ops:jenkins -n qa

oc new-app -f https://raw.githubusercontent.com/nalbam/sample-spring/master/openshift/templates/pipeline.json -n ops \
           -p SOURCE_REPOSITORY_URL=https://github.com/nalbam/sample-spring
```

### Start Build
```bash
oc start-build sample-spring-pipeline -n ops
```

### Cleanup
```bash
oc delete project ops dev qa
```
