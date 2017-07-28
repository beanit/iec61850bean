#!/bin/sh

rm ../../src/main/java-gen/org/openmuc/josistack/internal/presentation/asn1/*.java

jasn1-compiler -o ../../src/main/java-gen/ -p org.openmuc.josistack.internal.presentation -f iso-presentation-layer.asn
