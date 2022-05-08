package org.com.fisco;

import com.alibaba.fastjson.JSON;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.ABICodecException;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.transaction.model.exception.TransactionBaseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BcosSDKTest
{
    // 获取配置文件路径
    private static final String configFile = Objects.requireNonNull(BcosSDKTest.class.getClassLoader().getResource("config-example.toml")).getPath();
    private static final String AISFile = "src/main/resources/ais.csv";
    private static final String AISChainAddress = "0x32fa1a34152031b50eed348319f42bc1241bf753";

    public static void main(String[] args) throws Exception {
        // 初始化BcosSDK对象
        BcosSDK sdk = BcosSDK.build(configFile);
        // 获取Client对象，此处传入的群组ID为1
        Client client = sdk.getClient(1);
        // 构造AssembleTransactionProcessor对象，需要传入client对象，CryptoKeyPair对象和abi、binary文件存放的路径。abi和binary文件需要在上一步复制到定义的文件夹中。
        CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair();
        AssembleTransactionProcessor transactionProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(client, keyPair, "src/main/resources/abi/", "src/main/resources/bin/");

        // 创建调用交易函数的参数，此处为传入一个参数
        BufferedReader br = new BufferedReader(new FileReader(AISFile));
        br.readLine(); // skip the first line
        String data;
        while((data = br.readLine()) != null){
            String[] params = data.split(",");
            if (!isReplicatedAIS(transactionProcessor, params)) {
                System.out.println(Arrays.toString(params));
                // 调用AISChain合约，合约地址为hAISChainAddress， 调用函数名为『addAISData』，函数参数类型为params
                TransactionResponse transactionResponse = transactionProcessor.sendTransactionAndGetResponseByContractLoader("AISChain", AISChainAddress, "addAISData", Arrays.asList(params));
                System.out.println(transactionResponse.getTransactionReceipt());
                break;
            }
        }

        // 查询AISChain合约的『getAISData』函数，合约地址为AISChainAddress，参数为空
        CallResponse callResponse = transactionProcessor.sendCallByContractLoader("AISChain", AISChainAddress, "getAISData", new ArrayList<>());
        List<Object[][]> AISDataList = JSON.parseArray(callResponse.getValues(), Object[][].class);
        System.out.println(Arrays.toString(AISDataList.get(0)[AISDataList.get(0).length - 1]));

        System.exit(0);
    }

    // should be replaced with Bloom Filter Tree
    public static boolean isReplicatedAIS(AssembleTransactionProcessor transactionProcessor, String[] params) throws ABICodecException, TransactionBaseException {
        // 查询AISChain合约的『getAISData』函数，合约地址为AISChainAddress，参数为空
        CallResponse callResponse = transactionProcessor.sendCallByContractLoader("AISChain", AISChainAddress, "getAISData", new ArrayList<>());
        List<Object[][]> AISDataList = JSON.parseArray(callResponse.getValues(), Object[][].class);
        boolean flag = false;
        for (int i = 0; i < AISDataList.get(0).length; i++) {
            List<Object> AISData = List.of(AISDataList.get(0)[i]);
            if ((Objects.equals(AISData.get(1).toString(), params[0])) && (Objects.equals(AISData.get(2).toString(), params[1]))){
                flag = true;
                break;
            }
        }
        return flag;
    }
}
