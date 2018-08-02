package com.lohika.jclub.ttt.vaadinui;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import io.grpc.stub.StreamObserver;
import org.javaclub.grpc.tictactoeserver.MoveResponse;
import org.javaclub.grpc.tictactoeserver.PlayerResponse;
import org.javaclub.grpc.tictactoeserver.Point;

import static java.util.stream.Collectors.toMap;

@Route("")
@Push(PushMode.AUTOMATIC)
public class GameComponent extends Div {

  private static final Logger logger = Logger.getLogger(GameComponent.class.getName());

  private Map<Integer, Button> map;
  private TicTacToeClient client;
  private final String user;

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
    /* Access a service running on the local machine on port 50051 */
    user = UUID.randomUUID().toString();
    client.connect(user, new StreamObserver<PlayerResponse>() {
      @Override
      public void onNext(PlayerResponse playerResponse) {
        Point point = playerResponse.getPoint();
        Integer p = point.getY() + point.getX() + (2 * point.getX());
        String text = playerResponse.getChar().name();
        logger.info("Move: " + text + " to " + p);
        setText(text, map.get(p));
      }

      @Override
      public void onError(Throwable throwable) {
        logger.log(Level.SEVERE, "Error: ", throwable);
      }

      @Override
      public void onCompleted() {
        logger.log(Level.INFO, "Game finished!");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        map.forEach((k, v) -> setText("", v));
      }
    });
  }

  private Button createButton(Integer i) {
    Button button = new Button();
    button.setTabIndex(i);
    StreamObserver<MoveResponse> observer = new StreamObserver<MoveResponse>() {
      @Override
      public void onNext(MoveResponse moveResponse) {
        logger.info("Status: " + moveResponse.getSuccess());
      }

      @Override
      public void onError(Throwable throwable) {
        logger.log(Level.SEVERE, "Error: ", throwable);
      }

      @Override
      public void onCompleted() {
      }
    };
    button.addClickListener(e -> {
      client.makeMove(e.getSource().getTabIndex(), user, observer);
    });
    return button;
  }

  private void setText(String text, Button button) {
    Optional<UI> ui = button.getUI();
    ui.map(UI::getSession).ifPresent(s -> {
      s.lock();
      try {
        button.setText(text);
      } finally {
        s.unlock();
      }
    });
  }

}
