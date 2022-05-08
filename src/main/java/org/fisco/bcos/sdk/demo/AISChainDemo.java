package org.fisco.bcos.sdk.demo;

import com.alibaba.fastjson.JSON;
import com.radium.BloomFilterBuilder;
import com.radium.BloomFilterTree;
import com.radium.HashProvider.HashMethod;
import com.radium.bean.AISData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;

public class AISChainDemo {
    // 获取配置文件路径
    private static final String configFile =
            Objects.requireNonNull(
                            AISChainDemo.class.getClassLoader().getResource("config-example.toml"))
                    .getPath();
    private static final String AISFile = "../src/main/resources/ais.csv";
    private static final String simulateAISFile = "../src/main/resources/simulate_ais.csv";
    private static final Integer duplicationTimes = 3;
    private static final Integer ROWS_PER_BLOCK = 1000;
    private static final Integer groupId = 1;
    //    private static final String AISChainAddress =
    // "0x32fa1a34152031b50eed348319f42bc1241bf753";

    public static void main(String[] args) throws Exception {
        // 模拟生成真实AISChain场景下的AIS数据
        simulateAISData();

        // 初始化BcosSDK对象
        BcosSDK sdk = BcosSDK.build(configFile);
        // 获取Client对象，此处传入的群组ID为1
        Client client = sdk.getClient(groupId);
        // 构造AssembleTransactionProcessor对象，需要传入client对象，CryptoKeyPair对象和abi、binary文件存放的路径。abi和binary文件需要在上一步复制到定义的文件夹中。
        CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair();
        AssembleTransactionProcessor transactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                        client,
                        keyPair,
                        "../src/main/resources/abi/",
                        "../src/main/resources/bin/");

        // deploy the AISChain
        System.out.println("====== Deploy AISChain ====== ");
        // 部署AISChain合约。第一个参数为合约名称，第二个参数为合约构造函数的列表，是List<Object>类型。
        TransactionResponse response =
                transactionProcessor.deployByContractLoader("AISChain", new ArrayList<>());
        String AISChainAddress = response.getContractAddress();
        System.out.println(
                "====== Deploy AISChain success, address: " + AISChainAddress + " ====== ");

        // build DBFT
        Integer t = 20;
        BloomFilterTree bloomFilterTree =
                (new BloomFilterBuilder())
                        .falsePositiveProb(0.2D)
                        .expectedElements(10000)
                        .hashFunction(HashMethod.SHA1)
                        .timeInterval((long) (t * 60))
                        .build();
        int dbft_counter = 0;

        // 创建调用交易函数的参数，此处为传入一个参数
        BufferedReader br = new BufferedReader(new FileReader(simulateAISFile));
        br.readLine(); // skip the first line
        String data;
        while ((data = br.readLine()) != null) {
            if (dbft_counter % ROWS_PER_BLOCK == 0) {
                bloomFilterTree.addNewBlock();
                System.out.println("Accumulate number of AISdata: " + dbft_counter);
            }
            String[] params = data.split(",", -1);
            if (!inDBFT(bloomFilterTree, dbft_counter / ROWS_PER_BLOCK, params[0], params[1])) {
                // System.out.println(Arrays.toString(params));
                // 调用AISChain合约，合约地址为hAISChainAddress， 调用函数名为『addAISData』，函数参数类型为params
                TransactionResponse transactionResponse =
                        transactionProcessor.sendTransactionAndGetResponseByContractLoader(
                                "AISChain", AISChainAddress, "addAISData", Arrays.asList(params));
                //                System.out.println(transactionResponse.getTransactionReceipt());
                //                break;
            }

            dbft_counter++;
            if (dbft_counter > 100000) {
                break;
            }
        }

        // 查询AISChain合约的『getAISData』函数，合约地址为AISChainAddress，参数为空
        CallResponse callResponse =
                transactionProcessor.sendCallByContractLoader(
                        "AISChain", AISChainAddress, "getAISData", new ArrayList<>());
        List<Object[][]> AISDataList = JSON.parseArray(callResponse.getValues(), Object[][].class);
        System.out.println(Arrays.toString(AISDataList.get(0)[AISDataList.get(0).length - 1]));

        System.exit(0);
    }

    public static void simulateAISData() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(AISFile));
        String head_line = br.readLine(); // skip the first line
        String line;
        List<String> res = new ArrayList<String>();
        List<String> res_part = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            //            System.out.println(line);
            //            String[] params = old_line.split(",", -1);
            //            new_line = params[0] + ',' + params[1];
            for (int i = 0; i < duplicationTimes; i++) {
                res_part.add(line);
            }

            if (res_part.size() >= 10000) {
                Collections.shuffle(res_part);
                res.addAll(res_part);
                res_part.clear();
            }
        }

        // 通过BufferedReader类创建一个使用默认大小输出缓冲区的缓冲字符输出流
        BufferedWriter bw = new BufferedWriter(new FileWriter(simulateAISFile));

        bw.write(head_line);
        // 将文档的下一行数据赋值给lineData，并判断是否为空，若不为空则输出
        for (int i = 0; i < res.size(); i++) {
            // 调用write的方法将字符串写到流中
            bw.newLine();
            bw.write(res.get(i));
        }

        // 使用缓冲区的刷新方法将数据刷到目的地中
        bw.flush();
        // 关闭缓冲区，缓冲区没有调用系统底层资源，真正调用底层资源的是FileWriter对象，缓冲区仅仅是一个提高效率的作用
        // 因此，此处的close()方法关闭的是被缓存的流对象
        bw.close();
    }

    // search if AISdata is in DBFT as well as update DBFT
    public static boolean inDBFT(
            BloomFilterTree bloomFilterTree, Integer blockId, String timestamp, String id)
            throws ParseException {
        AISData aisData = new AISData(timestamp, id);
        List<Integer> containList = bloomFilterTree.contains(aisData, aisData.timestamp());
        bloomFilterTree.addElement(aisData, blockId, aisData.timestamp());

        return containList.size() > 0;
    }
}
