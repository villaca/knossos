package br.unirio.edd2.Comparators;

import br.unirio.edd2.Cell;

import java.util.Comparator;

/**
 * Auxiliary class that specifies that the cells will be sorted
 * according their 'f' field
 */
public class CellComparatorByF implements Comparator<Cell> {
    @Override
    public int compare(Cell cell1, Cell cell2){
        return cell1.f-cell2.f;
    }
} // end nested class CellComparatorByF