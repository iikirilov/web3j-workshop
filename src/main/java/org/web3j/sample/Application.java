package org.web3j.sample;

import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.geth.Geth;
import org.web3j.protocol.http.HttpService;
import org.web3j.sample.contracts.generated.Greeter;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

/**
 * A simple web3j application that demonstrates a number of core features of web3j:
 *
 * <ol>
 *     <li>Connecting to a node on the Ethereum network</li>
 *     <li>Loading an Ethereum wallet file</li>
 *     <li>Sending Ether from one address to another</li>
 *     <li>Deploying a smart contract to the network</li>
 *     <li>Reading a value from the deployed smart contract</li>
 *     <li>Updating a value in the deployed smart contract</li>
 *     <li>Viewing an event logged by the smart contract</li>
 * </ol>
 *
 * <p>To run this demo, you will need to provide:
 *
 * <ol>
 *     <li>Ethereum client (or node) endpoint. The simplest thing to do is
 *     <a href="https://infura.io/register.html">request a free access token from Infura</a></li>
 *     <li>A wallet file. This can be generated using the web3j
 *     <a href="https://docs.web3j.io/command_line.html">command line tools</a></li>
 *     <li>Some Ether. This can be requested from the
 *     <a href="https://www.rinkeby.io/#faucet">Rinkeby Faucet</a></li>
 * </ol>
 *
 * <p>For further background information, refer to the project README.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private Scanner s = new Scanner(System.in);
    private ContractGasProvider gasProvider =
            new StaticGasProvider(
                    Convert.toWei("40", Convert.Unit.GWEI).toBigInteger(),
                    BigInteger.valueOf(1_000_000L));

    public static void main(String[] args) throws Exception {
        new Application().run();
    }

    private void run() throws Exception {
        // We start by creating a new web3j instance to connect to remote nodes on the network.
        // Note: if using web3j Android, use Web3jFactory.build(...
        log.info("Enter your node url");
        final String node = s.nextLine();
        final Geth geth = Geth.build(new HttpService(node));
        try {
            geth.web3ClientVersion().send();
        } catch (final IOException e) {
            log.error("Failed to connect to node");
            throw e;
        }
        log.info("Connection to node successful");

        // We then need to load an account
        log.info("Please enter your wallet file path");
        final String walletFilePath = s.nextLine();
        log.info("Enter password");
        final String password = s.nextLine();

        final Credentials credentials =
                WalletUtils.loadCredentials(password, walletFilePath);

        // We need to fund your wallet to be able to deploy contracts
        log.info("Fund your wallet at: {}", credentials.getAddress());
        final DefaultBlockParameter dbp = DefaultBlockParameterName.LATEST;
        while (geth.ethGetBalance(credentials.getAddress(), dbp).send().getBalance().equals(BigInteger.ZERO)) {
            TimeUnit.SECONDS.sleep(3);
        }

        // Now lets deploy a smart contract
        log.info("Let's deploy your smart contract");
        log.info("What is your name?");
        final String name = s.nextLine();
        log.info("Deploying your contract, this may take a minute");
        final Greeter contract = Greeter.deploy(
                geth, credentials,
                gasProvider,
                name).send();
        log.info("Contract deployed at {}", contract.getContractAddress());

        // Events enable us to log specific events happening during the execution of our smart
        // contract to the blockchain. Index events cannot be logged in their entirety.
        // For Strings and arrays, the hash of values is provided, not the original value.
        // For further information, refer to https://docs.web3j.io/filters.html#filters-and-events

        // We register an observable for the event in our contract to be able to detect when someone sends us a message
        // Observables are useful to handle streams of data asynchronously
        // For more info, refer to http://reactivex.io/documentation/observable.html
        final Disposable disposable = contract.messageReceivedEventFlowable(dbp, dbp).subscribe(event -> {
            // onNext() method implementation
            log.info(event.message + " from " + event.name);
        }, error -> {
            // onError() method implementation
            log.error("Message received event flowable error", error);
        }, () -> {
            // onComplete() method implementation
            // used to clean up after the final onNext() call
            // *as the stream of blocks does not end, this case does not have a "final" onNext() call*
        });

        // Now lets send a message to our mate
        log.info("Enter your mates' contract address");
        final String matesContract = s.nextLine();
        log.info("What do you want to send them?");
        final String message = s.nextLine();
        log.info("Sending your message, it may take a while");
        final TransactionReceipt tr = contract.greet(matesContract, message).send();
        log.info("Your message was sent in {}", tr.getTransactionHash());

        // Currently you can only send one message per run -
        // TODO: Develop so you can actually "chat" using your contract,
    }
}
