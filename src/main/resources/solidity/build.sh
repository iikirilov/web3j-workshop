#!/usr/bin/env bash

set -e
set -o pipefail

baseDir="../../../../src/main/resources/solidity/greeter"
greeterDir="../${baseDir}/build"

targets="
Greeter
"

for target in ${targets}; do
    dirName=$(dirname "${target}")
    fileName=$(basename "${target}")

    cd $baseDir
    echo "Compiling Solidity file ${target}.sol"

    solc --abi --bin --metadata --optimize --overwrite \
            --allow-paths "$(pwd)" \
            ${dirName}/${fileName}.sol -o ${dirName}/build/
    echo "Complete"

    echo "Generating contract bindings"
    web3j solidity generate \
        -a=${greeterDir}/${fileName}.abi \
        -b=${greeterDir}/${fileName}.bin \
        -p org.web3j.sample.contracts.generated \
        -o ../../../java/ > /dev/null
    echo "Complete"

    cd -
done
