package a2;

import javafx.concurrent.Task;

public class WikitWorker extends Task<String> {
    private String _command;

    public WikitWorker(String command) {
        this._command = command;
    }

    @Override
    protected String call() throws Exception {

        _command = "wikit " + _command;
        String output = Terminal.command(_command);
        output = " " + output;

        return output;
    }
}
