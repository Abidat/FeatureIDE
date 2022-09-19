#!/bin/bash

#LOOK_FOR="com/ibm/icu/text/DecimalFormat"
#LOOK_FOR="org/eclipse/osgi/util/NLS"
#LOOK_FOR="runtime/IAdaptable"
#LOOK_FOR="org/eclipse/ui/plugin/AbstractUIPlugin"
#LOOK_FOR="org/osgi/service/prefs/BackingStoreException"
#LOOK_FOR="org/eclipse/ui"
LOOK_FOR="org/eclipse/ui/IFileEditorInput"

for i in `find . -name "*jar"`
do
#  echo "Looking in $i ..."
  jar tvf $i | grep $LOOK_FOR > /dev/null
  if [ $? == 0 ]
  then
    echo "==> Found \"$LOOK_FOR\" in $i"
  fi
done

