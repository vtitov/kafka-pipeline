

app_working_dir=`cygpath.exe -ma . 2>/dev/null || relapath .`
export app_working_dir
jar_file=`ls -td ${app_working_dir}/lib/kafka-pipeline{-assembly,}*.jar | head -n1`
export jar_file

conf_file=${app_working_dir}/conf/yajsw-config.properties
export conf_file

app_config_file=${app_working_dir}/conf/application-template.conf
export app_config_file

app_main_class_cli="com.github.vtitov.kafka.pipeline.cli.CliMain"
export app_main_class_cli

logback_file=${app_working_dir}/conf/logback-template.xml
export logback_file
log_dir=${app_working_dir}/logs
export log_dir

[[ -z ${bootstrap_servers} ]] && bootstrap_servers="localhost:7011"
export bootstrap_servers
