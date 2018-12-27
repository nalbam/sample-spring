def IMAGE_NAME = "sample-spring"
def REPOSITORY_URL = "git@github.com:nalbam/sample-spring.git"
def REPOSITORY_SECRET = "nalbam-secret"
def SLACK_TOKEN = ""

@Library("github.com/opsnow-tools/valve-butler")
def butler = new com.opsnow.valve.Butler()
def label = "worker-${UUID.randomUUID().toString()}"
def VERSION = ""
def SOURCE_LANG = ""
def SOURCE_ROOT = ""
properties([
  buildDiscarder(logRotator(daysToKeepStr: "60", numToKeepStr: "30"))
])
podTemplate(label: label, containers: [
  containerTemplate(name: "builder", image: "quay.io/opsnow-tools/valve-builder", command: "cat", ttyEnabled: true, alwaysPullImage: true),
  containerTemplate(name: "maven", image: "maven:3.5.4-jdk-8-alpine", command: "cat", ttyEnabled: true)
], volumes: [
  hostPathVolume(mountPath: "/var/run/docker.sock", hostPath: "/var/run/docker.sock"),
  hostPathVolume(mountPath: "/home/jenkins/.draft", hostPath: "/home/jenkins/.draft"),
  hostPathVolume(mountPath: "/home/jenkins/.helm", hostPath: "/home/jenkins/.helm")
]) {
  node(label) {
    stage("Prepare") {
      container("builder") {
        butler.prepare()

        if (!SLACK_TOKEN) {
          SLACK_TOKEN = butler.slack_token
        }
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
          butler.failure(SLACK_TOKEN, "Checkout", IMAGE_NAME)
          throw e
        }

        butler.scan(IMAGE_NAME, BRANCH_NAME, "java")

        VERSION = butler.version
        // SOURCE_LANG = butler.source_lang
        // SOURCE_ROOT = butler.source_root
      }
    }
    stage("Build") {
      container("maven") {
        try {
          butler.mvn_build()
          butler.success(SLACK_TOKEN, "Build", IMAGE_NAME, VERSION)
        } catch (e) {
          butler.failure(SLACK_TOKEN, "Build", IMAGE_NAME)
          throw e
        }
      }
    }
    stage("Tests") {
      container("maven") {
        try {
          butler.mvn_test()
        } catch (e) {
          butler.failure(SLACK_TOKEN, "Tests", IMAGE_NAME)
          throw e
        }
      }
    }
    stage("Code Analysis") {
      container("maven") {
        try {
          butler.mvn_sonar()
        } catch (e) {
          butler.failure(SLACK_TOKEN, "Code Analysis", IMAGE_NAME)
          throw e
        }
      }
    }
    if (BRANCH_NAME == "master") {
      stage("Build Image") {
        parallel(
          "Build Docker": {
            container("builder") {
              try {
                butler.build_image(IMAGE_NAME, VERSION)
              } catch (e) {
                butler.failure(SLACK_TOKEN, "Build Docker", IMAGE_NAME)
                throw e
              }
            }
          },
          "Build Charts": {
            container("builder") {
              try {
                butler.build_chart(IMAGE_NAME, VERSION)
              } catch (e) {
                butler.failure(SLACK_TOKEN, "Build Charts", IMAGE_NAME)
                throw e
              }
            }
          }
        )
      }
      stage("Deploy DEV") {
        container("builder") {
          try {
            butler.helm_install(IMAGE_NAME, VERSION, "dev", "dev.opsnow.com", "dev")
            butler.success(SLACK_TOKEN, "Deploy DEV", IMAGE_NAME, VERSION, "dev", "dev.opsnow.com")
          } catch (e) {
            butler.failure(SLACK_TOKEN, "Deploy DEV", IMAGE_NAME)
            throw e
          }
        }
      }
      stage("Proceed STAGE") {
        container("builder") {
          butler.proceed(SLACK_TOKEN, "Deploy STAGE", IMAGE_NAME, VERSION, "stage")
          timeout(time: 60, unit: "MINUTES") {
            input(message: "$IMAGE_NAME $VERSION to stage")
          }
        }
      }
      stage("Deploy STAGE") {
        container("builder") {
          try {
            butler.helm_install(IMAGE_NAME, VERSION, "stage", "dev.opsnow.com", "dev")
            butler.success(SLACK_TOKEN, "Deploy STAGE", IMAGE_NAME, VERSION, "stage", "dev.opsnow.com")
          } catch (e) {
            butler.failure(SLACK_TOKEN, "Deploy STAGE", IMAGE_NAME)
            throw e
          }
        }
      }
    }
  }
}
