package com.lohika.jclub.ttt.vaadinui;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.javaclub.grpc.tictactoeserver.ConnectionRequest;
import org.javaclub.grpc.tictactoeserver.GameGrpc;
import org.javaclub.grpc.tictactoeserver.MoveRequest;
import org.javaclub.grpc.tictactoeserver.MoveResponse;
import org.javaclub.grpc.tictactoeserver.PlayerResponse;
import org.javaclub.grpc.tictactoeserver.Point;

public class TicTacToeClient {
    private static final Logger logger = Logger.getLogger(TicTacToeClient.class.getName());

    private final ManagedChannel channel;
    private final GameGrpc.GameStub stub;

    public TicTacToeClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    TicTacToeClient(ManagedChannel channel) {
        this.channel = channel;
        stub = GameGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void connect(String name, StreamObserver<PlayerResponse> streamObserver) {
        logger.info("Connecting " + name + " ...");
        ConnectionRequest request = ConnectionRequest.newBuilder().setId(name).build();
        try {
            stub.connect(request, new StreamObserver<PlayerResponse>() {
                @Override
                public void onNext(PlayerResponse playerResponse) {
                    Point point = playerResponse.getPoint();
                    Integer p = point.getY() + point.getX() + (2 * point.getX());
                    String text = playerResponse.getChar().name();
//                    map.get(p).setText(text);
                    logger.info("Move: " + text + " to " + p);
                }

                @Override
                public void onError(Throwable throwable) {
                }

                @Override
                public void onCompleted() {
                }
            });
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
    }

    public void makeMove(Integer point, String id) {
        StreamObserver<MoveResponse> observer = new StreamObserver<MoveResponse>() {
            @Override
            public void onNext(MoveResponse moveResponse) {
                logger.info("Status: " + moveResponse.getSuccess());
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onCompleted() {

            }
        };
        StreamObserver<MoveRequest> move = stub.makeMove(observer);
        move.onNext(MoveRequest.newBuilder()
                .setPoint(Point.newBuilder().setX(point / 3).setY(point % 3).build())
                .setId(id)
                .build());
        move.onCompleted();
    }
}
