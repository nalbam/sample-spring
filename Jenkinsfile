def SERVICE_GROUP = "sample"
def SERVICE_NAME = "spring"
def IMAGE_NAME = "${SERVICE_GROUP}-${SERVICE_NAME}"
def REPOSITORY_URL = "https://github.com/nalbam/sample-spring"
def REPOSITORY_SECRET = ""
def SLACK_TOKEN_DEV = ""
def SLACK_TOKEN_DQA = ""

@Library("github.com/opspresso/builder")
def butler = new com.opspresso.builder.Butler()
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
        butler.prepare(IMAGE_NAME)
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
          butler.failure(SLACK_TOKEN_DEV, "Checkout")
          throw e
        }

        butler.scan("java")
      }
    }
    stage("Build") {
      container("maven") {
        try {
          butler.mvn_build()
          butler.success(SLACK_TOKEN_DEV, "Build")
        } catch (e) {
          butler.failure(SLACK_TOKEN_DEV, "Build")
          throw e
        }
      }
    }
    stage("Tests") {
      container("maven") {
        try {
          butler.mvn_test()
        } catch (e) {
          butler.failure(SLACK_TOKEN_DEV, "Tests")
          throw e
        }
      }
    }
    // stage("Code Analysis") {
    //   container("maven") {
    //     try {
    //       butler.mvn_sonar()
    //     } catch (e) {
    //       butler.failure(SLACK_TOKEN_DEV, "Code Analysis")
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
                butler.build_image()
              } catch (e) {
                butler.failure(SLACK_TOKEN_DEV, "Build Docker")
                throw e
              }
            }
          },
          "Build Charts": {
            container("builder") {
              try {
                butler.build_chart()
              } catch (e) {
                butler.failure(SLACK_TOKEN_DEV, "Build Charts")
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
            butler.deploy("local", "${SERVICE_GROUP}-dev", "${IMAGE_NAME}-dev", "dev")
            butler.success(SLACK_TOKEN_DEV, "Deploy DEV")
          } catch (e) {
            butler.failure(SLACK_TOKEN_DEV, "Deploy DEV")
            throw e
          }
        }
      }
      // stage("Request STAGE") {
      //   container("builder") {
      //     butler.proceed(SLACK_TOKEN_DEV, "Request STAGE", "stage")
      //     timeout(time: 60, unit: "MINUTES") {
      //       input(message: "${butler.name} ${butler.version} to stage")
      //     }
      //   }
      // }
      // stage("Proceed STAGE") {
      //   container("builder") {
      //     butler.proceed(SLACK_TOKEN_DQA, "Deploy STAGE", "stage")
      //     timeout(time: 60, unit: "MINUTES") {
      //       input(message: "${butler.name} ${butler.version} to stage")
      //     }
      //   }
      // }
      // stage("Deploy STAGE") {
      //   container("builder") {
      //     try {
      //       // deploy(cluster, namespace, sub_domain, profile)
      //       butler.deploy("local", "${SERVICE_GROUP}-stage", "${IMAGE_NAME}-stage", "stage")
      //       butler.success([SLACK_TOKEN_DEV,SLACK_TOKEN_DQA], "Deploy STAGE")
      //     } catch (e) {
      //       butler.failure([SLACK_TOKEN_DEV,SLACK_TOKEN_DQA], "Deploy STAGE")
      //       throw e
      //     }
      //   }
      // }
      stage("Proceed PROD") {
        container("builder") {
          butler.proceed(SLACK_TOKEN_DQA, "Deploy PROD", "prod")
          timeout(time: 60, unit: "MINUTES") {
            input(message: "${butler.name} ${butler.version} to prod")
          }
        }
      }
      stage("Deploy PROD") {
        container("builder") {
          try {
            // deploy(cluster, namespace, sub_domain, profile)
            butler.deploy("local", "${SERVICE_GROUP}-prod", "${IMAGE_NAME}", "prod")
            butler.success([SLACK_TOKEN_DEV,SLACK_TOKEN_DQA], "Deploy PROD")
          } catch (e) {
            butler.failure([SLACK_TOKEN_DEV,SLACK_TOKEN_DQA], "Deploy PROD")
            throw e
          }
        }
      }
    }
  }
}
