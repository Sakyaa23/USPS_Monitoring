scripttorun=${0}
scriptname=${0##/*/}
scriptpath=${0%%/$scriptname}
print $scriptpath
if [ $scriptpath != $0 ];then
cd $scriptpath
fi
/opt/IBM/FileNet/Config/Datum/java8/bin/java -Xms1024m -Xmx1024M -cp ../FileNetReconCE.jar com.ibm.Util.GetFileNetReconCEData
