package org.web3j.sample;

import java.math.BigInteger;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.sample.contracts.generated.Greeter;
import org.web3j.tx.gas.DefaultGasProvider;

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

        OkHttpClient client = new OkHttpClient().newBuilder()
                .authenticator((route, response) -> {
                    String credential = okhttp3.Credentials.basic("epirus", "epirus-rocks");
                    return response.request().newBuilder().header("Authorization", credential).build();
                })
                .build();


        // We start by creating a new web3j instance to connect to remote nodes on the network.
        // Note: if using web3j Android, use Web3jFactory.build(...
        Web3j geth = Web3j.build(new HttpService(
                "https://rinkby-geth.clients.epirus.blk.io", client, false));
        log.info("Connection to node successful");

        // We then need to create a new account
        log.info("Write your password - It can be empty");
        String password = s.nextLine();
        log.info("Please enter your mnemonic, WRITE IT DOWN SOMEWHERE SAFE TO ACCESS THIS ACCOUNT IN THE FUTURE");
        String mnemonic = s.nextLine();

        Credentials credentials =
                WalletUtils.loadBip39Credentials(password, mnemonic);

        // We need to fund your wallet to be able to deploy contracts
        log.info("Fund your wallet at: {}", credentials.getAddress());

        DefaultBlockParameter dbp = DefaultBlockParameterName.LATEST;

        do {
            final BigInteger balance = geth.ethGetBalance(credentials.getAddress(), dbp).send().getBalance();
            log.info("Your balance is: {}", balance);
            TimeUnit.SECONDS.sleep(3);
        } while (geth.ethGetBalance(credentials.getAddress(), dbp).send().getBalance().equals(BigInteger.ZERO));

        // Now lets deploy a smart contract
        log.info("Let's deploy your smart contract");
        log.info("What is your name?");
        String name = s.nextLine();
        log.info("Deploying your contract, this may take a minute");
        Greeter contract = Greeter.deploy(
                geth, credentials,
                new DefaultGasProvider(),
                name).send();
        log.info("Contract deployed at {}", contract.getContractAddress());

        // TODO: how can we load an already existing smart contract?

        // Events enable us to log specific events happening during the execution of our smart
        // contract to the blockchain. Index events cannot be logged in their entirety.
        // For Strings and arrays, the hash of values is provided, not the original value.
        // For further information, refer to https://docs.web3j.io/filters.html#filters-and-events

        // We register an observable for the event in our contract to be able to detect when someone sends us a message
        // Observables are useful to handle streams of data asynchronously
        // For more info, refer to http://reactivex.io/documentation/observable.html


        contract.messageReceivedEventFlowable(dbp,dbp).subscribe(event -> {
            // onNext() method implementation
            log.info("{} says: {} in {}", event.name, event.message, event.log.getTransactionHash());

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
        log.info("{} says: {} in {}", name, message, tr.getTransactionHash());

        // Currently you can only send one message per run -
        // TODO: Develop so you can actually "chat" using your contract,
    }
}
