#!/bin/bash

cd `dirname $0`

find ../../ -iname "*.java" | xargs sed -i 's/import com\.beanit\.asn1bean\.ber\.types\.BerBoolean/import com\.beanit\.iec61850bean\.internal\.BerBoolean/g'
