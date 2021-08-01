
import java.awt.Color;

public class Tiger extends Critter {
    private int moves;
    private Color currentColor;
 
    public Tiger(){
        moves=0;
        
    }

    public Color getColor(){
        int rand;
        if(moves%3==0){
            rand=(int) (Math.random()*2);
            if(rand==0){
                currentColor=Color.RED;
            }
            if(rand==1){
               currentColor=Color.BLUE; 
            }
            if(rand==2){
                currentColor=Color.GREEN;
            }
        }
        return currentColor;
        
    }
    
    public String toString(){
        return "TGR";
    }

    public Action getMove(CritterInfo info){
        moves++;
        if(info.getFront()==Neighbor.OTHER){
            return Action.INFECT;
        }
        else if(info.getFront()==Neighbor.WALL||info.getRight()==Neighbor.WALL){
            return Action.LEFT;
        }
        else if(info.getFront()==Neighbor.SAME){
            return Action.RIGHT;
        }
        else{
            return Action.HOP;
        }
    }
   }