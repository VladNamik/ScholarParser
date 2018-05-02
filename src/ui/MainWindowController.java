package ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import utils.ExcelUtils;
import web.GSCUser;
import web.ScholarParser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainWindowController {

    @FXML
    private TableView<GSCUser> mainList;
    private ObservableList<GSCUser> mainObservableList = FXCollections.observableArrayList();
    private List<GSCUser> lastDownloadedUsers = null;

    @FXML
    private Text bottomText;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private TextField urlField;


    public MainWindowController() {
    }


    @FXML
    private void initialize() {
        mainList.setItems(mainObservableList);
        TableColumn<GSCUser, String> nameCol = new TableColumn<>("Name");
        TableColumn<GSCUser, String> idCol = new TableColumn<>("Id");
        TableColumn<GSCUser, Integer> citationsCol = new TableColumn<>("Citations");
        TableColumn<GSCUser, Integer> hIndexCol = new TableColumn<>("H-index");
        TableColumn<GSCUser, Integer> presenceCol = new TableColumn<>("Presence");
        TableColumn<GSCUser, String> pageLinkCol = new TableColumn<>("Page link");

        nameCol.setCellValueFactory(
                new PropertyValueFactory<>("shortedName")
        );
        idCol.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );
        citationsCol.setCellValueFactory(
                new PropertyValueFactory<>("citationStatistics")
        );
        hIndexCol.setCellValueFactory(
                new PropertyValueFactory<>("hIndex")
        );
        presenceCol.setCellValueFactory(
                new PropertyValueFactory<>("presence")
        );
        pageLinkCol.setCellValueFactory(
                new PropertyValueFactory<>("userPageURL")
        );

        nameCol.setMinWidth(140);
        idCol.setMinWidth(140);
        citationsCol.setMinWidth(60);
        hIndexCol.setMinWidth(60);
        presenceCol.setMinWidth(60);
        pageLinkCol.setMinWidth(250);

        mainList.getColumns().addAll(nameCol, idCol, citationsCol, hIndexCol, presenceCol, pageLinkCol);
    }


    @FXML
    private void onParseButtonClicked()
    {
        beginConnecting();
        new Thread(() ->{
            try {
                if (mainObservableList.size() > 0) {
                   mainObservableList.clear();
                }
                lastDownloadedUsers = ScholarParser.parse(urlField.getText(), (userNumber) ->
                        Platform.runLater(() -> updateLastUserNumber(userNumber)));
                Platform.runLater(() -> {
                    lastDownloadedUsers.forEach((user) -> mainObservableList.add(user));
                    endParsing("Parsing finished successfully");
                });
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Platform.runLater(this::connectionErrorAlert);
            }
            catch (NullPointerException | IllegalArgumentException e)
            {
                Platform.runLater(this::errorInData);
            }
        }
        ).start();
    }

    @FXML
    private void onSaveInXLSButtonClicked() {
        if (lastDownloadedUsers == null) {
            noDataToSaveError();
            return;
        }
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Microsoft Excel files (*.xls)", "*.xls");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(MainWindow.getPrimaryStage());
        try {
            String path = file.getAbsolutePath();
            if (!path.endsWith(".xls")) {
                path += ".xls";
            }
            File destFile = new File(path);
            destFile.setWritable(true);
            ExcelUtils.save(destFile, lastDownloadedUsers);
            endParsing("Saved in " + path);
        } catch (IOException e) {
            savingErrorAlert();
        }
    }

    @FXML
    private void onUpdateButtonClicked() {
        try {
            FileChooser fileChooser = new FileChooser();

            //Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Microsoft Excel files (*.xls)", "*.xls");
            fileChooser.getExtensionFilters().add(extFilter);

            //Show open file dialog
            File file = fileChooser.showOpenDialog(MainWindow.getPrimaryStage());


            //Id reading
            lastDownloadedUsers = new ArrayList<>();
            try {
                List<String> ids = ExcelUtils.readIds(file);
                for (String id : ids) {
                    lastDownloadedUsers.add(new GSCUser("", GSCUser.getUserPageURLFromId(id)));
                }
            } catch (IOException e) {
                e.printStackTrace();
                unknownError(e);
            }

            //Data parsing
            beginConnecting();
            new Thread(() -> {
                try {
                    try {
                        Pair<List<GSCUser>, List<Integer>> parsedData = ScholarParser.parseUsersByIds(lastDownloadedUsers, (userNumber) ->
                                Platform.runLater(() -> updateLastUserNumber(userNumber)));
                        lastDownloadedUsers = parsedData.getKey();
                        if (parsedData.getValue().size() > 0) {
                            Platform.runLater(() -> errorInDataWhileUpdating(parsedData.getValue()));
                        }
                        Platform.runLater(() -> endParsing("Parsing finished successfully"));
                    } catch (IOException e) {
                        e.printStackTrace();
                        Platform.runLater(this::connectionErrorAlert);
                    } catch (NullPointerException | IllegalArgumentException e) {
                        e.printStackTrace();
                        Platform.runLater(this::errorInData);
                    }

                    if (lastDownloadedUsers.size() == 0) {
                        return;
                    }


                    //Updating
                    try {
                        ExcelUtils.update(file, lastDownloadedUsers);
                        endParsing("Saved in " + file.getAbsolutePath());
                    } catch (IOException e) {
                        Platform.runLater(this::savingErrorAlert);
                    }
                } catch (Exception e) {
                    unknownError(e);
                }
            }
            ).start();
        } catch (Exception e) {
            unknownError(e);
        }
    }

    private void beginConnecting()
    {
        progressBar.setManaged(true);
        progressBar.setVisible(true);
        bottomText.setText("Connecting...");
    }

    private void updateLastUserNumber(int number) {
        bottomText.setText("Parsing... already have " + number + " users.");
    }

    private void endParsing(String text)
    {
        progressBar.setProgress(1);
        bottomText.setText(text);
        progressBar.setManaged(false);
        progressBar.setVisible(false);
        progressBar.setProgress(0);
    }

    private void savingErrorAlert() {
        System.out.println(ScholarParser.currentlyParsedPagesCount);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ошибка при попытке сохранения");
        alert.setHeaderText("Ошибка при попытке сохранения");

        alert.showAndWait();
        endParsing("Saving has failed");
    }

    private void connectionErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Проблема доступа к данным");
        alert.setHeaderText("Проблема доступа к удалённым данным");
        alert.setContentText("Убедитесь, что ссылка введена верно, и присутствует подключение к сети");

        alert.showAndWait();
        endParsing("Parsing has failed");
    }

    private void errorInData() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ошибка в данных");
        alert.setHeaderText("Ошибка в данных");
        alert.setContentText("Убедитесь, что все данные для элементов введены правильно");

        alert.showAndWait();
        endParsing("Parsing has failed");
    }

    private void errorInDataWhileUpdating(List<Integer> indices) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ошибка в данных");
        alert.setHeaderText("Ошибка в данных");
        StringBuilder message = new StringBuilder();
        message.append("Несколько пользователей было пропущенно. Проверьте правильность id пользователей в строках:\n");
        for (Integer index: indices) {
            message.append(index);
            message.append(", ");
        }
        message.deleteCharAt(message.length() - 1);
        message.deleteCharAt(message.length() - 1);
        message.append('.');

        alert.setContentText(message.toString());

        alert.showAndWait();
    }

    private void noDataToSaveError() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Данные для сохранения отсутствуют");
        alert.setHeaderText("Нет данных для сохранения");
        alert.setContentText("Пожалуйста, нажмите сначала кнопку \"parse\"");

        alert.showAndWait();
    }

    private void unknownError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Неизвестная ошибка");
        alert.setHeaderText("Неизвестная ошибка");
        alert.setContentText(e.getLocalizedMessage());

        alert.showAndWait();
    }

