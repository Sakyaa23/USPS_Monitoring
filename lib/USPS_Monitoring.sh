scripttorun=${0}
scriptname=${0##/*/}
scriptpath=${0%%/$scriptname}
if [ $scriptpath != $0 ];then
cd $scriptpath
fi

INODE_CLASSPATH=../USPS_Check.jar:../lib/activation.jar:../lib/javax.mail.jar:../lib/jsch-0.1.54.jar:../lib/ojdbc8.jar:../lib/apache-poi-ooxml-schemas.jar:../lib/dom4j-1.6.1.jar:../lib/org.apache.xerces-2.9.0.jar:../lib/poi-3.9.jar:../lib/poi-ooxml-3.9.jar:../lib/xmlbeans-5.1.3.jar


/usr/WebSphere85/AppServer/java/jre/bin/java -cp ".:${INODE_CLASSPATH}" com.ibm.USPS_Monitoring