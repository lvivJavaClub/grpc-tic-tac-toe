package com.lohika.jclub.ttt.vaadinui;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.javaclub.grpc.tictactoeserver.ConnectionGrpc;
import org.javaclub.grpc.tictactoeserver.ConnectionRequest;
import org.javaclub.grpc.tictactoeserver.ConnectionResponse;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicTacToeClient {
    private static final Logger logger = Logger.getLogger(TicTacToeClient.class.getName());

    private final ManagedChannel channel;
    private final ConnectionGrpc.ConnectionBlockingStub blockingStub;

    public TicTacToeClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    TicTacToeClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = ConnectionGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void connect(String name) {
        logger.info("Connecting " + name + " ...");
        ConnectionRequest request = ConnectionRequest.newBuilder().setId(name).build();
        ConnectionResponse response;
        try {
            response = blockingStub.connect(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Connected: " + response.getSuccess());
    }
}
