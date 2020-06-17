#!/bin/bash

cd `dirname $0`

rm ../../src/main/java-gen/com/beanit/josistack/internal/presentation/asn1/*.java
asn1bean-compiler -o ../../src/main/java-gen/ -p com.beanit.josistack.internal.presentation -f iso-presentation-layer.asn -dv
