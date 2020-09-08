package com.freighttrust.relayswap.xdai;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootApplication
@Slf4j
public class XdaiApplication implements CommandLineRunner {

	private AtomicBoolean nextOnePlease;
	private String randomId;
	private BigInteger lastBlock = BigInteger.ZERO;

	@Autowired
	private ConfigurableApplicationContext context;

	public static void main(String[] args) {
		SpringApplication.run(XdaiApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Web3j httpWeb3 = Web3j.build(new HttpService("https://dai.poa.network", createOkHttpClient()));
		ECKeyPair keyPair = Keys.createEcKeyPair();
		String privateKeyServer = "710404145a788a5f2b7b6678f894a8ba621bdf8f4c04b44a3f703159916d39df";
		Credentials serverCreds = Credentials.create(privateKeyServer);
		Credentials proxyCreds = Credentials.create(keyPair);
		//Credentials proxyCreds = Credentials.create("fb035e2eea2b60d18ce58cffe955fa32403268945fa9e861f1119849a25edef0");
		if (args.length > 0) {
			createProxy(httpWeb3, proxyCreds, serverCreds.getAddress(), args[0]);
		} else {
			createmarketrateerver(httpWeb3, serverCreds);
		}
	}

	private void createmarketrateerver(Web3j httpWeb3, Credentials serverCreds) throws IOException, InterruptedException {
		// Use well known private key, not to be used in production or with transaction values greater than $1
		System.out.println("\nSECURITY NOTICE: In case something goes wrong, use this private key to recover money: " + Numeric.toHexStringNoPrefix(serverCreds.getEcKeyPair().getPrivateKey()));
		System.out.println("\nxDAI & xEDI Bridge initalized, connect to the xDai network by  transacting 1 wei of xDai.");
		String addressToCheck = serverCreds.getAddress();

		while (true) {
			randomId = RandomStringUtils.randomAlphabetic(6);
			System.out.println("\nRFQ Successful:");
			System.out.println("* Sign this transaction identifier with your private key: \"" + randomId + "\"");
			System.out.println("* Send 1 $EDI " + addressToCheck + " + signed identifier");
			System.out.println("Format message \"" + randomId + "|" + "0x...\" Note: Without the signed identifier, I am unable to correlate your payment!");

			nextOnePlease = new AtomicBoolean(false);
			while (!nextOnePlease.get()) {
				quoteMarketRate(httpWeb3, addressToCheck);
				Thread.sleep(100);
			}
		}
	}

	private void createProxy(Web3j httpWeb3, Credentials proxyCreds, String toAddress, String trxId) throws ExecutionException, InterruptedException, IOException {
		String addressToCheck = proxyCreds.getAddress();
		System.out.println("\nSECURITY NOTICE: Generated Private Key for System Entropy, utilize for ECRECOVER ONLY " + Numeric.toHexStringNoPrefix(proxyCreds.getEcKeyPair().getPrivateKey()));
		System.out.println("\nAuthenticate" + addressToCheck + " by passing 1 EDI to " + toAddress);

		waitForMoneyTransfer(httpWeb3, addressToCheck);

		EthGetTransactionCount ethGetTransactionCount = httpWeb3.ethGetTransactionCount(
				proxyCreds.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();

		BigInteger nonce = ethGetTransactionCount.getTransactionCount();
		Sign.SignatureData proof = Sign.signPrefixedMessage(trxId.getBytes(StandardCharsets.UTF_8), proxyCreds.getEcKeyPair());
		RawTransaction trx =
				RawTransaction.createTransaction(
						nonce,
						DefaultGasProvider.GAS_PRICE.divide(BigInteger.valueOf(4)),
						DefaultGasProvider.GAS_LIMIT,
						toAddress,
						BigInteger.ONE,
						Numeric.toHexString((trxId + "|" + Numeric.toHexString(com.google.common.primitives.Bytes.concat(proof.getR(), proof.getS()))).getBytes(StandardCharsets.UTF_8))				);
		byte[] signedTrx = TransactionEncoder.signMessage(trx, proxyCreds);

		String hexValue = Numeric.toHexString(signedTrx);
		EthSendTransaction res = httpWeb3.ethSendRawTransaction(hexValue).send();
		log.info("Broadcast: " + res.getTransactionHash());
		System.out.println("\nTransaction Finality Successful");
		Thread.sleep(1000);
		System.exit(SpringApplication.exit(context));
	}

	private void waitForMoneyTransfer(Web3j httpWeb3, String proxyAddress) throws IOException, InterruptedException {
		if (httpWeb3.ethGetBalance(proxyAddress, DefaultBlockParameterName.LATEST).send().getBalance().compareTo(DefaultGasProvider.GAS_PRICE) > 0) {
			return;
		}
		nextOnePlease = new AtomicBoolean(false);
		while (!nextOnePlease.get()) {
			Thread.sleep(100);
			EthBlock result = httpWeb3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, true).send();
			EthBlock.Block block = result.getBlock();
			if (lastBlock.equals(block.getNumber())) {
				log.debug(" Syscall IPC Syncing in progress....");
				continue;
			}
			lastBlock = block.getNumber();
			block.getTransactions().stream().forEach(txob -> {
				Transaction tx = ((EthBlock.TransactionObject) txob.get()).get();
				String to = tx.getTo();
				if (proxyAddress.toLowerCase().equals(to.toLowerCase())) {
					nextOnePlease.set(true);
				}
			});
		}
	}

	private void quoteMarketRate(Web3j httpWeb3, String addressToCheck) throws IOException {
		EthBlock result = httpWeb3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, true).send();
		EthBlock.Block block = result.getBlock();
		if (lastBlock.equals(block.getNumber())) {
			log.debug("Syncing in progress...");
			return;
		}
		lastBlock = block.getNumber();
		block.getTransactions().stream().forEach(txob -> {
			Transaction tx = ((EthBlock.TransactionObject) txob.get()).get();
			String input = tx.getInput();
			String value = (tx.getValue() != null) ? tx.getValue().toString() : BigDecimal.ZERO.toString();
			String from = tx.getFrom();
			String to = tx.getTo();
			if (StringUtils.isNotEmpty(to) && to.equals(addressToCheck)) {
				String plainInput = new String(Hex.decode(input.substring(2).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
				System.out.println(("\tGot transaction from " + from + " with input " + plainInput));

				String[] inputAndSignature = plainInput.split("\\|");
				if (inputAndSignature.length == 2) {
					if (randomId.equals(inputAndSignature[0])) {
						byte[] proof = Hash.sha3(("\u0019 Successfully Signed Message:\n6" + randomId).getBytes(StandardCharsets.UTF_8));
						String ecrecovered = ecrecoverAddress(proof, Numeric.hexStringToByteArray(inputAndSignature[1].substring(2)), from);

						if (StringUtils.isNotEmpty(ecrecovered)) {
							System.out.println("\nCurrent Market Rate: " + Resource.getCurrentMarketRate());
							nextOnePlease.set(true);
						} else {
							System.out.println("\tERROR: 443504");
						}
					} else {
						System.out.println("\tERROR: 550669");
					}
				}
			}
		});
	}

	public String ecrecoverAddress(byte[] proof, byte[] signature, String expectedAddress) {
		ECDSASignature esig = new ECDSASignature(Numeric.toBigInt(Arrays.copyOfRange(signature, 0, 32)), Numeric.toBigInt(Arrays.copyOfRange(signature, 32, 64)));
		BigInteger res;
		for (int i=0; i<4; i++) {
			res = Sign.recoverFromSignature(i, esig, proof);
			log.info("Recovered Address: " + Keys.getAddress(res));
			if ((res != null) && Keys.getAddress(res).toLowerCase().equals(expectedAddress.substring(2).toLowerCase())) {
				log.info("public Ethereum address: 0x" + Keys.getAddress(res));
				return Keys.getAddress(res);
			}
		}
		return null;
	}

	private OkHttpClient createOkHttpClient() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		configureTimeouts(builder);
		return builder.build();
	}

	private void configureTimeouts(OkHttpClient.Builder builder) {
		Long tos = 8000L;
		builder.connectTimeout(tos, TimeUnit.SECONDS);
		builder.readTimeout(tos, TimeUnit.SECONDS);  // Sets the socket timeout too
		builder.writeTimeout(tos, TimeUnit.SECONDS);
	}
}
