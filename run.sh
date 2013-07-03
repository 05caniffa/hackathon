export JAVA_HOME=/export/home/apps/Oracle/Middleware/jdk160_14_R27.6.5-32/
export ANT_HOME=/export/home/apps/Oracle/Middleware/modules/org.apache.ant_1.7.0/
export PATH=$PATH:${ANT_HOME}/bin
export _WLS_PASS_KEY=`cat .dpass`

ant -emacs run

