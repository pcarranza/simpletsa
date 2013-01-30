#!/bin/bash

if [[ -f ca.key ]]; then
	echo "ca.key file exists, quitting"
	exit 1
fi
echo ""
echo "CREATE PRIVATE KEY: ca.key"
echo ""
openssl genrsa -out ca.key 4096

echo ""
echo "CREATE SELF SIGNED CA ROOT CERTIFICATE: ca.crt"
echo ""
openssl req -new -x509 -days 1826 -key ca.key -out ca.crt

echo ""
echo "CREATE SUBORDINATE CA: ia.key"
echo ""
openssl genrsa -out ia.key 4096

echo ""
echo "CREATE CERTIFICATE REQUEST FOR THE SUBORDINATE CA: ia.csr"
echo ""
openssl req -new -key ia.key -out ia.csr

echo ""
echo "CREATE CERTIFICATE BY PROCESSING THE PREVIOUS REQUEST: ia.crt"
echo ""
openssl x509 -req -days 730 -in ia.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out ia.crt

echo ""
echo "CREATE PACK KEY AND CERTIFICATE CHAIN AS P12 FILE: ia.p12"
echo ""
openssl pkcs12 -export -out ia.p12 -inkey ia.key -in ia.crt -chain -CAfile ca.crt
