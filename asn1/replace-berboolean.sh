#!/bin/bash

cd `dirname $0`

# replace BerBoolean from ASN1bean with special one for IEC 61850 so that true is coded as 0x01 instead of 0xff
find ../ -iname "*.java" | xargs sed -i 's/import com\.beanit\.asn1bean\.ber\.types\.BerBoolean/import com\.beanit\.iec61850bean\.internal\.BerBoolean/g'
