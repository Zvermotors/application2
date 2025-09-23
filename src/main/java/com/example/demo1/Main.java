package com.example.demo1;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.beans.value.ObservableValue;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Главный класс приложения для работы с таблицей студентов.
 * Реализует функционал добавления, редактирования, удаления и поиска студентов.
 */
public class Main extends Application implements Initializable {

    // Компоненты UI, аннотированные для связи с FXML
    @FXML private TableView<Student> tableView;
    @FXML private TableColumn<Student, String> colSurname;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colPatronymic;
    @FXML private TableColumn<Student, Integer> colAge;
    @FXML private TableColumn<Student, String> colCity;
    @FXML private TableColumn<Student, String> colGroup;

    @FXML private TextField tfSearchSurname;
    @FXML private TextField tfSearchGroup;
    @FXML private Button btnAdd, btnEdit, btnDelete, btnSearch, btnReset;
    @FXML private Label lblLog;

    // Основной список студентов и путь к файлу данных
    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final String FILE_PATH = "C:\\Users\\Notebook\\OneDrive\\Desktop\\JavaFX LABA готовые\\laba2javafx\\students.txt";

    //-----------------------------------------------------------------------------------------------------------
    /**
     * Точка входа в JavaFX приложение.
     * Загружает FXML файл интерфейса и отображает главное окно.
     * @param stage главное окно приложения
     */
    @Override
    public void start(Stage stage) throws Exception {
        // Загрузка FXML файла с абсолютного пути
        FXMLLoader loader = new FXMLLoader(new URL("file:///C:\\Users\\Notebook\\OneDrive\\Desktop\\JavaFX LABA готовые\\laba2javafx\\src\\main\\resources\\com\\example\\demo1\\hello-view.fxml"));
        loader.setController(this); // Установка текущего класса в качестве контроллера
        AnchorPane root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle("Работа с таблицами");
        stage.setScene(scene);
        stage.show();
    }
    //-----------------------------------------------------------------------------------------------------------
    /**
     * Метод инициализации JavaFX контроллера.
     * Вызывается после загрузки FXML файла.
     * Настраивает таблицу, загружает данные и устанавливает обработчики событий.
     * @param url location used to resolve relative paths for the root object
     * @param rb resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Привязка колонок таблицы к свойствам класса Student
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPatronymic.setCellValueFactory(new PropertyValueFactory<>("patronymic"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        colGroup.setCellValueFactory(new PropertyValueFactory<>("group"));

        // Установка списка студентов в таблицу
        tableView.setItems(students);

        // Слушатель выбора элемента в таблице для отображения информации в логе
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
                lblLog.setText(newVal.toString());
        });

        // Загрузка данных из файла
        loadFromFile();

        // Установка обработчиков событий для кнопок
        btnAdd.setOnAction(this::onAdd);
        btnEdit.setOnAction(this::onEdit);
        btnDelete.setOnAction(this::onDelete);
        btnSearch.setOnAction(this::onSearch);
        btnReset.setOnAction(this::onReset);

        // Добавляем валидацию для полей поиска
        setupValidation();
    }
    //-----------------------------------------------------------------------------------------------------------
    /**
     * Настраивает валидацию для полей ввода
     */
    private void setupValidation() {
        // Валидация для поля поиска фамилии
        tfSearchSurname.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !newValue.matches("[а-яА-ЯёЁa-zA-Z\\s-]*")) {
                tfSearchSurname.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                lblLog.setText("Фамилия может содержать только буквы, пробелы и дефис");
            } else {
                tfSearchSurname.setStyle("");
            }
        });

        // Валидация для поля поиска группы
        tfSearchGroup.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !newValue.matches("[а-яА-ЯёЁa-zA-Z0-9-]*")) {
                tfSearchGroup.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                lblLog.setText("Группа может содержать только буквы, цифры и дефис");
            } else {
                tfSearchGroup.setStyle("");
            }
        });
    }
    //-----------------------------------------------------------------------------------------------------------
    /**
     * Загружает список студентов из текстового файла.
     * Формат файла: Фамилия;Имя;Отчество;Возраст;Город;Группа
     * Очищает текущий список перед загрузкой.
     */
    private void loadFromFile() {
        students.clear();
        Path path = Paths.get(FILE_PATH);
        if (Files.exists(path)) {
            try {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    String[] parts = line.split(";");
                    if (parts.length == 6) {
                        try {
                            // Валидация данных из файла
                            validateStudentData(parts[1], parts[0], parts[2], parts[3], parts[4], parts[5]);

                            // Создание нового студента из данных файла
                            Student s = new Student(parts[1], parts[0], parts[2],
                                    Integer.parseInt(parts[3]), parts[4], parts[5]);
                            students.add(s);
                        } catch (IllegalArgumentException e) {
                            lblLog.setText("Ошибка в данных файла: " + e.getMessage() + " в строке: " + line);
                        }
                    } else {
                        lblLog.setText("Некорректный формат строки: " + line);
                    }
                }
            } catch (IOException e) {
                lblLog.setText("Ошибка чтения файла: " + e.getMessage());
            } catch (Exception e) {
                lblLog.setText("Неизвестная ошибка при загрузке файла: " + e.getMessage());
            }
        } else {
            lblLog.setText("Файл не найден: " + FILE_PATH);
        }
    }
    //-----------------------------------------------------------------------------------------------------------
    /**
     * Валидирует данные студента
     */
    private void validateStudentData(String name, String surname, String patronymic, String ageStr, String city, String group) {
        // Проверка имени
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        if (!name.matches("[а-яА-ЯёЁa-zA-Z\\s-]+")) {
            throw new IllegalArgumentException("Имя содержит недопустимые символы");
        }

        // Проверка фамилии
        if (surname == null || surname.trim().isEmpty()) {
            throw new IllegalArgumentException("Фамилия не может быть пустой");
        }
        if (!surname.matches("[а-яА-ЯёЁa-zA-Z\\s-]+")) {
            throw new IllegalArgumentException("Фамилия содержит недопустимые символы");
        }

        // Проверка отчества
        if (patronymic == null || patronymic.trim().isEmpty()) {
            throw new IllegalArgumentException("Отчество не может быть пустым");
        }
        if (!patronymic.matches("[а-яА-ЯёЁa-zA-Z\\s-]+")) {
            throw new IllegalArgumentException("Отчество содержит недопустимые символы");
        }

        // Проверка возраста
        try {
            int age = Integer.parseInt(ageStr);
            if (age <= 0 || age > 120) {
                throw new IllegalArgumentException("Возраст должен быть от 1 до 120");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Возраст должен быть числом");
        }

        // Проверка города
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("Город не может быть пустым");
        }
        if (!city.matches("[а-яА-ЯёЁa-zA-Z\\s-]+")) {
            throw new IllegalArgumentException("Город содержит недопустимые символы");
        }

        // Проверка группы
        if (group == null || group.trim().isEmpty()) {
            throw new IllegalArgumentException("Группа не может быть пустой");
        }
        if (!group.matches("[а-яА-ЯёЁa-zA-Z0-9-]+")) {
            throw new IllegalArgumentException("Группа содержит недопустимые символы");
        }
    }
    //-----------------------------------------------------------------------------------------------------------
    /**
     * Обработчик кнопки "Добавить".
     * Создает нового студента и открывает диалоговое окно для ввода данных.
     * @param e событие нажатия кнопки
     */
    private void onAdd(ActionEvent e) {
        Student s = new Student();
        if (showDialog(s)) {
            students.add(s);
            saveToFile();
            lblLog.setText("Добавлен новый студент: " + s.getSurname());
        }
    }
    //-----------------------------------------------------------------------------------------------------------
    /**
     * Обработчик кнопки "Редактировать".
     * Открывает диалоговое окно для редактирования выбранного студента.
     * @param e событие нажатия кнопки
     */
    private void onEdit(ActionEvent e) {
        Student s = tableView.getSelectionModel().getSelectedItem();
        if (s != null) {
            if (showDialog(s)) {
                saveToFile();
                lblLog.setText("Данные студента обновлены: " + s.getSurname());
            }
        } else {
            lblLog.setText("Ошибка: Выберите студента для редактирования");
        }
    }
    //-----------------------------------------------------------------------------------------------------------
    /**
     * Обработчик кнопки "Удалить".
     * Удаляет выбранного студента из таблицы и списка.
     * @param e событие нажатия кнопки
     */
    private void onDelete(ActionEvent e) {
        Student s = tableView.getSelectionModel().getSelectedItem();
        if (s != null) {
            students.remove(s);
            saveToFile();
            lblLog.setText("Удалено: " + s.getSurname());
        } else {
            lblLog.setText("Ошибка: Выберите студента для удаления");
        }
    }
    //-----------------------------------------------------------------------------------------------------------
    /**
     * Сохраняет список студентов в файл
     */
    private void saveToFile() {
        try {
            Path path = Paths.get(FILE_PATH);
            List<String> lines = students.stream()
                    .map(s -> String.format("%s;%s;%s;%d;%s;%s",
                            s.getSurname(), s.getName(), s.getPatronymic(),
                            s.getAge(), s.getCity(), s.getGroup()))
                    .collect(Collectors.toList());
            Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            lblLog.setText("Ошибка сохранения файла: " + e.getMessage());
        }
    }
    //-----------------------------------------------------------------------------------------------------------
    /**
     * Обработчик кнопки "Поиск".
     * Выполняет фильтрацию студентов по фамилии и/или группе.
     * @param e событие нажатия кнопки
     */
    private void onSearch(ActionEvent e) {
        // Получаем и очищаем введенные данные
        String surname = tfSearchSurname.getText().trim();
        String group = tfSearchGroup.getText().trim();

        // Проверка на пустые поля (хотя бы одно должно быть заполнено)
        if (surname.isEmpty() && group.isEmpty()) {
            lblLog.setText("Ошибка: Введите фамилию или группу для поиска");
            return;
        }

        // Проверка формата фамилии (если поле не пустое)
        if (!surname.isEmpty() && !surname.matches("[а-яА-ЯёЁa-zA-Z\\s-]+")) {
            lblLog.setText("Ошибка: Фамилия может содержать только буквы, пробелы и дефис");
            tfSearchSurname.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            return;
        }

        // Проверка формата группы (если поле не пустое)
        if (!group.isEmpty() && !group.matches("[а-яА-ЯёЁa-zA-Z0-9-]+")) {
            lblLog.setText("Ошибка: Группа может содержать только буквы, цифры и дефис");
            tfSearchGroup.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            return;
        }

        try {
            // Приводим к нижнему регистру для поиска без учета регистра
            String surnameLower = surname.toLowerCase();
            String groupLower = group.toLowerCase();

            // Фильтрация студентов по заданным критериям
            List<Student> filtered = students.stream()
                    .filter(s -> (surname.isEmpty() || s.getSurname().toLowerCase().contains(surnameLower)) &&
                            (group.isEmpty() || s.getGroup().toLowerCase().contains(groupLower)))
                    .collect(Collectors.toList());

            // Отображаем результаты фильтрации в таблице
            tableView.setItems(FXCollections.observableArrayList(filtered));

            // Выводим информацию о количестве найденных результатов
            if (filtered.isEmpty()) {
                lblLog.setText("Студенты не найдены");
            } else {
                lblLog.setText("Найдено студентов: " + filtered.size());
            }

        } catch (Exception ex) {
            lblLog.setText("Ошибка при поиске: " + ex.getMessage());
        }
    }
    //-----------------------------------------------------------------------------------------------------------
    /**
     * Обработчик кнопки "Сброс".
     * Восстанавливает полный список студентов из файла.
     * @param e событие нажатия кнопки
     */
    private void onReset(ActionEvent e) {
        loadFromFile();
        tableView.setItems(students);
        tfSearchSurname.clear();
        tfSearchGroup.clear();
        tfSearchSurname.setStyle("");
        tfSearchGroup.setStyle("");
        lblLog.setText("Поиск сброшен, отображены все студенты");
    }
    //-----------------------------------------------------------------------------------------------------------
    /**
     * Отображает диалоговое окно для редактирования данных студента.
     * @param student объект студента для редактирования
     * @return true если пользователь нажал OK и данные прошли валидацию, иначе false
     */
    private boolean showDialog(Student student) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Данные студента");
        dialog.setHeaderText("Введите данные студента");

        GridPane grid = new GridPane();
        // Создание полей ввода с текущими значениями студента
        TextField tfName = new TextField(student.getName());
        TextField tfSurname = new TextField(student.getSurname());
        TextField tfPatronymic = new TextField(student.getPatronymic());
        TextField tfAge = new TextField(String.valueOf(student.getAge()));
        TextField tfCity = new TextField(student.getCity());
        TextField tfGroup = new TextField(student.getGroup());

        // Добавление валидации в реальном времени
        setupFieldValidation(tfName, "[а-яА-ЯёЁa-zA-Z\\s-]*", "Имя может содержать только буквы, пробелы и дефис");
        setupFieldValidation(tfSurname, "[а-яА-ЯёЁa-zA-Z\\s-]*", "Фамилия может содержать только буквы, пробелы и дефис");
        setupFieldValidation(tfPatronymic, "[а-яА-ЯёЁa-zA-Z\\s-]*", "Отчество может содержать только буквы, пробелы и дефис");
        setupFieldValidation(tfCity, "[а-яА-ЯёЁa-zA-Z\\s-]*", "Город может содержать только буквы, пробелы и дефис");
        setupFieldValidation(tfGroup, "[а-яА-ЯёЁa-zA-Z0-9-]*", "Группа может содержать только буквы, цифры и дефис");

        // Ограничение длины полей
        tfName.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= 50 ? change : null));
        tfSurname.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= 50 ? change : null));
        tfPatronymic.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= 50 ? change : null));
        tfCity.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= 50 ? change : null));
        tfGroup.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= 20 ? change : null));
        tfAge.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*") && change.getControlNewText().length() <= 3 ? change : null));

        // Добавление полей ввода на форму
        grid.addRow(0, new Label("Имя*:"), tfName);
        grid.addRow(1, new Label("Фамилия*:"), tfSurname);
        grid.addRow(2, new Label("Отчество*:"), tfPatronymic);
        grid.addRow(3, new Label("Возраст*:"), tfAge);
        grid.addRow(4, new Label("Город*:"), tfCity);
        grid.addRow(5, new Label("Группа*:"), tfGroup);

        // Настройка отступов между элементами формы
        grid.setVgap(10);
        grid.setHgap(10);

        // Установка содержимого диалогового окна
        dialog.getDialogPane().setContent(grid);
        // Добавление кнопок OK и Cancel
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Отображение диалога и ожидание ответа пользователя
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Валидация введенных данных
                if (tfName.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("Имя не может быть пустым");
                }
                if (!tfName.getText().trim().matches("[а-яА-ЯёЁa-zA-Z\\s-]+")) {
                    throw new IllegalArgumentException("Имя содержит недопустимые символы");
                }

                if (tfSurname.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("Фамилия не может быть пустой");
                }
                if (!tfSurname.getText().trim().matches("[а-яА-ЯёЁa-zA-Z\\s-]+")) {
                    throw new IllegalArgumentException("Фамилия содержит недопустимые символы");
                }

                if (tfPatronymic.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("Отчество не может быть пустым");
                }
                if (!tfPatronymic.getText().trim().matches("[а-яА-ЯёЁa-zA-Z\\s-]+")) {
                    throw new IllegalArgumentException("Отчество содержит недопустимые символы");
                }

                if (tfCity.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("Город не может быть пустым");
                }
                if (!tfCity.getText().trim().matches("[а-яА-ЯёЁa-zA-Z\\s-]+")) {
                    throw new IllegalArgumentException("Город содержит недопустимые символы");
                }

                int age;
                try {
                    age = Integer.parseInt(tfAge.getText().trim());
                    if (age <= 0 || age > 120) {
                        throw new IllegalArgumentException("Возраст должен быть от 1 до 120");
                    }
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Возраст должен быть числом");
                }

                if (tfGroup.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("Группа не может быть пустой");
                }
                if (!tfGroup.getText().trim().matches("[а-яА-ЯёЁa-zA-Z0-9-]+")) {
                    throw new IllegalArgumentException("Группа содержит недопустимые символы");
                }

                // Сохранение данных если все проверки пройдены
                student.setName(tfName.getText().trim());
                student.setSurname(tfSurname.getText().trim());
                student.setPatronymic(tfPatronymic.getText().trim());
                student.setAge(age);
                student.setCity(tfCity.getText().trim());
                student.setGroup(tfGroup.getText().trim());

                return true;

            } catch (IllegalArgumentException ex) {
                lblLog.setText("Ошибка: " + ex.getMessage());
                showAlert("Ошибка валидации", ex.getMessage());
            } catch (Exception ex) {
                lblLog.setText("Неизвестная ошибка: " + ex.getMessage());
                showAlert("Ошибка", "Произошла неизвестная ошибка: " + ex.getMessage());
            }
        }
        return false;
    }
    //-----------------------------------------------------------------------------------------------------------
    /**
     * Настраивает валидацию для поля ввода
     */
    private void setupFieldValidation(TextField field, String regex, String errorMessage) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !newValue.matches(regex)) {
                field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                lblLog.setText(errorMessage);
            } else {
                field.setStyle("");
            }
        });
    }
    //-----------------------------------------------------------------------------------------------------------
    /**
     * Показывает диалоговое окно с ошибкой
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    //-----------------------------------------------------------------------------------------------------------
    /**
     * Внутренний класс, представляющий студента.
     * Использует JavaFX Properties для совместимости с TableView.
     */
    public static class Student {
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty surname = new SimpleStringProperty();
        private final StringProperty patronymic = new SimpleStringProperty();
        private final IntegerProperty age = new SimpleIntegerProperty();
        private final StringProperty city = new SimpleStringProperty();
        private final StringProperty group = new SimpleStringProperty();

        /**
         * Конструктор по умолчанию.
         * Создает студента с пустыми значениями.
         */
        public Student() {
            this("", "", "", 0, "", "");
        }

        /**
         * Основной конструктор студента.
         * @param name имя студента
         * @param surname фамилия студента
         * @param patronymic отчество студента
         * @param age возраст студента
         * @param city город проживания
         * @param group учебная группа
         */
        public Student(String name, String surname, String patronymic, int age, String city, String group) {
            this.name.set(name);
            this.surname.set(surname);
            this.patronymic.set(patronymic);
            this.age.set(age);
            this.city.set(city);
            this.group.set(group);
        }

        // Геттеры, сеттеры и свойства для всех полей класса
        public String getName() { return name.get(); }
        public void setName(String value) { name.set(value); }
        public StringProperty nameProperty() { return name; }

        public String getSurname() { return surname.get(); }
        public void setSurname(String value) { surname.set(value); }
        public StringProperty surnameProperty() { return surname; }

        public String getPatronymic() { return patronymic.get(); }
        public void setPatronymic(String value) { patronymic.set(value); }
        public StringProperty patronymicProperty() { return patronymic; }

        public int getAge() { return age.get(); }
        public void setAge(int value) { age.set(value); }
        public IntegerProperty ageProperty() { return age; }

        public String getCity() { return city.get(); }
        public void setCity(String value) { city.set(value); }
        public StringProperty cityProperty() { return city; }

        public String getGroup() { return group.get(); }
        public void setGroup(String value) { group.set(value); }
        public StringProperty groupProperty() { return group; }

        /**
         * Возвращает строковое представление студента.
         * @return строка с данными студента
         */
        @Override
        public String toString() {
            return String.format("%s %s %s, %d лет, %s, группа %s",
                    surname.get(), name.get(), patronymic.get(), age.get(), city.get(), group.get());
        }
    }

    /**
     * Главный метод приложения.
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        launch(args);
    }
}