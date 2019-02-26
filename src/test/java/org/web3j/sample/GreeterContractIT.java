package org.web3j.sample;

import org.junit.Test;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.sample.contracts.generated.Greeter;
import org.web3j.tx.gas.DefaultGasProvider;

import java.util.concurrent.CompletableFuture;


/**
 * Integration test to run our main application.
 */
public class GreeterContractIT {

    @Test
    public void testGreeterContract() throws Exception {
        DefaultGasProvider gasProvider = new DefaultGasProvider();
        Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/fd932842b76e4f3f879e833690220675"));
        Credentials credentials = WalletUtils.loadCredentials("Seba-r13",
                "/Users/sebastianraba/Desktop/work/web3j-workshop/src/test/keystore.json");

        CompletableFuture<Greeter> greeter = Greeter.deploy(web3j, credentials, gasProvider, "First Contract").sendAsync();

        System.out.println(greeter.get().getContractAddress());
    }
}
