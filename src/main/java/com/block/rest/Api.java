package com.block.rest;

import com.block.commons.JSON;
import com.block.manager.*;
import com.block.message.Message;
import com.block.message.ProcessMessage;
import com.block.model.*;
import com.block.service.*;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static spark.Spark.*;
import static spark.Spark.get;
import static spark.Spark.post;

public class Api {
    public static final String API_POOL = "/api/pool";
    public static final String API_LEDGER = "/api/ledger";
    public static final String API_SERVERS = "/api/servers";
    public static final String API_BLOCK = "/api/block";
    public static final String API_TRANSACTION = "/api/transaction";

    private MessageParameters params = new MessageParameters();
    private Server server = new Server();

    public void start(String url, int port, String user, String configFilePath) {
        server.init(url,port,user,configFilePath);
        port(port);
        get(API_LEDGER, (request, response) -> {
            return server.apiGetLedger();
        });

        get("/api/balance/:id", (request, response) -> {
            String accountId = request.params(":id");
            BigDecimal balance = null;
            try {
                balance = server.apiGetBalanceById(accountId);
                return new ResultSet.ResultSetBuilder().setOkStatus().setData(balance).build();
            } catch (Exception e) {
                return new ResultSet.ResultSetBuilder().setErrorStatus().setData(e.getMessage()).build();
            }
        });
        get("/api/mine", ((request, response) -> {
            return server.apiGetMine();
        }));

        put("/api/transfer", ((request, response) -> {
            AccountTransfer transfer = (AccountTransfer) JSON.fromJson(request.body(), AccountTransfer.class);
            return server.apiPutTransfer(transfer);
        }));

        post("/api/transaction", ((request, response) -> {
            Transaction tx  = (Transaction) JSON.fromJson(request.body(), Transaction.class);
            return  server.apiPostTransaction(tx);
        }));

        post("/api/servers", ((request, response) -> {
            Message msg = (Message) JSON.fromJson(request.body(), Message.class);
            return server.apiProcessMessage(msg);
        }));
        post("/api/seed", ((request, response) -> {
            Message msg = (Message) JSON.fromJson(request.body(), Message.class);
            return server.apiProcessMessage(msg);
        }));
        get("/api/servers", ((request, response) -> {
            return server.apiGetServers();
        }));
        post("/api/stats", ((request, response) -> {
            Message msg = (Message) JSON.fromJson(request.body(), Message.class);
            return server.apiProcessMessage(msg);
        }));

        post("/api/block", ((request, response) -> {
            try {
                String originatingServer = URLDecoder.decode(request.queryParams("server"), StandardCharsets.UTF_8.toString());
                Block b = (Block) JSON.fromJson(request.body(), Block.class);
                return server.apiPostBlock(b);
            } catch (Exception e) {
                return "FAILED";
            }
        }));

        get("/api/block/:hash", ((request, response) -> {
            String hash = request.params("hash");
            String index = request.queryParams("index");
            return server.apiGetBlock(hash, index);
        }));

        get("/api/block", ((request, response) -> {
            String hash = request.params("hash");
            String index = request.queryParams("index");
            return server.apiGetBlock(hash, index);
        }));

        get("/api/pool", ((request, response) -> {
            return server.apiGetTransactionPool();
        }));

        get("/api/unspent", ((request, response) -> {
            return server.apiGetUnspent();
        }));

        server.startup();
    }

    public void stopServer() {
        stop();
    }

    public static void main(String[] args) {
        Api api = new Api();
        api.start(args[0], Integer.valueOf(args[1]), args[2], args[3]);
    }
}
