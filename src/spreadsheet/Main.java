package spreadsheet;


import spreadsheet.gui.SpreadsheetGUI;

public class Main {

    private static final int DEFAULT_NUM_ROWS = 5000;
    private static final int DEFAULT_NUM_COLUMNS = 5000;

    public static void main(String[] args) {
        Spreadsheet spreadsheet = new Spreadsheet();
        SpreadsheetGUI spreadsheetGUI;
        switch (args.length) {
            case 2:
                spreadsheetGUI = new SpreadsheetGUI(spreadsheet, Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                break;
            default:
                spreadsheetGUI = new SpreadsheetGUI(spreadsheet, DEFAULT_NUM_ROWS, DEFAULT_NUM_COLUMNS);

        }
        spreadsheetGUI.start();

    }

}
