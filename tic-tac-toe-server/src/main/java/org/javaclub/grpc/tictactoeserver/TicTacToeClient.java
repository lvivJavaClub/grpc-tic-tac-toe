package org.javaclub.grpc.tictactoeserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicTacToeClient {
    private static final Logger logger = Logger.getLogger(TicTacToeClient.class.getName());

    private final ManagedChannel channel;
    private final ConnectionGrpc.ConnectionBlockingStub blockingStub;

    /** Construct client connecting to HelloWorld server at {@code host:port}. */
    public TicTacToeClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    /** Construct client for accessing HelloWorld server using the existing channel. */
    TicTacToeClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = ConnectionGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /** Say hello to server. */
    public void connect(String name) {
        logger.info("Will try to connect " + name + " ...");
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

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting.
     */
    public static void main(String[] args) throws Exception {
        TicTacToeClient client = new TicTacToeClient("localhost", 50051);
        try {
            /* Access a service running on the local machine on port 50051 */
            String user = "Player1";
            if (args.length > 0) {
                user = args[0]; /* Use the arg as the name to connect if provided */
            }
            client.connect(user);
        } finally {
            client.shutdown();
        }
    }
}
