package com.lohika.jclub.ttt.vaadinui;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.router.Route;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Route("")
public class GameComponent extends Div {

  private Map<Integer, Button> map;

  public GameComponent() {
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
  }

  private Button createButton(Integer i) {
    Button button = new Button();
    button.setTabIndex(i);
    button.addClickListener(this::onClickListener);
    return button;
  }

  private void onClickListener(ClickEvent<Button> buttonClickEvent) {
    Button button = buttonClickEvent.getSource();
    button.setText("X");

    List<Button> avalableButtons = map.values().stream()
        .filter(b -> "".equals(b.getText()))
        .collect(toList());

    Collections.shuffle(avalableButtons);

    Optional<Button> firstButton = avalableButtons.stream().findAny();

    firstButton.ifPresent(b -> b.setText("O"));
  }
}
