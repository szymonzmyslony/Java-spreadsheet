package spreadsheet;

import spreadsheet.api.CellLocation;
import spreadsheet.api.ExpressionUtils;
import spreadsheet.api.observer.Observer;
import spreadsheet.api.value.InvalidValue;
import spreadsheet.api.value.Value;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Szymon on 17.02.2016.
 */
public class Cell implements Observer<Cell> {
    private CellLocation cellLocation;
    private Spreadsheet spreadsheet;
    private String expression = "";
    private Value value = null;
    private Set<Cell> setReferences= new HashSet();
    private Set<Observer<Cell>> changedRefereneces = new HashSet<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cell cell = (Cell) o;

        if (cellLocation != null ? !cellLocation.equals(cell.cellLocation) : cell.cellLocation != null) return false;
        return spreadsheet != null ? spreadsheet.equals(cell.spreadsheet) : cell.spreadsheet == null;

    }

    @Override
    public int hashCode() {
        int result = cellLocation != null ? cellLocation.hashCode() : 0;
        result = 31 * result + (spreadsheet != null ? spreadsheet.hashCode() : 0);
        return result;
    }

    public Cell(CellLocation cellLocation, Spreadsheet spreadsheet) {
        this.cellLocation = cellLocation;
        this.spreadsheet = spreadsheet;

    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        setReferences.clear();
        this.expression = expression;
        value = new InvalidValue(expression);
        spreadsheet.add(this);
        for (CellLocation l : (ExpressionUtils.getReferencedLocations(expression))){
            Cell c = new Cell(l,spreadsheet);
            setReferences.add(c);
        }
        changedRefereneces.notifyAll();
    }

   private void removeObserver(Observer<Cell> observer){

   }


    @Override
    public void update(Cell changed) {
        if (!spreadsheet.isIn(changed)){
            spreadsheet.recompute();
            changed.setValue(new InvalidValue(changed.expression));
            changed.changedRefereneces.notifyAll();

        }



    }
}
