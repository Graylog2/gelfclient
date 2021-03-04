pipeline
{
   agent
   {
     label 'linux'
   }

   options
   {
      ansiColor('xterm')
      buildDiscarder logRotator(artifactDaysToKeepStr: '30', artifactNumToKeepStr: '10', daysToKeepStr: '30', numToKeepStr: '10')
      timestamps()
   }

   tools
   {
      maven 'Maven'
   }

   environment
   {
      MAVEN_OPTS = '-Djansi.force=true'
   }

   stages
   {
      stage('Build')
      {
          steps
          {
             sh 'mvn package'
             stash name: 'workspace'
          }
       }

      stage('Package')
      {
         when
         {
             buildingTag()
         }

         steps
         {
            unstash 'workspace'
            sh 'mvn deploy'
         }

         post
         {
            success
            {
               archiveArtifacts 'target/gelfclient*.jar'
               cleanWs()
            }
         }
      }
   }
}