//    private class MainListItem extends HBox
//    {
//        GSCUser user;
//        public MainListItem(GSCUser user)
//        {
//            this.user = user;
//
//            Text name = new Text(user.getShortedName());
//            this.getChildren().add(name);
//
//            Text id = new Text(user.getId());
//            this.getChildren().add(id);
//
//
//            Text userPageURL = new Text(user.getUserPageURL());
//
////            userPageURL.setWrappingWidth(MainWindow.getPrimaryStage().getWidth()-70.0);
//            userPageURL.setOnMouseClicked((event) ->
//            {
//                if(event.getButton().equals(MouseButton.PRIMARY)){
//                    if(event.getClickCount() == 2){
//                        try {
//                            Desktop.getDesktop().browse((new URL(user.getUserPageURL())).toURI());
//                        } catch (IOException | URISyntaxException e)
//                        {
//                            Alert alert = new Alert(Alert.AlertType.WARNING);
//                            alert.setTitle("Неверное URL");
//                            alert.setHeaderText("Проблемы при попытке перейти по заданному адресу");
//                            alert.setContentText("Убедитесь в том, что url введено правильно и присутствует подключение к сети");
//                            alert.showAndWait();
//                        }
//                    }
//                }
//            });
//            this.getChildren().add(userPageURL);
//        }
//    }
}
