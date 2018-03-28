package org.web3j.sample;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.geth.Geth;
import org.web3j.protocol.http.HttpService;
import org.web3j.sample.contracts.generated.Greeter;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

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

    public static void main(String[] args) throws Exception {
        new Application().run();
    }

    private void run() throws Exception {

        // We start by creating a new web3j instance to connect to remote nodes on the network.
        // Note: if using web3j Android, use Web3jFactory.build(...
        log.info("Enter your node url");
        String node = s.nextLine();
        Geth geth = Geth.build(new HttpService(node));
        log.info("Connection to node successful");

        // Let us create a new account for you
        log.info("Lets create a new account");
        log.info("Choose a password for your account:");
        String password = s.nextLine();
        log.info("Your password is {}", password);
        NewAccountIdentifier newAccountIdentifier = geth.personalNewAccount(password).send();
        log.info("New account {} created", newAccountIdentifier.getAccountId());

        // We then need to load our Ethereum wallet file
        log.info("Please enter you wallet file location");
        String walletFileLocation = s.nextLine();

        Credentials credentials =
                WalletUtils.loadCredentials(
                        password,
                        walletFileLocation);

        // Now lets deploy a smart contract - Remeber to enter YOUR name
        log.info("Let's deploy your smart contract");
        log.info("What is your name?");
        String name = s.nextLine();
        log.info("Deploying your contract, this may take a minute");
        Greeter contract = Greeter.deploy(
                geth, credentials,
                ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT,
                name).send();
        log.info("Contract deployed at {}", contract.getContractAddress());



        // Events enable us to log specific events happening during the execution of our smart
        // contract to the blockchain. Index events cannot be logged in their entirety.
        // For Strings and arrays, the hash of values is provided, not the original value.
        // For further information, refer to https://docs.web3j.io/filters.html#filters-and-events

        // We register an observable for the event in our contract to be able to detect when someone sends us a message
        // Observables are useful to handle streams of data asynchronously
        // For more info, refer to http://reactivex.io/documentation/observable.html
        DefaultBlockParameter dbp = DefaultBlockParameterName.LATEST;
        contract.messageReceivedEventObservable(dbp,dbp).subscribe(event -> {
            // onNext() method implementation
            log.info(event.message + " from " + Arrays.toString(event.name));
        }, error -> {
            // onError() method implementation
            log.error("Message received event observable error {}", error);
        }, () -> {
            // onComplete() method implementation
            // used to clean up after the final onNext() call
            // *as the stream of blocks does not end, this case does not have a "final" onNext() call*
        });

        // Now lets send a message to our mate
        log.info("Enter your mates' contract address");
        String matesContract = s.nextLine();
        log.info("What do you want to send them?");
        String message = s.nextLine();
        log.info("Sending your message, it may take a while");
        TransactionReceipt tr = contract.greet(matesContract, message).send();
        log.info("Your message was sent in {}", tr.getTransactionHash());

        // Currently you can only send one message per run -
        // TODO: Develop so you can actually "chat" using your contract,
    }
}
