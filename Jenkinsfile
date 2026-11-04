// The same file is used in every sample-* repository. See docs/pipeline.md.
//
// GitHub -> Jenkins -> (Nexus) -> Harbor or Amazon ECR -> GitOps deploy.
// Branch and pull request builds run build, test and smoke test. Tag builds
// (v*) also publish the artifact, push the image and deploy.
//
// Jenkins environment variables (global or folder properties):
//   REGISTRY          Image registry and path, for example harbor.example.com/sample
//                     or 123456789012.dkr.ecr.ap-northeast-2.amazonaws.com. Required for tags.
//   NEXUS_URL         Nexus base URL. When empty, the Nexus stage is skipped.
//   NEXUS_REPOSITORY  Nexus raw hosted repository. Default raw-releases.
//   GITOPS_REPO       owner/repo that receives the deploy event. When empty, deploy stages are skipped.
// Jenkins credentials:
//   nexus             Username with password, for NEXUS_URL.
//   registry          Username with password, for a Harbor REGISTRY. ECR uses the agent's AWS credentials.
//   gitops-token      Secret text, a GitHub token that can dispatch events to GITOPS_REPO.

pipeline {
  agent any

  options {
    buildDiscarder(logRotator(numToKeepStr: '30'))
    disableConcurrentBuilds()
    timeout(time: 60, unit: 'MINUTES')
  }

  stages {
    stage('Prepare') {
      steps {
        script {
          env.APP_NAME = sh(returnStdout: true, script: 'basename -s .git "$(git config --get remote.origin.url)"').trim()
          env.VERSION = env.TAG_NAME ?: "v0.0.0-ci.${env.GIT_COMMIT}"
          env.PRERELEASE = ''
          if (env.TAG_NAME) {
            env.PRERELEASE = sh(returnStdout: true, script: 'scripts/release.sh tag "$TAG_NAME"').trim() - 'prerelease='
          }
          currentBuild.displayName = "#${env.BUILD_NUMBER} ${env.VERSION}"
        }
      }
    }

    // The Dockerfile build stage runs the tests.
    stage('Build and test') {
      steps {
        sh 'docker buildx build --pull --load --build-arg VERSION="$VERSION" --tag "$APP_NAME:ci-$BUILD_NUMBER" .'
      }
    }

    stage('Smoke test') {
      steps {
        sh 'scripts/smoke.sh "$APP_NAME:ci-$BUILD_NUMBER" "$VERSION"'
      }
    }

    stage('Artifact') {
      steps {
        sh 'scripts/release.sh artifact "$APP_NAME" "$VERSION" dist'
        archiveArtifacts artifacts: 'dist/*', fingerprint: true
      }
    }

    stage('Nexus') {
      when {
        buildingTag()
        expression { env.NEXUS_URL }
      }
      steps {
        withCredentials([usernamePassword(credentialsId: 'nexus', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]) {
          sh 'scripts/release.sh nexus "$APP_NAME" "$VERSION" dist'
        }
      }
    }

    stage('Image') {
      when { buildingTag() }
      steps {
        script {
          if (!env.REGISTRY) {
            error('REGISTRY is not set. Set it to a Harbor project or an Amazon ECR registry.')
          }
          if (env.REGISTRY ==~ /^[0-9]{12}\.dkr\.ecr\.[a-z0-9-]+\.amazonaws\.com(\/.*)?$/) {
            sh 'scripts/release.sh image "$REGISTRY" "$APP_NAME" "$VERSION"'
          } else {
            withCredentials([usernamePassword(credentialsId: 'registry', usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASSWORD')]) {
              sh 'scripts/release.sh image "$REGISTRY" "$APP_NAME" "$VERSION"'
            }
          }
        }
      }
    }

    stage('Deploy alpha') {
      when {
        buildingTag()
        expression { env.GITOPS_REPO }
      }
      steps {
        withCredentials([string(credentialsId: 'gitops-token', variable: 'GITOPS_TOKEN')]) {
          sh 'scripts/release.sh deploy alpha'
        }
      }
    }

    // Stable tags only, after a person approves.
    stage('Deploy prod') {
      when {
        beforeInput true
        buildingTag()
        expression { env.GITOPS_REPO && env.PRERELEASE == 'false' }
      }
      input {
        message 'Deploy to prod?'
        ok 'Deploy'
      }
      steps {
        withCredentials([string(credentialsId: 'gitops-token', variable: 'GITOPS_TOKEN')]) {
          sh 'AUTO_MERGE=true scripts/release.sh deploy prod'
        }
      }
    }
  }

  post {
    always {
      sh 'docker image rm "$APP_NAME:ci-$BUILD_NUMBER" > /dev/null 2>&1 || true'
    }
  }
}
