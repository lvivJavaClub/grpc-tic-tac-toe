package com.lohika.jclub.ttt.vaadinui;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.router.Route;
import io.grpc.stub.StreamObserver;
import org.javaclub.grpc.tictactoeserver.PlayerResponse;
import org.javaclub.grpc.tictactoeserver.Point;

import static java.util.stream.Collectors.toMap;

@Route("")
public class GameComponent extends Div {

  private static final Logger logger = Logger.getLogger(GameComponent.class.getName());

  private Map<Integer, Button> map;
  private TicTacToeClient client;
  private final String user;
  private final StreamObserver<PlayerResponse> streamObserver;

  public GameComponent() throws InterruptedException {
    map = IntStream.range(0, 9).boxed()
        .map(this::createButton)
        .collect(toMap(Focusable::getTabIndex, e -> e));

    map.values().stream()
        .peek(b -> {
          if (b.getTabIndex() % 3 == 0) {
            add(new Hr());
          }
        })
        .forEach(this::add);

    /**
     * Connecting to server via gRPC
     */
    client = new TicTacToeClient("localhost", 50051);
    try {
      /* Access a service running on the local machine on port 50051 */
      user = UUID.randomUUID().toString();

      streamObserver = new StreamObserver<PlayerResponse>() {
        @Override
        public void onNext(PlayerResponse playerResponse) {
          Point point = playerResponse.getPoint();
          Integer p = point.getY() + point.getX() + (2 * point.getX());
          String text = playerResponse.getChar().name();
          map.get(p).setText(text);
          logger.info("Move: " + text + " to " + p);
          System.out.println("Move: " + text + " to " + p);
        }

        @Override
        public void onError(Throwable throwable) {
        }

        @Override
        public void onCompleted() {
        }
      };
      client.connect(user, streamObserver);
    } finally {
//      client.shutdown();
    }
  }

  private Button createButton(Integer i) {
    Button button = new Button();
    button.setTabIndex(i);
    button.addClickListener(this::onClickListener);
    return button;
  }

  private void onClickListener(ClickEvent<Button> buttonClickEvent) {
    Button button = buttonClickEvent.getSource();
//    button.setText("X");
    client.makeMove(button.getTabIndex(), user);

//    List<Button> avalableButtons = map.values().stream()
//        .filter(b -> "".equals(b.getText()))
//        .collect(toList());
//
//    Collections.shuffle(avalableButtons);
//
//    Optional<Button> firstButton = avalableButtons.stream().findAny();
//
//    firstButton.ifPresent(b -> b.setText("O"));
  }
}
