# fresh checkout
#git clone https://github.com/molgenis/molgenis.git
#cd molgenis
# install all modules in local repository
mvn install
cd molgenis-app-omicsconnect
# start clean
mvn clean
# run jetty, this will generate, compile, create a war and run jetty
#mvn jetty:run
# result: success
