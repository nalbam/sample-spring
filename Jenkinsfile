def SERVICE_GROUP = "sample"
def SERVICE_NAME = "spring"
def IMAGE_NAME = "${SERVICE_GROUP}-${SERVICE_NAME}"
def REPOSITORY_URL = "https://github.com/nalbam/sample-spring"
def REPOSITORY_SECRET = ""
def SLACK_TOKEN_DEV = ""
def SLACK_TOKEN_DQA = ""

@Library("github.com/opspresso/builder")
def builder = new com.opspresso.builder.Builder()
def label = "worker-${UUID.randomUUID().toString()}"

properties([
  buildDiscarder(logRotator(daysToKeepStr: "60", numToKeepStr: "30"))
])
podTemplate(label: label, containers: [
  containerTemplate(name: "builder", image: "opspresso/builder:kube", command: "cat", ttyEnabled: true, alwaysPullImage: true),
  containerTemplate(name: "maven", image: "maven:3.5.4-jdk-8-alpine", command: "cat", ttyEnabled: true)
], volumes: [
  hostPathVolume(mountPath: "/var/run/docker.sock", hostPath: "/var/run/docker.sock"),
  hostPathVolume(mountPath: "/home/jenkins/.m2", hostPath: "/home/jenkins/.m2")
]) {
  node(label) {
    stage("Prepare") {
      container("builder") {
        builder.prepare(IMAGE_NAME)
      }
    }
    stage("Checkout") {
      container("builder") {
        try {
          if (REPOSITORY_SECRET) {
            git(url: REPOSITORY_URL, branch: BRANCH_NAME, credentialsId: REPOSITORY_SECRET)
          } else {
            git(url: REPOSITORY_URL, branch: BRANCH_NAME)
          }
        } catch (e) {
          builder.failure(SLACK_TOKEN_DEV, "Checkout")
          throw e
        }

        builder.scan("java")
      }
    }
    stage("Build") {
      container("maven") {
        try {
          builder.mvn_build()
          builder.success(SLACK_TOKEN_DEV, "Build")
        } catch (e) {
          builder.failure(SLACK_TOKEN_DEV, "Build")
          throw e
        }
      }
    }
    stage("Tests") {
      container("maven") {
        try {
          builder.mvn_test()
        } catch (e) {
          builder.failure(SLACK_TOKEN_DEV, "Tests")
          throw e
        }
      }
    }
    // stage("Code Analysis") {
    //   container("maven") {
    //     try {
    //       builder.mvn_sonar()
    //     } catch (e) {
    //       builder.failure(SLACK_TOKEN_DEV, "Code Analysis")
    //       throw e
    //     }
    //   }
    // }
    if (BRANCH_NAME == "master") {
      stage("Build Image") {
        parallel(
          "Build Docker": {
            container("builder") {
              try {
                builder.build_image()
              } catch (e) {
                builder.failure(SLACK_TOKEN_DEV, "Build Docker")
                throw e
              }
            }
          },
          "Build Charts": {
            container("builder") {
              try {
                builder.build_chart()
              } catch (e) {
                builder.failure(SLACK_TOKEN_DEV, "Build Charts")
                throw e
              }
            }
          }
        )
      }
      stage("Deploy DEV") {
        container("builder") {
          try {
            // deploy(cluster, namespace, sub_domain, profile)
            builder.deploy("dev", "${SERVICE_GROUP}-dev", "${IMAGE_NAME}-dev", "dev")
            builder.success(SLACK_TOKEN_DEV, "Deploy DEV")
          } catch (e) {
            builder.failure(SLACK_TOKEN_DEV, "Deploy DEV")
            throw e
          }
        }
      }
      stage("Request STAGE") {
        container("builder") {
          builder.proceed(SLACK_TOKEN_DEV, "Request STAGE", "stage")
          timeout(time: 60, unit: "MINUTES") {
            input(message: "${builder.name} ${builder.version} to stage")
          }
        }
      }
      stage("Proceed STAGE") {
        container("builder") {
          builder.proceed(SLACK_TOKEN_DQA, "Deploy STAGE", "stage")
          timeout(time: 60, unit: "MINUTES") {
            input(message: "${builder.name} ${builder.version} to stage")
          }
        }
      }
      stage("Deploy STAGE") {
        container("builder") {
          try {
            // deploy(cluster, namespace, sub_domain, profile)
            builder.deploy("dev", "${SERVICE_GROUP}-stage", "${IMAGE_NAME}-stage", "stage")
            builder.success([SLACK_TOKEN_DEV,SLACK_TOKEN_DQA], "Deploy STAGE")
          } catch (e) {
            builder.failure([SLACK_TOKEN_DEV,SLACK_TOKEN_DQA], "Deploy STAGE")
            throw e
          }
        }
      }
      stage("Proceed PROD") {
        container("builder") {
          builder.proceed(SLACK_TOKEN_DQA, "Deploy PROD", "prod")
          timeout(time: 60, unit: "MINUTES") {
            input(message: "${builder.name} ${builder.version} to prod")
          }
        }
      }
      stage("Deploy PROD") {
        container("builder") {
          try {
            // deploy(cluster, namespace, sub_domain, profile)
            builder.deploy("prod", "${SERVICE_GROUP}-prod", "${IMAGE_NAME}", "prod")
            builder.success([SLACK_TOKEN_DEV,SLACK_TOKEN_DQA], "Deploy PROD")
          } catch (e) {
            builder.failure([SLACK_TOKEN_DEV,SLACK_TOKEN_DQA], "Deploy PROD")
            throw e
          }
        }
      }
    }
  }
}
