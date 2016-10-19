package editor;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.control.ScrollBar;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Editor extends Application {

    private EventHandler<KeyEvent> keyEventHandler;
    private Cursor cursor;
    private TextList textList;
    private LineRenderer lineRenderer;
    private ScrollBar scrollbar;
    private UndoRedo undoRedo;
    private Group root;
    private Group textRoot;
    private Scene scene;

    public File file;
    private String fileName;
    
    private int fontSize;
    private String fontName = "Verdana";

    private int windowWidth;
    private int windowHeight;

    private void setFile() {
        List<String> parameters = getParameters().getRaw();
        if (parameters.size() < 1) {
            System.out.println("Error: File name not given");
            System.exit(1);
        }
        fileName = parameters.get(0);
        file = new File(fileName);
        if (file.exists()) {
            open(file);
        }
    }

    private void setFont(int size) {
        fontSize = size;
        textList.setFont(fontName, fontSize);
        cursor.update();
    }

    private class ScrollBarListener implements  ChangeListener<Number> {
        @Override
        public void changed(ObservableValue<? extends  Number> observable,
                            Number oldValue, Number newValue) {
            int newLocation = newValue.intValue();
            scroll(newLocation);
        }
    }

    private class MouseClickEventHandler implements  EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent mouseEvent) {
            int mousePressedX = (int) Math.round(mouseEvent.getX());
            int mousePressedY = (int) Math.round(mouseEvent.getY());
            mouseClick(mousePressedX, mousePressedY);
        }
    }


    /** An EventHandler to handle keys that get pressed. */
    private class KeyEventHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED && !keyEvent.isShortcutDown()) {

                String characterTyped = keyEvent.getCharacter();

                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
                    // Ignore control keys, which have non-zero length, as well as the backspace key, which is
                    // represented as a character of value = 8 on Windows
                    if (characterTyped.equals("\r")) {
                        characterTyped = "\n";
                    }
                    insert(characterTyped);
                    keyEvent.consume();
                }

            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {

                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).
                KeyCode code = keyEvent.getCode();
                if (keyEvent.isShortcutDown()) {
                    if (code == KeyCode.S) {
                        save();
                    } else if (code == KeyCode.P){
                        printCursor();
                    } else if (code == KeyCode.PLUS || code == KeyCode.EQUALS) {
                        fontSizeUp();
                    } else if (code == KeyCode.MINUS) {
                        fontSizeDown();
                    } else if (code == KeyCode.Z) {
                        undo();
                    } else if (code == KeyCode.Y) {
                        redo();
                    }
                }
                if (code == KeyCode.LEFT) {
                    moveCursorLeft();
                } else if (code == KeyCode.RIGHT) {
                    moveCursorRight();
                } else if (code == KeyCode.UP){
                    moveCursorUp();
                } else if (code == KeyCode.DOWN) {
                    moveCursorDown();
                } else if (code == KeyCode.BACK_SPACE) {
                    backspace();
                }
            }
            }
        }

    public void insert(String inputString) {
        Text t = new Text(inputString);
        t.setFont(Font.font (fontName, fontSize));
        t.setTextOrigin(VPos.TOP);
        cursor.insertText(t);
        lineRenderer.render(cursor.cursorTextNode.getHeight());
        cursor.update();
        setScrollBarMax();
        textRoot.getChildren().add(t);
        updateScrollBar();
        UndoRedo.UndoAction action = undoRedo.createUndoAction(cursor.cursorTextNode, "insert");
        undoRedo.addUndo(action);
        undoRedo.resetRedo();
    }

    public void backspace() {
        if (!cursor.atBeginning()) {
            TextNode currText = cursor.cursorTextNode;
            textRoot.getChildren().remove(cursor.getCurrText());
            cursor.delete();
            lineRenderer.render(cursor.cursorTextNode.getHeight());
            setScrollBarMax();
            cursor.update();
            updateScrollBar();
            UndoRedo.UndoAction action = undoRedo.createUndoAction(currText, "delete");
            undoRedo.addUndo(action);
            undoRedo.resetRedo();
        }
    }

    public void fontSizeUp () {
        fontSize += 4;
        textList.setFont(fontName, fontSize);
        if (!textList.isEmpty()) {
            lineRenderer.render(cursor.cursorTextNode.getHeight());
            setScrollBarMax();
        }
        cursor.update();
    }

    public void fontSizeDown () {
        if (fontSize > 4) {
            setFont(fontSize - 4);
            textList.setFont(fontName, fontSize);
            if (!textList.isEmpty()) {
                lineRenderer.render(cursor.cursorTextNode.getHeight());
                setScrollBarMax();
            }
            cursor.update();
            if (lineRenderer.totalHeight() < windowHeight) {
                scroll(0);
            }
        }
    }

    public void moveCursorLeft() {
        cursor.moveLeft();
        updateScrollBar();
    }

    public void moveCursorRight() {
        cursor.moveRight();
        updateScrollBar();
    }

    public void moveCursorUp() {
        TextNode firstTextNodeAbove = lineRenderer.getLineAbove(cursor.getCursorYLocation(cursor.cursorTextNode));
        cursor.moveRightTo(firstTextNodeAbove, cursor.getCursorXLocation(cursor.cursorTextNode));
        updateScrollBar();
    }

    public void moveCursorDown() {
        TextNode firstTextNodeBelow = lineRenderer.getLineBelow(cursor.getCursorYLocation(cursor.cursorTextNode));
        cursor.moveRightTo(firstTextNodeBelow, cursor.getCursorXLocation(cursor.cursorTextNode));
        updateScrollBar();
    }

    public void mouseClick(int xPos, int yPos) {
        TextNode firstTextNodeOfLine = lineRenderer.getLineAt(yPos);
        cursor.moveRightTo(firstTextNodeOfLine, xPos);
    }

    public void setScrollBarMax() {
        if (lineRenderer.totalHeight() > windowHeight) {
            scrollbar.setMax(lineRenderer.totalHeight() - windowHeight);
        } else {
            scrollbar.setMax(0);
        }
    }

    public void scroll(int distance) {
        textRoot.setLayoutY(-distance);
        lineRenderer.setTopLocation(distance);
        cursor.update();
    }

    public void updateScrollBar() {
        int topOfCursorAbove = cursor.getCursorYLocation(cursor.cursorTextNode);
        int bottomOfCursorBelow = cursor.getCursorYLocation(cursor.cursorTextNode) + cursor.cursorTextNode.getHeight();
        if (topOfCursorAbove < lineRenderer.getTopLocation()) {
            scroll(topOfCursorAbove);
            scrollbar.setValue(topOfCursorAbove);
        } else if (bottomOfCursorBelow > windowHeight + lineRenderer.getTopLocation()) {
            scroll(bottomOfCursorBelow - windowHeight);
            scrollbar.setValue(bottomOfCursorBelow - windowHeight);
        }
    }

    public void undo() {
        if (undoRedo.noUndos()) {
            return;
        }
        UndoRedo.UndoAction action = undoRedo.getCurrentUndoAction();
        if (action.getType().equals("insert")) {
            textRoot.getChildren().remove(action.getTextNode().getText());
        } else if (action.getType().equals("delete")) {
            textRoot.getChildren().add(action.getTextNode().getText());
            action.getTextNode().getText().setFont(Font.font (fontName, fontSize));
            action.getTextNode().getText().setTextOrigin(VPos.TOP);
        }
        undoRedo.undo();
        lineRenderer.render(cursor.cursorTextNode.getHeight());
        cursor.update();
        setScrollBarMax();
        updateScrollBar();
    }

    public void redo() {
        if (undoRedo.noRedos()) {
            return;
        }
        UndoRedo.UndoAction action = undoRedo.getCurrentRedoAction();
        if (action.getType().equals("insert")) {
            textRoot.getChildren().add(action.getTextNode().getText());
            action.getTextNode().getText().setFont(Font.font (fontName, fontSize));
            action.getTextNode().getText().setTextOrigin(VPos.TOP);
        } else if (action.getType().equals("delete")) {
            textRoot.getChildren().remove(action.getTextNode().getText());
        }
        undoRedo.redo();
        lineRenderer.render(cursor.cursorTextNode.getHeight());
        cursor.update();
        setScrollBarMax();
        updateScrollBar();
    }

    public void open(File file) {
        try {
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            int intRead = -1;
            // Keep reading from the file input read() returns -1, which means the end of the file
            // was reached.
            while ((intRead = bufferedReader.read()) != -1) {
                // The integer read can be cast to a char, because we're assuming ASCII.
                char charRead = (char) intRead;
                String stringRead = Character.toString(charRead);
                if (stringRead.equals("\r\n")){
                    stringRead = "\n";
                }
                Text textRead = new Text(stringRead);
                cursor.insertText(textRead);
                textRead.setFont(Font.font (fontName, fontSize));
                textRead.setTextOrigin(VPos.TOP);
                textRoot.getChildren().add(textRead);
            }
            bufferedReader.close();
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("File not found! Exception was: " + fileNotFoundException);
            System.exit(1);
        } catch (IOException ioException) {
            System.out.println("Error when opening; exception was: " + ioException);
            System.exit(1);
        }
        cursor.update();
        if (!textList.isEmpty()) {
            lineRenderer.render(cursor.cursorTextNode.getHeight());
            setScrollBarMax();
        }
    }


    public void save() {
        try {
            FileWriter writer = new FileWriter(file);
            TextNode t = textList.getFirstTextNode();
            while (!textList.reachedEnd(t)) {
                String stringRead = t.toString();
                char charRead = stringRead.charAt(0);
                writer.write(charRead);
                t = t.getNext();
            }
            writer.close();
        } catch (Exception exception) {
            System.out.println("Error when writing; exception was: " + exception);
            System.exit(1);
        }
    }

    public void printCursor() {
        System.out.println(cursor.getXLocation() + ", " + cursor.getYLocation());
    }

    @Override
    public void start(Stage primaryStage) {
        windowWidth = 500;
        windowHeight = 500;

        root = new Group();
        textRoot = new Group();
        cursor = new Cursor();
        textList = new TextList();
        lineRenderer = new LineRenderer(470);
        scrollbar = new ScrollBar();
        undoRedo = new UndoRedo(textList);
        keyEventHandler = new KeyEventHandler();

        cursor.setTextList(textList);
        cursor.setMaxXLocation(475);
        lineRenderer.setTextList(textList);

        scrollbar.setMin(0);
        scrollbar.setValue(0);
        scrollbar.setLayoutX(windowWidth - scrollbar.getLayoutBounds().getWidth());
        scrollbar.setOrientation(Orientation.VERTICAL);
        scrollbar.setPrefHeight(windowHeight);

        setFont(12);
        setFile();

        setScrollBarMax();
        ScrollBarListener scrollListener = new ScrollBarListener();
        scrollbar.valueProperty().addListener(scrollListener);

        scene = new Scene(root, windowWidth, windowHeight, Color.WHITE);
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        scene.setOnMouseClicked(new MouseClickEventHandler());

        root.getChildren().add(textRoot);
        root.getChildren().add(scrollbar);
        textRoot.getChildren().add(cursor.cursorDisplay);
        cursor.makeCursorBlink();
        cursor.update();

        primaryStage.setTitle(fileName);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}