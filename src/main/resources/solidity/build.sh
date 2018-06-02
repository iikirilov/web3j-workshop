#!/usr/bin/env bash

targets="
greeter/Greeter
"

for target in ${targets}; do
    dirName=$(dirname $target)
    fileName=$(basename $target)

    cd $dirName
    echo "Compiling Solidity files in ${dirName}:"
    solc --bin --abi --optimize --overwrite ${fileName}.sol -o build/
    echo "Complete"

    echo "Generating web3j bindings"
    web3j solidity generate \
        build/${fileName}.bin \
        build/${fileName}.abi \
        -p org.web3j.sample.contracts.generated \
        -o ../../../java/ > /dev/null
    echo "Complete"

    cd -
done