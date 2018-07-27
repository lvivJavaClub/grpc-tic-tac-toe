package org.javaclub.grpc.tictactoeserver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class TicTacToeServer {

        private static final Logger logger = Logger.getLogger(TicTacToeServer.class.getName());

        private Server server;
        private Game game;



        private void start() throws IOException {
            /* The port on which the server should run */
            game = new Game();
            int port = 50051;
            server = ServerBuilder.forPort(port)
                    .addService(new GameImpl(game))
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

    static class GameImpl extends GameGrpc.GameImplBase {

        private final Game game;
        private Set<StreamObserver<PlayerResponse>> observers;

        GameImpl(Game game) {
            this.game = game;
            observers = new HashSet<>();
        }

        @Override
        public void connect(ConnectionRequest request, StreamObserver<PlayerResponse> responseObserver) {
            logger.info("Connecting user: " + request.getId());
            game.newPlayer(request.getId());
            observers.add(responseObserver);
        }

        @Override
        public StreamObserver<MoveRequest> makeMove(StreamObserver<MoveResponse> responseObserver) {
            return new StreamObserver<MoveRequest>() {
                @Override
                public void onNext(MoveRequest moveRequest) {
                    Character character = game.makeMove(moveRequest.getId());
                    PlayerResponse playerResponse = PlayerResponse.newBuilder()
                            .setChar(character)
                            .setPoint(moveRequest.getPoint())
                            .build();
                    observers.forEach(o -> o.onNext(playerResponse));

                    responseObserver.onNext(MoveResponse.newBuilder()
                            .setSuccess(true)
                            .build());
                    responseObserver.onCompleted();
                }

                @Override
                public void onError(Throwable throwable) {
                    logger.log(Level.SEVERE, "Error: ", throwable);
                }

                @Override
                public void onCompleted() {
                }
            };
        }
    }
}
