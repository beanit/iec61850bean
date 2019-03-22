#!/bin/bash

cd `dirname $0`

find ../../ -iname "*.java" | xargs sed -i 's/import com\.beanit\.jasn1\.ber\.types\.BerBoolean/import com\.beanit\.openiec61850\.internal\.BerBoolean/g'
