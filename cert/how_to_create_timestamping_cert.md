Step 0 (optional): create a self signed CA certificate with openssl
    > mkdir CA
    > cd CA

    # Create basic RSA key and Certificate Signature Request that will work as a CA
    > openssl req -new -newkey rsa:4096 -nodes -out ca.csr -keyout ca.key
    # Complete the data with whatever you consider insteresting to have in your CA

    # Sign your Certificate Signature Request with your key and emit your first certificate
    > openssl x509 -trustout -signkey ca.key -days 1461 -req -in ca.csr -out ca.pem

    # You now have 3 files:
    ## ca.key -> your CA private key
    ## ca.csr -> certificate signature request
    ## ca.pem -> your CA trusted public certificate

Step 1: create your TSA certificate and private key with openssl
    > mkdir tsa
    > cd tsa

    # Create another RSA private key
    > openssl genrsa -out client.key 4096

    # On osx we will need a basic openssl configuration with a little tweaks
    > cat /System/Library/OpenSSL/openssl.cnf > ext.config
    > echo "[ tsa ]" >> ext.config
    > echo "extendedKeyUsage=critical,timeStamping" >> ext.config

    # Create a configuration file with the timestamping extensions
    > echo "extendedKeyUsage=critical,timeStamping" >> ext-only.config
    
    # Create a certificate signature request with the TSA extension configuration file
    > openssl req -new -key client.key -out client.csr -nodes -days 1461 -extensions 'tsa' -config ./ext.config

    # Sign the request with your CA using the timestamper configuration file
    > openssl x509 -req -days 1641 -in client.csr -CA ../CA/ca.pem -CAkey ../CA/ca.key -set_serial 01 -out client.crt -extfile ext-only.config

    # Validate by printing the certificate information
    > openssl x509 -text -in client.crt
    # Look for timestamping critical extensions:
            X509v3 extensions:
                X509v3 Extended Key Usage: critical
                    Time Stamping

Step 2: import your openssl generated timestamper keypair and certificate with java keytool
    # Export the tsa certificate with openssl with the whole chain (the password should be longer than 6 chars)
    > openssl pkcs12 -export -out tsa.p12 -inkey client.key -in client.crt -CAfile ../CA/ca.pem -chain

    # Print the certificates in the keystore:
    > keytool -list -keystore tsa.p12 -v -storetype PKCS12

    # Change the alias for a valid alias
    > keytool -changealias -keystore tsa.p12 -storetype pkcs12 -alias 1 -destalias timestamping-cert

    # Check it again just to be sure
