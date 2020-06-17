#!/bin/bash

cd `dirname $0`

find ../../ -iname "*.java" | xargs sed -i 's/import com\.beanit\.asn1bean\.ber\.types\.BerBoolean/import com\.beanit\.openiec61850\.internal\.BerBoolean/g'
