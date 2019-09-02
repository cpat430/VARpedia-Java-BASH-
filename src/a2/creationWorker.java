package a2;

import javafx.concurrent.Task;

public class creationWorker extends Task<Void> {

    private String _command1;
    private String _command2;

    public creationWorker(String command1, String command2) {
        this._command1 = command1;
        this._command2 = command2;
    }

    @Override
    protected Void call() throws Exception {

        // Create a creation
        Terminal.command(_command1);
        Terminal.command(_command2);
        return null;
    }
}
