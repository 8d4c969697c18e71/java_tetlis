import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class Main extends JFrame implements KeyListener {
  private Label label;
  
	private int[][] map = new int[23][10];
  private static HashMap<String, List<Integer>> direction;
  private int current_mino_idx;
  private int current_base_x;
  private int current_base_y;
  private boolean gameover_flag = false;

  // difficulty
  private int score = 0;
  private int lines = 0;
  private int level = 0;  //TODO
  private int drop_ratio = 20000;

  private static int next_base_x;
  private static int next_base_y;
  private static int minos[][][] = { // base pos: {5,0}
    {{-2,0},{-1,0},{0,0},{1,0}}, // I0
    {{-1,1},{-1,0},{0,0},{0,1}}, // O1
    {{-1,0},{0,1},{0,0},{1,0}}, // T2
    {{-1,0},{1,0},{0,0},{-1,1}}, // L3
    {{-1,0},{1,0},{0,0},{1,1}}, // J4
    {{-1,1},{0,1},{0,0},{1,0}}, // S5
    {{-1,0},{0,0},{0,1},{1,1}}, // Z6
  };

  // mino design
  // 0: space, 1: placed, 2: active
  private static char[] mino_left = {'.', '[', '{'};
  private static char[] mino_right = {' ', ']', '}'};

  public Main(){
    // make frame
    super("Java Tetlis");
    setSize(350, 600);
    setResizable(false);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    getContentPane().setBackground(Color.black);
    setVisible(true);
    addKeyListener(this);

    // initialize label
    label = new Label(Color.green, "Monospaced", 16);
    add(label, BorderLayout.PAGE_START);

		// initialize map
		for(int i=0; i<map.length; i++){
			for(int j=0; j<map[i].length; j++){
				map[i][j] = 0;
			}
		}

    // initialize info
    next_base_x = map[0].length/2;
    next_base_y = map.length-20;

    // initialize direction map
    direction = new HashMap<>();
    direction.put("left", new ArrayList<>(Arrays.asList(-1,0)));
    direction.put("right", new ArrayList<>(Arrays.asList(1,0)));
    direction.put("down", new ArrayList<>(Arrays.asList(0,1)));
  }

  // update display
  public void update(int[][] map){
    String str = "<html>";
    
    // add info
    str = (new StringBuilder(String.valueOf(str))).append("SCORE: ").append(score).append("<br>").toString();
    str = (new StringBuilder(String.valueOf(str))).append("LINES: ").append(lines).append("<br>").toString();
    str = (new StringBuilder(String.valueOf(str))).append("LEVEL: ").append(level).append("<br>").toString();

    // reflect map
    for(int i=map.length-20; i<map.length; i++){
      str = (new StringBuilder(String.valueOf(str))).append("|").toString();
      for(int j=0; j<map[i].length; j++){
        str = (new StringBuilder(String.valueOf(str))).append(mino_left[map[i][j]]).toString();
        str = (new StringBuilder(String.valueOf(str))).append(mino_right[map[i][j]]).toString();
      }
      str = (new StringBuilder(String.valueOf(str))).append("|<br>").toString();
    }

    // bottom line
    str = (new StringBuilder(String.valueOf(str))).append("|").toString();
    for(int j=0; j<map[0].length; j++){
      str = (new StringBuilder(String.valueOf(str))).append("-").toString();
      str = (new StringBuilder(String.valueOf(str))).append("-").toString();
    }
    str = (new StringBuilder(String.valueOf(str))).append("|").toString();

    // html end tag
    str = (new StringBuilder(String.valueOf(str))).append("</html>").toString();
    
    // reflect to label
    label.setText(str);
  }

  // ===========================================================

  @Override
  public void keyTyped(KeyEvent event){}

  @Override
  public void keyPressed(KeyEvent event){
    if(gameover_flag) return;
    switch (event.getKeyCode()) {
      case KeyEvent.VK_LEFT:
        move("left");
        break;
    
      case KeyEvent.VK_RIGHT:
        move("right");
        break;
      
      case KeyEvent.VK_DOWN:
        drop();
        break;

      default:
        break;
    }
  }

  @Override
  public void keyReleased(KeyEvent event){
    switch (event.getKeyCode()) {
      case KeyEvent.VK_Z:
        if(gameover_flag) return;
        rotate(true);
        break;

      case KeyEvent.VK_X:
        if(gameover_flag) return;
        rotate(false);
        break;

      case KeyEvent.VK_ESCAPE:
        this.dispose();
        System.exit(0);
        break;
    
      default:
        break;
    }
  }

  // ===========================================================

  // @param boolean dir:
  // true: left, false: right
  public boolean rotate(boolean dir){
    // cannot rotate when next to wall
    if(current_base_x<=0 || current_base_x>=map[0].length-1 || current_base_y<=0 || current_base_y>=map.length-1)
      return false;

    List<List<Integer>> rotated_arr;
    
    switch(current_mino_idx){
      case 0: // I
        int tmp_arr_four[][] = {
          {map[current_base_y-1][current_base_x-2], map[current_base_y-1][current_base_x-1], map[current_base_y-1][current_base_x], map[current_base_y-1][current_base_x+1]},
          {map[current_base_y][current_base_x-2], map[current_base_y][current_base_x-1], map[current_base_y][current_base_x], map[current_base_y][current_base_x+1]},
          {map[current_base_y+1][current_base_x-2], map[current_base_y+1][current_base_x-1], map[current_base_y+1][current_base_x], map[current_base_y+1][current_base_x+1]},
          {map[current_base_y+2][current_base_x-2], map[current_base_y+2][current_base_x-1], map[current_base_y+2][current_base_x], map[current_base_y+2][current_base_x+1]},
        };
        rotated_arr = rotate(tmp_arr_four, dir);
        // check rotate possibility
        for(int i=-1; i<=2; i++){
          for(int j=-2; j<=1; j++){
            if(rotated_arr.get(i+1).get(j+2) == 2 && map[current_base_y+i][current_base_x+j] == 1)
              return false;
          }
        }
        // reflect
        for(int i=-1; i<=2; i++){
          for(int j=-2; j<=1; j++){
            if(map[current_base_y+i][current_base_x+j] == 2)
              map[current_base_y+i][current_base_x+j] = 0;
          }
        }
        for(int i=-1; i<=2; i++){
          for(int j=-2; j<=1; j++){
            if(rotated_arr.get(i+1).get(j+2) == 2)
              map[current_base_y+i][current_base_x+j] = 2;
          }
        }
        break;
      case 1: // O
        break;
      case 2: // T
      case 3: // L
      case 4: // J
      case 5: // S
      case 6: // Z
        int tmp_arr_three[][] = {
          {map[current_base_y-1][current_base_x-1], map[current_base_y-1][current_base_x], map[current_base_y-1][current_base_x+1]},
          {map[current_base_y][current_base_x-1], map[current_base_y][current_base_x], map[current_base_y][current_base_x+1]},
          {map[current_base_y+1][current_base_x-1], map[current_base_y+1][current_base_x], map[current_base_y+1][current_base_x+1]},
        };
        rotated_arr = rotate(tmp_arr_three, dir);
        // check rotate possibility
        for(int i=-1; i<=1; i++){
          for(int j=-1; j<=1; j++){
            if(rotated_arr.get(i+1).get(j+1) == 2 && map[current_base_y+i][current_base_x+j] == 1)
              return false;
          }
        }
        // reflect
        for(int i=-1; i<=1; i++){
          for(int j=-1; j<=1; j++){
            if(map[current_base_y+i][current_base_x+j] == 2)
              map[current_base_y+i][current_base_x+j] = 0;
          }
        }
        for(int i=-1; i<=1; i++){
          for(int j=-1; j<=1; j++){
            if(rotated_arr.get(i+1).get(j+1) == 2)
              map[current_base_y+i][current_base_x+j] = 2;
          }
        }
        break;
      default:
        System.out.println("error: invalid minoidx");
        break;
    }
    return true;
  }

  private List<List<Integer>> rotate(int[][] A, boolean dir){
    List<List<Integer>> B = new ArrayList<>();
    for(int i=0; i<A[0].length; i++){
      B.add(new ArrayList<>());
      for(int j=0; j<A.length; j++){
        B.get(i).add(0);
      }
    }

    if(dir){  // left
      for(int i=0; i<A.length; i++){
        for(int j=0; j<A[i].length; j++){
          B.get(A[i].length-1-j).set(i, A[i][j]);
        }
      }
    }
    else{ // right
      for(int i=0; i<A.length; i++){
        for(int j=0; j<A[i].length; j++){
          B.get(j).set(A.length-1-i, A[i][j]);
        }
      }
    }
    return B;
  }

  public boolean move(String dir){
    int dir_x = direction.get(dir).get(0);
    int dir_y = direction.get(dir).get(1);
    boolean canmove = true;
    List<List<Integer>> active_list = new ArrayList<>();
    getActiveList(active_list);
    
    for(List<Integer> xy : active_list){
      int x = xy.get(0);
      int y = xy.get(1);
      if(x+dir_x < 0 || y+dir_y < 0 || x+dir_x >= map[0].length || y+dir_y >= map.length || map[y+dir_y][x+dir_x]==1){
        canmove = false;
        break;
      }
    }

    if(canmove){
      // delete active blocks
      for(List<Integer> xy : active_list){
        int x = xy.get(0);
        int y = xy.get(1);
        map[y][x] = 0;
      }
      // reput active blocks
      for(List<Integer> xy : active_list){
        int x = xy.get(0);
        int y = xy.get(1);
        map[y+dir_y][x+dir_x] = 2;
      }
      current_base_x += dir_x;
      current_base_y += dir_y;
      return true;
    }
    return false;
  }

  public void drop(){
    if(move("down")) return;

    // drop to bottom
    List<List<Integer>> active_list = new ArrayList<>();
    getActiveList(active_list);
    
    // set active mino to uncontrolled
    for(List<Integer> xy : active_list){
      int x = xy.get(0);
      int y = xy.get(1);
      map[y][x] = 1;
    }

    // set next mino
    next();
  }

  public boolean next(){
    List<List<Integer>> active_list = new ArrayList<>();
    getActiveList(active_list);
    Random rand = new Random();

    // decide next mino type
    int idx;
    do{
      idx = rand.nextInt(minos.length);
    }while(current_mino_idx == idx);
    current_mino_idx = idx;
    current_base_x = next_base_x;
    current_base_y = next_base_y;

    // put next mino
    for(int block[] : minos[idx]){
      // check gameover
      if(map[next_base_y+block[1]][next_base_x+block[0]]!=0){
        gameover();
        return false;
      }
      map[next_base_y+block[1]][next_base_x+block[0]] = 2;
    }
    lineClear();
    return true;
  }

  public void lineClear(){
    boolean clear_flag;
    int clear_lines = 0;

    for(int i=0; i<map.length; i++){
      clear_flag = true;
      for(int j=0; j<map[i].length; j++){
        if(map[i][j] != 1){
          clear_flag = false;
          break;
        }
      }
      if(clear_flag){
        for(int j=0; j<map[0].length; j++){
          map[i][j] = 0;
        }
        clear_lines++;
        lines++;
        dropPlaced(i);
        levelUp();
        getScore(clear_lines);
      }
    }
  }

  public void dropPlaced(int clear_idx){
    for(int i=clear_idx-1; i>=0; i--){
      for(int j=0; j<map[i].length; j++){
        if(map[i][j] == 1){
          map[i][j] = 0;
          map[i+1][j] = 1;
        }
      }
    }
  }

  public void getScore(int lines){
    if(lines == 1){
      score += 40 * (level+1);
    }
    else if(lines == 2){
      score += 100 * (level+1);
    }
    else if(lines == 3){
      score += 300 * (level+1);
    }
    else if(lines == 4){
      score += 1200 * (level+1);
    }
  }

  public boolean levelUp(){
    if(lines % 10 == 0){
      level++;
      drop_ratio *= 0.90;
      return true;
    }
    return false;
  }

  public void gameover(){
    System.out.println("gameover");
    gameover_flag = true;
    label.setForeground(Color.red);
  }

  // get active mino position
  private void getActiveList(List<List<Integer>> c_list){
    for(int i=0; i<map.length; i++){
      for(int j=0; j<map[i].length; j++){
        if(map[i][j] == 2){
          List<Integer> tmp = new ArrayList<>();
          tmp.add(j);
          tmp.add(i);
          c_list.add(tmp);
        }
      }
    }
  }
  
  // ===========================================================

	public static void main(String[] args) throws Exception {
		Main main = new Main();

		main.loop();
	}

	// game loop
	private void loop(){
    //TODO
    int cnt = 0;
    next();
    
		while(!gameover_flag){
			update(map);
      if(cnt++ == drop_ratio){
        drop();
        cnt = 0;
      }
		}
	}
}

class Label extends JLabel {
  public Label(Color color, String font, int font_size){
    setFont(new Font(font, Font.BOLD, font_size));
    setForeground(color);
  }
}
