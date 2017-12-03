package com.example.andrew.myapp;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.Console;
import java.lang.reflect.Array;
import java.nio.file.FileAlreadyExistsException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public void showFinalDlg(Context context, String txt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(txt)
                .setMessage("Хотите сыграть ещё раз?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        newGame(btnList, valuesArr);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Спасибо за игру!", Toast.LENGTH_LONG).show();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public int fieldsRange = 4;
    public Boolean player = Boolean.TRUE;
    public Button[][] btnList = new Button[fieldsRange][fieldsRange];

    /*  -1 <-- "O"
         0 <-- " "
         1 <-- "X"   */

    public int[][] valuesArr = new int[fieldsRange][fieldsRange];

    // проверка на переполненное поле
    public boolean isFieldOverflow(int[][] valuesArr)
    {
        for (int ii = 0; ii < 4; ii++) {
            for (int jj = 0; jj < 4-1; jj++) {
                if (valuesArr[ii][jj] == 0)
                {
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }

    /*
    -1 - Computer win
     0 - Draw
     1 - Player win
     2 - Still gaming
     */
    public int checkWinner(int[][] valuesArr)
    {
        int v = 0;
        // rows
        for (int ii = 0; ii < 4; ii++) {
            if ((valuesArr[ii][0] == valuesArr[ii][1]) && (valuesArr[ii][2] == valuesArr[ii][3]) && (valuesArr[ii][1] == valuesArr[ii][2])) {
                if (valuesArr[ii][0] == 1) {
                    return 1;
                } else {
                    if (valuesArr[ii][0] == -1) {
                        return -1;
                    }
                }
            }
        }
        // columns
        for (int ii = 0; ii < 4; ii++) {
            if ((valuesArr[0][ii] == valuesArr[1][ii]) && (valuesArr[2][ii] == valuesArr[3][ii]) && (valuesArr[1][ii] == valuesArr[2][ii])) {
                if (valuesArr[0][ii] == 1) {
                    return 1;
                } else {
                    if (valuesArr[0][ii] == -1) {
                        return -1;
                    }
                }
            }
        }

        // diag
        if ((valuesArr[0][0] == valuesArr[1][1]) && (valuesArr[1][1] == valuesArr[2][2]) && (valuesArr[2][2] == valuesArr[3][3])) {
            if (valuesArr[0][0] == 1) {
                return 1;
            } else {
                if (valuesArr[0][0] == -1) {
                    return -1;
                }
            }
        }

        if ((valuesArr[0][3] == valuesArr[1][2]) && (valuesArr[1][2] == valuesArr[2][1]) && (valuesArr[2][1] == valuesArr[3][0])) {
            if (valuesArr[0][3] == 1) {
                return 1;
            } else {
                if (valuesArr[0][3] == -1) {
                    return -1;
                }
            }
        }

        boolean gameOver = (isFieldOverflow(valuesArr));
        if (gameOver){
            return 0; // ничья
        }
        return 2; // игра продолжается

    }
    /*
    // вектор доступных ходов
    // для точки [0,2]: (0*4)+2 = 2
    // для точки [2,3]: (2*4)+3 = 11
    // для точки [3,1]: (3*4)+1 = 13
    // незаполненные компаненты равны -1
    public int[] getAvailableMoves(int[][] valuesArr)
    {
        int answer[] = new int[16];
        int pos = 0;
        for (int i = 0; i<fieldsRange; i++)
        {
            for (int j = 0; j<fieldsRange; j++)
            {
                if (valuesArr[i][j] == 0)
                {
                    answer[pos] = i*fieldsRange + j;
                    pos += 1;
                }
            }
        }
        for (int i = pos; i<16; i++)
        {
            answer[i] = -1;
        }
        return answer;
    }
    */

    // возвращает List доступных ходов (пары координат, значения в которых пустые)
    private List<int[]> generateMoves(int[][] board)
    {
        List<int[]> nextMoves = new ArrayList<int[]>(); // List ходов (пар координат)
        if (isFieldOverflow(board) || (checkWinner(board) != 2)) {
            return nextMoves;   // return empty list
        }
        // поиск пар координат, значения в которых пустые
        for (int i = 0; i < fieldsRange; i++) {
            for (int j = 0; j < fieldsRange; j++) {
                if (board[i][j] == 0) {
                    nextMoves.add(new int[] {i, j});
                }
            }
        }
        return nextMoves;
    }

    // возвращает игровое поле с только что сделаным ходом hashMove игрока с меткой value (-1,1)
    public int[][] doMove(int[][] valuesArr, int row, int col, int value)
    {
        valuesArr[row][col] = value;
        return valuesArr;
    }

    // возвращает оценку положеия на игровом поле
    private int evaluate() {
        int score = 0;
        score += evaluateLine(0, 0, 0, 1, 0, 2, 0, 3);  // row 0
        score += evaluateLine(1, 0, 1, 1, 1, 2, 1, 3);  // row 1
        score += evaluateLine(2, 0, 2, 1, 2, 2, 2, 3);  // row 2
        score += evaluateLine(3, 0, 3, 1, 3, 2, 3, 3);  // row 3
        score += evaluateLine(0, 0, 1, 0, 2, 0, 3, 0);  // col 0
        score += evaluateLine(0, 1, 1, 1, 2, 1, 3, 1);  // col 1
        score += evaluateLine(0, 2, 1, 2, 2, 2, 3, 2);  // col 2
        score += evaluateLine(0, 3, 1, 3, 2, 3, 3, 3);  // col 3
        score += evaluateLine(0, 0, 1, 1, 2, 2, 3, 3);  // diagonal
        score += evaluateLine(0, 3, 1, 2, 2, 1, 3, 0);  // alternate diagonal
        return score;
    }

    /** функция оценки особых точек по 4м парам координат
     Возвращает след. значения: +1000, +100, +10, +1 for 4-, 3-, 2-, 1  (computer)
     -1000, -100, -10, -1 for 4-, 3-, 2-, 1  (opponent)
     0 не особое состояние */
    private int evaluateLine(int row1, int col1, int row2, int col2, int row3, int col3, int row4, int col4) {
        int score = 0;

        // First cell
        if (valuesArr[row1][col1] == 1) {
            score = 1;
        } else if (valuesArr[row1][col1] == -1) {
            score = -1;
        }

        // Second cell
        if (valuesArr[row2][col2] == 1) {
            if (score == 1) {   // ячейка_1 = 1
                score = 10;
            } else if (score == -1) {  // ячейка_1 = -1
                return 0;
            } else {  // ячейка_1 = empty
                score = 1;
            }
        } else if (valuesArr[row2][col2] == -1) {
            if (score == -1) { // ячейка_1 = -1
                score = -10;
            } else if (score == 1) { // ячейка_1 = 1
                return 0;
            } else {  // ячейка_1 = empty
                score = -1;
            }
        }

        // Third cell
        if (valuesArr[row3][col3] == 1) {
            if (score > 0) {  // ячейка_1 and/or ячейка_2 = 1
                score *= 10;
            } else if (score < 0) {  // ячейка_1 and/or ячейка_2 = -1
                return 0;
            } else {  // ячейка_1 и ячейка_2 пустые
                score = 1;
            }
        } else if (valuesArr[row3][col3] == -1) {
            if (score < 0) {  // ячейка_1 and/or ячейка_2 = -1
                score *= 10;
            } else if (score > 1) {  // ячейка_1 and/or ячейка_2 = 1
                return 0;
            } else {  // ячейка_1 и ячейка_2 пустые
                score = -1;
            }
        }

        // Forth cell
        if (valuesArr[row4][col4] == 1) {
            if (score > 0) {  // ячейка_1 and/or ячейка_2 = 1
                score *= 10;
            } else if (score < 0) {  // ячейка_1 and/or ячейка_2 = -1
                return 0;
            } else {  // ячейка_1 и ячейка_2 пустые
                score = 1;
            }
        } else if (valuesArr[row4][col4] == -1) {
            if (score < 0) {  // ячейка_1 and/or ячейка_2 = -1
                score *= 10;
            } else if (score > 1) {  // ячейка_1 and/or ячейка_2 = 1
                return 0;
            } else {  // ячейка_1 и ячейка_2 пустые
                score = -1;
            }
        }
        return score;
    }

    /** Minimax (recursive) at level of depth for maximizing or minimizing i_player
     with alpha-beta cut-off. Return int[3] of {score, row, col}  */
    private int[] minimax(int[][] board, int depth, int i_player, int alpha, int beta) {
        // Generate possible next moves in a list of int[2] of {row, col}.
        List<int[]> nextMoves = generateMoves(board);


        // 1 is maximizing; while -1 is minimizing
        int score;
        int bestRow = -1;
        int bestCol = -1;

        if (nextMoves.isEmpty() || depth == 0) {
            // Gameover or depth reached, evaluate score
            score = evaluate();
            return new int[] {score, bestRow, bestCol};
        } else {
            for (int[] move : nextMoves) {
                // try this move for the current "i_player"
                board[move[0]][move[1]] = i_player;
                if (i_player == 1) {  // 1 (computer) is maximizing i_player
                    score = minimax(board, depth - 1, -1, alpha, beta)[0];
                    if (score > alpha) {
                        alpha = score;
                        bestRow = move[0];
                        bestCol = move[1];
                    }
                } else {  // -1 is minimizing i_player
                    score = minimax(board, depth - 1, 1, alpha, beta)[0];
                    if (score < beta) {
                        beta = score;
                        bestRow = move[0];
                        bestCol = move[1];
                    }
                }
                // undo move
                board[move[0]][move[1]] = 0;
                // cut-off
                if (alpha >= beta) break;
            }
            return new int[] {(i_player == 1) ? alpha : beta, bestRow, bestCol};
        }
    }

    int[] move(int[][] valuesArr) {
        int[] result = minimax(valuesArr, 2, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return new int[] {result[1], result[2]};   // row, col
    }

    public void setGameValue(Button[][] btnList, int[][] valuesArr, int x, int y, int value){
        valuesArr[x][y] = value;
        if (value == 1){
            btnList[x][y].setText("X");
        }
        else {
            if (value == -1){
                btnList[x][y].setText("O");
            }
            else {
                btnList[x][y].setText("");
            }
        }
    }

    public boolean resetPlayer(Boolean player){
        return  Boolean.TRUE;
    }

    public boolean invertPlayer(Boolean player){
        return !player;
    }


    public void newGame(Button[][] btnList, int[][] valuesArr){
        for (int i = 0; i<fieldsRange; i++){
            for (int j = 0; j<fieldsRange; j++){
                setGameValue(btnList, valuesArr, i, j, 0);
                player = resetPlayer(player);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnList[0][0] = findViewById(R.id.b11);
        btnList[0][1] = findViewById(R.id.b12);
        btnList[0][2] = findViewById(R.id.b13);
        btnList[0][3] = findViewById(R.id.b14);

        btnList[1][0] = findViewById(R.id.b21);
        btnList[1][1] = findViewById(R.id.b22);
        btnList[1][2] = findViewById(R.id.b23);
        btnList[1][3] = findViewById(R.id.b24);

        btnList[2][0] = findViewById(R.id.b31);
        btnList[2][1] = findViewById(R.id.b32);
        btnList[2][2] = findViewById(R.id.b33);
        btnList[2][3] = findViewById(R.id.b34);

        btnList[3][0] = findViewById(R.id.b41);
        btnList[3][1] = findViewById(R.id.b42);
        btnList[3][2] = findViewById(R.id.b43);
        btnList[3][3] = findViewById(R.id.b44);

        newGame(btnList, valuesArr);
        Toast.makeText(MainActivity.this, "Крестики-нолики 4х4", Toast.LENGTH_LONG).show();

        final Context form = this;
        for (int i = 0; i<fieldsRange; i++){
            for (int j = 0; j<fieldsRange; j++){
                final Button btn = btnList[i][j];
                btn.setText("");
                final int ii = i;
                final int jj = j;

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int val, r, c;
                        int[] rc;
                        if (valuesArr[ii][jj] == 0) {
//                            if (player){
//                                val = 1;
//                                r = ii;
//                                c = jj;
//                            }
//                            else{
//                                val = -1;
//                                rc = move(valuesArr);
//                                r = rc[0];
//                                c = rc[1];
//                            }
                            setGameValue(btnList, valuesArr, ii, jj, 1);
                            int va = checkWinner(valuesArr);
                            if (va != 2) {
                                if (va == 1){
                                    showFinalDlg(form, "Вы победили!");
                                }
                                else if (va == -1){
                                    showFinalDlg(form, "Вы проиграли!");
                                }
                                else if (va == 0){
                                    showFinalDlg(form, "Ничья!");
                                }

                            }
                        }
                        rc = move(valuesArr);
                        r = rc[0];
                        c = rc[1];
                        if (valuesArr[r][c] == 0)
                        {
                            setGameValue(btnList, valuesArr, r, c, -1);
                            int va = checkWinner(valuesArr);
                            if (va != 2) {
                                if (va == 1){
                                    showFinalDlg(form, "Вы победили!");
                                }
                                else if (va == -1){
                                    showFinalDlg(form, "Вы проиграли!");
                                }
                                else if (va == 0){
                                    showFinalDlg(form, "Ничья!");
                                }


                            }
                            //player = invertPlayer(player);
                        }
                    }
                });
            }
        }


    }
}
