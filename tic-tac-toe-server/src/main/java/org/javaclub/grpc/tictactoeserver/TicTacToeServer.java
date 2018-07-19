package org.javaclub.grpc.tictactoeserver;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.logging.Logger;

public class TicTacToeServer {

        private static final Logger logger = Logger.getLogger(TicTacToeServer.class.getName());

        private Server server;

        private void start() throws IOException {
            /* The port on which the server should run */
            int port = 50051;
            server = ServerBuilder.forPort(port)
                    .addService(new ConnectionImpl())
                    .build()
                    .start();
            logger.info("Server started, listening on " + port);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                TicTacToeServer.this.stop();
                System.err.println("*** server shut down");
            }));
        }

        private void stop() {
            if (server != null) {
                server.shutdown();
            }
        }

        /**
         * Await termination on the main thread since the grpc library uses daemon threads.
         */
        private void blockUntilShutdown() throws InterruptedException {
            if (server != null) {
                server.awaitTermination();
            }
        }

        /**
         * Main launches the server from the command line.
         */
        public static void main(String[] args) throws IOException, InterruptedException {
            final TicTacToeServer server = new TicTacToeServer();
            server.start();
            server.blockUntilShutdown();
        }

    static class ConnectionImpl extends ConnectionGrpc.ConnectionImplBase {

        @Override
        public void connect(ConnectionRequest request, StreamObserver<ConnectionResponse> responseObserver) {
            logger.info("Connecting user: " + request.getId());
            ConnectionResponse reply = ConnectionResponse.newBuilder().setSuccess(true).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
