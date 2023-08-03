module io.xj.workstation.main {
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.graphics;

  opens io.xj.workstation to javafx.graphics;
}
