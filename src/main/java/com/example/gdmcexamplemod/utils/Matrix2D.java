package com.example.gdmcexamplemod.utils;

import java.util.Iterator;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Represents a 2 dimension Matrix, with several accessors, and basic operations.
 *
 * The matrix dimension are like this:
 *                               width
 *          _____________________________________
 *          |  0  10  20  30  40  50  60  70  80 |
 *          |  1  11  21  31  41  51  61  71  81 |
 *          |  2  12  22  32  42  52  62  72  82 |
 *  length  |  3  13  23  33  43  53  63  73  83 |
 *          |  4  14  24  34  44  54  64  74  84 |
 *          |  5  15  25  35  45  55  65  75  85 |
 *          |  6  16  26  36  46  56  66  76  86 |
 *          |____________________________________|
 *
 * matrix.get(5, 0) = 40;
 * matrix.get(2, 5) = 14
 *
 * @param <T> The type of objects stored in the matrix.
 */
@SuppressWarnings("unchecked")
public class Matrix2D<T> implements Iterable<T> {
    
    private final int width;
    private final int length;
    private final T[][] matrix;
    
    public Matrix2D(int width, int length) {
        this.width = width;
        this.length = length;

        matrix = (T[][])new Object[length][width];
    }

    public Matrix2D(int width, int length, T fillerValue) {
        this.width = width;
        this.length = length;

        matrix = (T[][])new Object[length][width];
        fill(fillerValue);
    }

    /**
     *  Get the element at position (i, j) in the matrix
     *
     * @param i stands for the abscissa
     * @param j stands for the ordinate
     * @return the element at position (i, j)
     */
    public T get(int i, int j) {

        if (!coordinateInMatrix(i, j)) throw new IllegalArgumentException("Coordinate must be in matrix boundaries");

        return matrix[j][i];
    }

    public void set(int i, int j, T value) {

        if (!coordinateInMatrix(i, j)) throw new IllegalArgumentException("Coordinate must be in matrix boundaries");

        matrix[j][i] = value;
    }

    public Row getRow(int j) {

        if (!indexInLength(j)) throw new IllegalArgumentException("j must be : 0 <= j < matrix.length");

        return new Row(j);
    }

    public Column getColumn(int i) {

        if (!indexInWidth(i)) throw new IllegalArgumentException("i must be : 0 <= i < matrix.width");

        return new Column(i);
    }

    public SubMatrix2D getSubMatrix(int i1, int j1, int i2, int j2) {

        if (!coordinateInMatrix(i1, j1)) throw new IllegalArgumentException("First point in out of matrix boundaries");
        if (!coordinateInMatrix(i2, j2)) throw new IllegalArgumentException("Second point in out of matrix boundaries");

        int minI = min(i1, i2);
        int maxI = max(i1, i2);
        int minJ = min(j1, j2);
        int maxJ = max(j1, j2);

        return new SubMatrix2D(minI, minJ, maxI, maxJ);
    }

    public void setRow(int rowIndex, T[] rowValues) {

        if (rowValues.length != width) throw new IllegalArgumentException("The row's size must match the width of the matrix");

        Row rowToModify = getRow(rowIndex);
        setRow(rowToModify, rowValues);
    }

    public void setRow(Row row, T[] rowValues) {

        if (rowValues.length != width) throw new IllegalArgumentException("The row's size must match the width of the matrix");

        for  (int i = 0; i < width; i++) {
            row.set(i, rowValues[i]);
        }
    }

    public void setColumn(int columnIndex, T[] columnValues) {

        if (columnValues.length != length) throw new IllegalArgumentException("The row's size must match the width of the matrix");

        Column columnToModify = getColumn(columnIndex);
        setColumn(columnToModify, columnValues);
    }

    public void setColumn(Column column, T[] columnValues) {

        if (columnValues.length != length) throw new IllegalArgumentException("The row's size must match the width of the matrix");

        for  (int j = 0; j < length; j++) {
            column.set(j, columnValues[j]);
        }
    }

    public void setSubMatrix(int i1, int j1, int i2, int j2, Matrix2D<T> matrix) {

        SubMatrix2D subMatrix = getSubMatrix(i1, j1, i2, j2);

        setSubMatrix(subMatrix, matrix);
    }

    public void setSubMatrix(SubMatrix2D subMatrix, Matrix2D<T> matrix) {

        if (subMatrix.length != matrix.length || subMatrix.width != matrix.width) throw new IllegalArgumentException("SubMatrix and given matrix must have same dimension");

        for (int i = 0; i < subMatrix.width; i++) {
            for (int j = 0; j < subMatrix.length; j++) {
                subMatrix.setRelative(i, j, matrix.get(i, j));
            }
        }
    }

