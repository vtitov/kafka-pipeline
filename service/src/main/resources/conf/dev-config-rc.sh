
app_working_dir=`cygpath.exe -ma . 2>/dev/null || relapath .`
export app_working_dir
jar_file=`ls -td ${app_working_dir}/kafka-pipeline/target/scala-2.12/kafka-pipeline-assembly*.jar | head -n1`
export jar_file

conf_file=${app_working_dir}/service/src/main/resources/conf/yajsw-config.properties
export conf_file

app_config_file=${app_working_dir}/service/src/main/resources/conf/application-template.conf
export app_config_file

bootstrap_servers="localhost:7011"
export bootstrap_servers

logback_file=${app_working_dir}/service/src/main/resources/conf/logback-template.xml
export logback_file
log_dir=${app_working_dir}/logs
export log_dir
mkdir -p log_dir