    public void fill(T value) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < length; j++) {
                set(i, j, value);
            }
        }
    }

    public boolean indexInWidth(int index) {
        return 0 <= index && index < width;
    }

    public boolean indexInLength(int index) {
        return 0 <= index && index < length;
    }

    public boolean coordinateInMatrix(int i, int j) {
        return indexInWidth(i) && indexInLength(j);
    }

    @Override
    public Iterator<T> iterator() {
        return new MainIterator();
    }

    //TODO: Test all methods
    //TODO: Adapt all for Point2D

    class Row implements Iterable<T> {

        private int index;

        private Row(int j) {
            assert 0 <= j && j < length;
            index = j;
        }

        public T get(int x) {
            return Matrix2D.this.get(x, index);
        }

        public void set(int x, T value) {
            Matrix2D.this.set(x, index, value);
        }

        @Override
        public Iterator<T> iterator() {
            return new RowIterator();
        }

        class RowIterator implements  Iterator<T> {

            private final int i = index;
            private int j = 0;

            @Override
            public boolean hasNext() {
                return j < length - 1;
            }

            @Override
            public T next() {
                T data = Matrix2D.this.get(i, j);
                j++;
                return data;
            }
        }

    }

    class Column implements Iterable<T> {

        private int index;

        private Column(int i) {
            assert 0 <= i && i < width;
            index = i;
        }

        public T get(int x) {
            return Matrix2D.this.get(index, x);
        }

        public void set(int x, T value) {
            Matrix2D.this.set(index, x, value);
        }

        @Override
        public Iterator<T> iterator() {
            return new ColumnIterator();
        }

        class ColumnIterator implements  Iterator<T> {

            private final int j = index;
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < length - 1;
            }

            @Override
            public T next() {
                T data = Matrix2D.this.get(i, j);
                i++;
                return data;
            }
        }

    }

    class SubMatrix2D implements Iterable<T> {

        private int originI;
        private int originJ;
        private int width;
        private int length;

        private SubMatrix2D(int i1, int j1, int i2, int j2) {
            assert i1 < i2 && j1 < j2;
            this.originI = i1;
            this.originJ = j1;
            this.width = i2 - i1 + 1;
            this.length = j2 - j1 + 1;
        }

        public T getRelative(int relativeI, int relativeJ) {

            if (!relativeCoordinateInSubMatrix(relativeI, relativeJ)) throw new IllegalArgumentException("Coordinate must be in matrix boundaries");

            return Matrix2D.this.get(originI + relativeI, originJ + relativeJ);
        }

        public void setRelative(int relativeI, int relativeJ, T value) {

            if (!relativeCoordinateInSubMatrix(relativeI, relativeJ)) throw new IllegalArgumentException("Coordinate must be in matrix boundaries");

            Matrix2D.this.set(originI + relativeI, originJ + relativeJ, value);
        }

        public int getSubWidth() {
            return width;
        }

        public int getSubLength() {
            return length;
        }

        public Matrix2D<T> extractSubMatrix() {
            Matrix2D<T> extractedMatrix = new Matrix2D<>(width, length);

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < length; j++) {
                    extractedMatrix.set(i, j, getRelative(i, j));
                }
            }

            return extractedMatrix;
        }

        public boolean indexInSubWidth(int index) {
            return 0 <= index && index < width;
        }

        public boolean indexInSubLength(int index) {
            return 0 <= index && index < length;
        }

        public boolean relativeCoordinateInSubMatrix(int i, int j) {
            return indexInSubWidth(i) && indexInSubLength(j);
        }

        @Override
        public Iterator<T> iterator() {
            return new SubMatrixIterator();
        }

        class SubMatrixIterator implements Iterator<T> {

            private int i = 0;
            private int j = 0;

            @Override
            public boolean hasNext() {
                if (j < length - 1) {
                    return true;
                } else {
                    return i < width - 1;
                }
            }

            @Override
            public T next() {
                T data = getRelative(i, j);
                if (j < length - 1) {
                    j++;
                } else {
                    j = 0;
                    i++;
                }
                return data;
            }

        }

    }

    class MainIterator implements Iterator<T> {

        private int i = 0;
        private int j = 0;

        @Override
        public boolean hasNext() {
            if (j < length - 1) {
                return true;
            } else {
                return i < width - 1;
            }
        }

        @Override
        public T next() {
            T data = get(i, j);
            if (j < length - 1) {
                j++;
            } else {
                j = 0;
                i++;
            }
            return data;
        }
    }

}
